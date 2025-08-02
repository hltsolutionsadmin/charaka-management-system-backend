package com.juvarya.user.access.mgmt.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsernameLoginRequest {
    @NotBlank
    private String username;

    private String fullName;

    private String email;

    private String primaryContact;

    @NotBlank
    private String password;
}
