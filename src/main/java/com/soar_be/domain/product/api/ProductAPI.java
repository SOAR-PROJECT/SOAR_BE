package com.soar_be.domain.product.api;

import com.soar_be.auth.details.CustomUserDetails;
import com.soar_be.domain.product.dto.ExcelUploadResponse;
import com.soar_be.domain.product.dto.ProductDetailResponse;
import com.soar_be.domain.product.dto.ProductListResponse;
import com.soar_be.domain.product.dto.ProductRequest;
import com.soar_be.domain.product.dto.ProductResponse;
import com.soar_be.domain.product.dto.ProductStatusUpdateRequest;
import com.soar_be.domain.product.dto.ProductUpdateRequest;
import com.soar_be.domain.product.entity.ProductStatus;
import com.soar_be.global.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

public interface ProductAPI {

    ResponseEntity<ApiResponse<ExcelUploadResponse>> uploadProducts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("storeId") Long storeId,
            @RequestParam("file") MultipartFile file);

    ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProductRequest request);

    ResponseEntity<ApiResponse<ProductListResponse>> getProducts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("storeId") Long storeId,
            @RequestParam(value = "status", required = false) ProductStatus status,
            @RequestParam(value = "keyword", required = false) String keyword,
            Pageable pageable);

    ResponseEntity<ApiResponse<ProductDetailResponse>> getProductById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("productId") Long productId);

    ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("productId") Long productId,
            @Valid @RequestBody ProductUpdateRequest request);

    ResponseEntity<ApiResponse<ProductResponse>> updateProductStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("productId") Long productId,
            @Valid @RequestBody ProductStatusUpdateRequest request);

    ResponseEntity<ApiResponse<Void>> deleteProduct(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("productId") Long productId);
}