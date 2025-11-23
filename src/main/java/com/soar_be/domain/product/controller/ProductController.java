package com.soar_be.domain.product.controller;

import com.soar_be.auth.details.CustomUserDetails;
import com.soar_be.domain.product.api.ProductAPI;
import com.soar_be.domain.product.dto.ExcelUploadResponse;
import com.soar_be.domain.product.dto.ProductDetailResponse;
import com.soar_be.domain.product.dto.ProductListResponse;
import com.soar_be.domain.product.dto.ProductRequest;
import com.soar_be.domain.product.dto.ProductResponse;
import com.soar_be.domain.product.dto.ProductStatusUpdateRequest;
import com.soar_be.domain.product.dto.ProductUpdateRequest;
import com.soar_be.domain.product.entity.ProductStatus;
import com.soar_be.domain.product.service.ProductService;
import com.soar_be.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController implements ProductAPI {

    private final ProductService productService;

    @Override
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<ExcelUploadResponse>> uploadProducts(
            CustomUserDetails userDetails,
            Long storeId,
            MultipartFile file) {

        log.debug("Product upload request - userId: {}, storeId: {}, filename: {}",
                userDetails.getUserId(), storeId, file.getOriginalFilename());

        ApiResponse<ExcelUploadResponse> response = productService.uploadProducts(
                userDetails.getUserId(),
                storeId,
                file
        );

        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(CustomUserDetails userDetails,
                                                                      ProductRequest request) {
        log.debug("Product create request - userId: {}, storeId: {}, managementCode: {}",
                userDetails.getUserId(), request.getStoreId(), request.getManagementCode());

        ApiResponse<ProductResponse> response = productService.createProduct(
                userDetails.getUserId(),
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<ProductListResponse>> getProducts(
            CustomUserDetails userDetails,
            @RequestParam("storeId") Long storeId,
            @RequestParam(value = "status", required = false) ProductStatus status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Product list request - userId: {}, storeId: {}, status: {}, page: {}",
                userDetails.getUserId(), storeId, status, pageable.getPageNumber());

        ApiResponse<ProductListResponse> response = productService.getProducts(
                userDetails.getUserId(),
                storeId,
                status,
                keyword,
                pageable
        );

        return ResponseEntity.ok(response);
    }


    @Override
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductById(
            CustomUserDetails userDetails,
            @PathVariable("productId") Long productId) {

        log.debug("Product detail request - userId: {}, productId: {}",
                userDetails.getUserId(), productId);

        ApiResponse<ProductDetailResponse> response = productService.getProductById(
                userDetails.getUserId(),
                productId
        );

        return ResponseEntity.ok(response);
    }

    @Override
    @PatchMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            CustomUserDetails userDetails,
            @PathVariable("productId") Long productId,
            @Valid @RequestBody ProductUpdateRequest request) {

        log.debug("Product update request - userId: {}, productId: {}",
                userDetails.getUserId(), productId);

        ApiResponse<ProductResponse> response = productService.updateProduct(
                userDetails.getUserId(),
                productId,
                request
        );

        return ResponseEntity.ok(response);
    }


    @Override
    @PatchMapping("/{productId}/status")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProductStatus(
            CustomUserDetails userDetails,
            @PathVariable("productId") Long productId,
            @Valid @RequestBody ProductStatusUpdateRequest request) {

        log.debug("Product status update request - userId: {}, productId: {}, status: {}",
                userDetails.getUserId(), productId, request.getStatus());

        ApiResponse<ProductResponse> response = productService.updateProductStatus(
                userDetails.getUserId(),
                productId,
                request
        );

        return ResponseEntity.ok(response);
    }


    @Override
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            CustomUserDetails userDetails,
            @PathVariable("productId") Long productId) {

        log.debug("Product delete request - userId: {}, productId: {}",
                userDetails.getUserId(), productId);

        ApiResponse<Void> response = productService.deleteProduct(
                userDetails.getUserId(),
                productId
        );

        return ResponseEntity.ok(response);
    }

}