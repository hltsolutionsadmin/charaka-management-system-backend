package com.juvarya.delivery.dto;

import com.juvarya.commonservice.enums.ERole;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

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
