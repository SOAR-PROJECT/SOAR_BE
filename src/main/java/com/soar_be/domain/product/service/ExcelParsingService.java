package com.soar_be.domain.product.service;

import com.soar_be.domain.product.entity.Product;
import com.soar_be.domain.product.entity.ProductStatus;
import com.soar_be.domain.store.entity.Store;
import com.soar_be.global.exception.CustomException;
import com.soar_be.global.exception.ErrorCode;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelParsingService {
    private static final DateTimeFormatter[] DATE_FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy.MM.dd"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    };

    public List<Product> parseExcelFile(MultipartFile file, Store store) {
        validateExcelFile(file);

        List<Product> products = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                if (row == null || isEmptyRow(row)) {
                    continue;
                }

                try {
                    Product product = parseRow(row, store, i + 1);
                    products.add(product);
                } catch (Exception e) {
                    log.warn("Row {} parsing failed: {}", i + 1, e.getMessage());

                    String managementCode = "";
                    try {
                        managementCode = getCellValue(row, ExcelColumn.MANAGEMENT_CODE.getIndex());
                    } catch (Exception ignored) {
                    }

                    Product failedProduct = Product.builder()
                            .store(store)
                            .managementCode(managementCode)
                            .registeredName("PARSING_FAILED:" + e.getMessage())
                            .actualProductName("")
                            .primaryKeyword("")
                            .marketplace("")
                            .status(ProductStatus.DELETED)
                            .build();

                    products.add(failedProduct);
                }
            }

            log.info("Excel parsing completed - Total products: {}", products.size());

        } catch (IOException e) {
            log.error("Excel file parsing error", e);
            throw new CustomException(ErrorCode.PRODUCT_INVALID_FILE_FORMAT);
        }

        return products;
    }

    private Product parseRow(Row row, Store store, int rowNum) {
        String managementCode = getCellValue(row, ExcelColumn.MANAGEMENT_CODE.getIndex());
        String registeredName = getCellValue(row, ExcelColumn.REGISTERED_NAME.getIndex());
        String actualProductName = getCellValue(row, ExcelColumn.ACTUAL_PRODUCT_NAME.getIndex());
        String primaryKeyword = getCellValue(row, ExcelColumn.PRIMARY_KEYWORD.getIndex());
        String marketplace = getCellValue(row, ExcelColumn.MARKETPLACE.getIndex());
        String registeredDateStr = getCellValue(row, ExcelColumn.REGISTERED_DATE.getIndex());

        validateRequiredField(managementCode, ExcelColumn.MANAGEMENT_CODE, rowNum);
        validateRequiredField(registeredName, ExcelColumn.REGISTERED_NAME, rowNum);
        validateRequiredField(actualProductName, ExcelColumn.ACTUAL_PRODUCT_NAME, rowNum);
        validateRequiredField(primaryKeyword, ExcelColumn.PRIMARY_KEYWORD, rowNum);
        validateRequiredField(marketplace, ExcelColumn.MARKETPLACE, rowNum);

        LocalDate registeredDate = parseRegisteredDate(registeredDateStr);

        return Product.builder()
                .store(store)
                .managementCode(managementCode.trim())
                .registeredName(registeredName.trim())
                .actualProductName(actualProductName.trim())
                .primaryKeyword(primaryKeyword.trim())
                .marketplace(marketplace.trim())
                .registeredDate(registeredDate)
                .status(ProductStatus.ACTIVE)
                .build();
    }

    private String getCellValue(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);

        if (cell == null) {
            return "";
        }

        CellType cellType = cell.getCellType();

        switch (cellType) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return getFormulaResultValue(cell);
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    private void validateRequiredField(String value, ExcelColumn column, int rowNum) {
        if (isEmpty(value)) {
            throw new IllegalArgumentException(
                    String.format("Row %d: 필수 항목(%s)이 누락되었습니다.",
                            rowNum, column.getDisplayName()));
        }
    }

    private LocalDate parseRegisteredDate(String dateStr) {
        if (isEmpty(dateStr)) {
            return null;
        }

        if (dateStr.startsWith("#")) {
            log.warn("Invalid date display: {}", dateStr);
            return null;
        }

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                log.warn("Failed to parse date like: {}", formatter);
                continue;
            }
        }

        log.warn("Failed to parse date: {}", dateStr);
        return null;
    }

    private void validateExcelFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.PRODUCT_INVALID_FILE_FORMAT);
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".xlsx")) {
            throw new CustomException(ErrorCode.PRODUCT_INVALID_FILE_FORMAT);
        }
    }

    private boolean isEmptyRow(Row row) {
        for (ExcelColumn column : ExcelColumn.values()) {
            Cell cell = row.getCell(column.getIndex());
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValue(row, column.getIndex());
                if (!isEmpty(value)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private String getFormulaResultValue(Cell cell) {
        try {
            CellType cachedFormulaResultType = cell.getCachedFormulaResultType();

            switch (cachedFormulaResultType) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                    }
                    return String.valueOf((long) cell.getNumericCellValue());
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                default:
                    return "";
            }
        } catch (Exception e) {
            log.warn("Failed to get formula result value", e);
            return "";
        }
    }
}