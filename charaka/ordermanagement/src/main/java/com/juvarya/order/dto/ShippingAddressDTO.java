package com.juvarya.order.dto;

import lombok.Data;

@Data
public class ShippingAddressDTO {
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
}
