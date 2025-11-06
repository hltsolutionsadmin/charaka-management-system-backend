package com.hlt.productmanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {

    private Long id;
    private String fullName;
    private String username;
    private String email;
    private Long profilePicture;
    private String primaryContact;
    private String gender;
    private String type;
    private Long postalCode;
    private String fcmToken;
    private String juviId;
    private LocalDate lastLogOutDate;
    private LocalDate recentActivityDate;

    private AddressDTO address;

    private Set<String> roles;
}
