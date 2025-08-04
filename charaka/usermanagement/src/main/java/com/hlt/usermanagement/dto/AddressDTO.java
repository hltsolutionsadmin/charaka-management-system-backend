package com.hlt.usermanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddressDTO {

    private Long id;
    private String addressLine1;
    private String addressLine2;
    private String street;
    private String city;
    private String state;
    private String country;
    private Double latitude;
    private Double longitude;
    private String postalCode;
    private Long userId;
    private Boolean isDefault;

    public static record ApiKeyDTO(String apiKey, String tenantDbName) {}
}
