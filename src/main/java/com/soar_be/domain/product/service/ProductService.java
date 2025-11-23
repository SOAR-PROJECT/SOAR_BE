package com.soar_be.domain.product.service;

import com.soar_be.domain.product.dto.ExcelUploadFailedRow;
import com.soar_be.domain.product.dto.ExcelUploadResponse;
import com.soar_be.domain.product.dto.ProductDetailResponse;
import com.soar_be.domain.product.dto.ProductListResponse;
import com.soar_be.domain.product.dto.ProductRequest;
import com.soar_be.domain.product.dto.ProductResponse;
import com.soar_be.domain.product.dto.ProductStatusUpdateRequest;
import com.soar_be.domain.product.dto.ProductUpdateRequest;
import com.soar_be.domain.product.entity.Product;
import com.soar_be.domain.product.entity.ProductStatus;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
            if (product.getRegisteredName().startsWith("PARSING_FAILED:")) {
                failedRows.add(ExcelUploadFailedRow.builder()
                        .row(rowNum)
                        .managementCode(product.getManagementCode())
                        .reason(product.getRegisteredName().replace("PARSING_FAILED:", ""))
                        .build());
                rowNum++;
                continue;
            }

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

    @Transactional
    public ApiResponse<ProductResponse> createProduct(Long userId, ProductRequest request) {
        Store store = findStoreByIdAndValidateOwner(request.getStoreId(), userId);

        if (productRepository.existsByStoreIdAndManagementCode(request.getStoreId(), request.getManagementCode())) {
            throw new CustomException(ErrorCode.PRODUCT_MANAGEMENT_CODE_DUPLICATE);
        }

        Product product = Product.builder()
                .store(store)
                .managementCode(request.getManagementCode())
                .registeredName(request.getRegisteredName())
                .actualProductName(request.getActualProductName())
                .primaryKeyword(request.getPrimaryKeyword())
                .marketplace(request.getMarketplace())
                .registeredDate(request.getRegisteredDate())
                .status(ProductStatus.ACTIVE)
                .build();

        Product savedProduct = productRepository.save(product);

        log.info("Product created - productId: {}, storeId: {}, managementCode: {}",
                savedProduct.getId(), request.getStoreId(), request.getManagementCode());

        ProductResponse response = ProductResponse.from(savedProduct);

        return ApiResponse.success("상품이 등록되었습니다.", response);
    }

    @Transactional(readOnly = true)
    public ApiResponse<ProductListResponse> getProducts(Long userId, Long storeId,
                                                        ProductStatus status, String keyword,
                                                        Pageable pageable) {
        findStoreByIdAndValidateOwner(storeId, userId);

        Page<Product> productPage;

        if (status != null) {
            productPage = productRepository.findAllByStoreIdAndStatus(storeId, status, pageable);
        } else {
            productPage = productRepository.findAllByStoreId(storeId, pageable);
        }

        // TODO: keyword 검색 기능은 추후 구현

        log.info("Products retrieved - storeId: {}, status: {}, page: {}, size: {}",
                storeId, status, pageable.getPageNumber(), productPage.getContent().size());

        ProductListResponse response = ProductListResponse.from(productPage);

        return ApiResponse.success("상품 목록 조회가 완료되었습니다.", response);
    }


    @Transactional(readOnly = true)
    public ApiResponse<ProductDetailResponse> getProductById(Long userId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        if (!product.getStore().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.STORE_ACCESS_DENIED);
        }

        log.info("Product retrieved - productId: {}, userId: {}", productId, userId);

        ProductDetailResponse response = ProductDetailResponse.from(product);

        return ApiResponse.success("상품 조회가 완료되었습니다.", response);
    }


    @Transactional
    public ApiResponse<ProductResponse> updateProduct(Long userId, Long productId,
                                                      ProductUpdateRequest request) {
        Product product = findProductByIdAndValidateOwner(productId, userId);

        product.update(
                request.getRegisteredName(),
                request.getActualProductName(),
                request.getPrimaryKeyword(),
                request.getMarketplace(),
                request.getRegisteredDate()
        );

        log.info("Product updated - productId: {}, userId: {}", productId, userId);

        ProductResponse response = ProductResponse.from(product);

        return ApiResponse.success("상품 정보가 수정되었습니다.", response);
    }

    @Transactional
    public ApiResponse<ProductResponse> updateProductStatus(Long userId, Long productId,
                                                            ProductStatusUpdateRequest request) {
        Product product = findProductByIdAndValidateOwner(productId, userId);

        if (request.getStatus() == ProductStatus.DELETED) {
            throw new CustomException(ErrorCode.PRODUCT_INVALID_STATUS);
        }

        product.updateStatus(request.getStatus());

        String message = request.getStatus() == ProductStatus.INACTIVE
                ? "상품이 비활성화되었습니다."
                : "상품이 활성화되었습니다.";

        log.info("Product status updated - productId: {}, status: {}, userId: {}",
                productId, request.getStatus(), userId);

        ProductResponse response = ProductResponse.from(product);

        return ApiResponse.success(message, response);
    }

    @Transactional
    public ApiResponse<Void> deleteProduct(Long userId, Long productId) {
        Product product = findProductByIdAndValidateOwner(productId, userId);

        product.updateStatus(ProductStatus.DELETED);

        log.info("Product deleted - productId: {}, userId: {}", productId, userId);

        return ApiResponse.success("상품이 삭제되었습니다.", null);
    }

    private Product findProductByIdAndValidateOwner(Long productId, Long userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        if (!product.getStore().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.STORE_ACCESS_DENIED);
        }

        return product;
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