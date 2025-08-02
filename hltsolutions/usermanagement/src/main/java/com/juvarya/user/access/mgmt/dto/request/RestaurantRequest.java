package com.juvarya.user.access.mgmt.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestaurantRequest {

    private Long businessId;
    private String businessName;
    private String address;
    private String contactPerson;
    private String contactNumber;
    private boolean approved;
    private String restaurantName;
    private String gstNumber;
    private String fssaiNo;
    private String restaurantAddress;
    private Long userId;
}
