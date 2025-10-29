package com.juvarya.order.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPartnerDTO {

    private Long id;
    private Long userId;
    private String deliveryPartnerId;
    private String vehicleNumber;
    private Boolean active;
    private Boolean available;
    private LocalDateTime lastAssignedTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
