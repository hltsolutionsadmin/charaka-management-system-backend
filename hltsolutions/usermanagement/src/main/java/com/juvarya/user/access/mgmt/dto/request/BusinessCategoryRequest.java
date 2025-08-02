package com.juvarya.user.access.mgmt.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusinessCategoryRequest {
    @NotBlank
    private String name;

    private String description;
}
