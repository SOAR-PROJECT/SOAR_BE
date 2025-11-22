package com.soar_be.domain.product.api;

import com.soar_be.auth.details.CustomUserDetails;
import com.soar_be.domain.product.dto.ExcelUploadResponse;
import com.soar_be.global.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

public interface ProductAPI {

    ResponseEntity<ApiResponse<ExcelUploadResponse>> uploadProducts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("storeId") Long storeId,
            @RequestParam("file") MultipartFile file);
}