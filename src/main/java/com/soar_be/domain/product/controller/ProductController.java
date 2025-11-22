package com.soar_be.domain.product.controller;

import com.soar_be.auth.details.CustomUserDetails;
import com.soar_be.domain.product.api.ProductAPI;
import com.soar_be.domain.product.dto.ExcelUploadResponse;
import com.soar_be.domain.product.dto.ProductRequest;
import com.soar_be.domain.product.dto.ProductResponse;
import com.soar_be.domain.product.service.ProductService;
import com.soar_be.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
}