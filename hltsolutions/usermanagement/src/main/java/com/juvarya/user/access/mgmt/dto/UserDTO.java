package com.juvarya.user.access.mgmt.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.juvarya.commonservice.enums.UserVerificationStatus;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {

    private Long id;
    private String fullName;
    private String username;
    private String email;
    private String rollNumber;
    private Long year;
    private String qualification;
    private Long profilePicture;
    private String primaryContact;
    private String gender;
    private String type;
    private String postalCode;
    private String fcmToken;
    private String juviId;
    private LocalDate lastLogOutDate;
    private LocalDate recentActivityDate;
    private List<AddressDTO> addresses;
    private String branch;
    private UserVerificationStatus userVerificationStatus;

    private boolean skillrat;
    private boolean yardly;
    private boolean eato;
    private boolean sancharalakshmi;

    private Set<String> roles;
    @JsonIgnore
    private String password;


}
