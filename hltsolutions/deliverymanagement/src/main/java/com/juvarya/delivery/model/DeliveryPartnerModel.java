package com.juvarya.delivery.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "delivery_partners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPartnerModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "delivery_partner_id", unique = true, nullable = false)
    private String deliveryPartnerId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "vehicle_number")
    private String vehicleNumber;

    @Column(name = "is_active")
    private Boolean active;

    @Column(name = "is_available")
    private Boolean available;

    @Column(name = "last_assigned_time")
    private LocalDateTime lastAssignedTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toLocalDateTime();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.available == null) this.available = true;
        if (this.active == null) this.active = true;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toLocalDateTime();
    }
}
