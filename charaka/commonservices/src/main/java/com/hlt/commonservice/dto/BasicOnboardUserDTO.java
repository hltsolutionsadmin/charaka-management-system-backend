package com.hlt.commonservice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hlt.commonservice.enums.ERole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
public class BasicOnboardUserDTO {
    private String username;
    private String email;
    private String fullName;
    private String primaryContact;
    private Set<ERole> userRoles;
    private Long businessId;
    private String password;

    @JsonCreator
    public BasicOnboardUserDTO(@JsonProperty("username") String username, @JsonProperty("email") String email,
                               @JsonProperty("fullName") String fullName, @JsonProperty("primaryContact") String primaryContact,
                               @JsonProperty("userRoles") Set<ERole> userRoles, @JsonProperty("businessId") Long businessId,
                               @JsonProperty("password") String password) {

        this.email = email;
        this.fullName = fullName;
        this.username = username;
        this.primaryContact = primaryContact;
        this.userRoles = userRoles;
        this.businessId = businessId;
        this.password = password;

    }

}
