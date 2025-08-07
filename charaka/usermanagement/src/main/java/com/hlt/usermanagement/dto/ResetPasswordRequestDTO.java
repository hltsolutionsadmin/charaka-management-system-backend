package com.hlt.usermanagement.dto;

import lombok.Data;

@Data
public class ResetPasswordRequestDTO {
    private String token;
    private String newPassword;

    // getters and setters
}