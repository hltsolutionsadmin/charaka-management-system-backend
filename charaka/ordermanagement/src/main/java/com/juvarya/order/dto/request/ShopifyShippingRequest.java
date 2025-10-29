package com.juvarya.order.dto.request;

import lombok.Data;

@Data
public class ShopifyShippingRequest {
    private String cartId;
    private String firstName;
    private String lastName;
    private String address1;
    private String city;
    private String province;
    private String zip;
    private String country;
    private String phone;
}