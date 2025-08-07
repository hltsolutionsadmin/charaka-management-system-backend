package com.hlt.usermanagement.dto;

import lombok.Data;

@Data
public class ResetPasswordDTO {
    private String oldPassword;
    private String newPassword;

    // getters and setters
}