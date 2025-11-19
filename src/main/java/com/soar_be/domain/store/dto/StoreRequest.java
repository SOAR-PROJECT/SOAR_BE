package com.soar_be.domain.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class StoreRequest {

    @NotBlank(message = "스토어명은 필수입니다.")
    @Size(min = 2, max = 100, message = "스토어명은 2자 이상 100자 이하여야 합니다.")
    private String name;

}
