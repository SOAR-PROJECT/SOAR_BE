package com.soar_be.domain.product.service;

import com.soar_be.domain.product.dto.ExcelUploadFailedRow;
import com.soar_be.domain.product.dto.ExcelUploadResponse;
import com.soar_be.domain.product.entity.Product;
import com.soar_be.domain.product.repository.ProductRepository;
import com.soar_be.domain.store.entity.Store;
import com.soar_be.domain.store.repository.StoreRepository;
import com.soar_be.global.dto.ApiResponse;
import com.soar_be.global.exception.CustomException;
import com.soar_be.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final ExcelParsingService excelParsingService;

    @Transactional
    public ApiResponse<ExcelUploadResponse> uploadProducts(Long userId, Long storeId, MultipartFile file) {
        Store store = findStoreByIdAndValidateOwner(storeId, userId);

        List<Product> parsedProducts = excelParsingService.parseExcelFile(file, store);

        List<Product> successProducts = new ArrayList<>();
        List<ExcelUploadFailedRow> failedRows = new ArrayList<>();
        int rowNum = 2;

        for (Product product : parsedProducts) {
            try {
                validateManagementCodeDuplicate(storeId, product.getManagementCode(), rowNum);

                productRepository.save(product);
                successProducts.add(product);

            } catch (CustomException e) {
                failedRows.add(ExcelUploadFailedRow.builder()
                        .row(rowNum)
                        .managementCode(product.getManagementCode())
                        .reason(e.getMessage())
                        .build());
                log.warn("Product save failed - row: {}, code: {}, reason: {}",
                        rowNum, product.getManagementCode(), e.getMessage());
            } catch (Exception e) {
                failedRows.add(ExcelUploadFailedRow.builder()
                        .row(rowNum)
                        .managementCode(product.getManagementCode())
                        .reason("상품 등록 중 오류가 발생했습니다.")
                        .build());
                log.error("Unexpected error during product save - row: {}", rowNum, e);
            }

            rowNum++;
        }

        log.info("Excel upload completed - storeId: {}, total: {}, success: {}, fail: {}",
                storeId, parsedProducts.size(), successProducts.size(), failedRows.size());

        ExcelUploadResponse response = ExcelUploadResponse.builder()
                .storeId(storeId)
                .totalRows(parsedProducts.size())
                .successCount(successProducts.size())
                .failCount(failedRows.size())
                .failedRows(failedRows)
                .build();

        return ApiResponse.success(response.getSuccessMessage(), response);
    }

    private Store findStoreByIdAndValidateOwner(Long storeId, Long userId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        if (!store.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.STORE_ACCESS_DENIED);
        }

        return store;
    }

    private void validateManagementCodeDuplicate(Long storeId, String managementCode, int rowNum) {
        if (productRepository.existsByStoreIdAndManagementCode(storeId, managementCode)) {
            throw new CustomException(ErrorCode.PRODUCT_MANAGEMENT_CODE_DUPLICATE,
                    String.format("Row %d: 중복된 관리번호입니다.", rowNum));
        }
    }
}