package com.hlt.usermanagement.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "b2b_unit")
@Getter
@Setter
public class B2BUnitModel extends AuditableModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel userModel;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "business_category_id", nullable = false)
    private Long businessCategoryId;

    @Column(name = "contact_number", nullable = false)
    private String contactNumber;

    @Column(name = "approved", nullable = false)
    private boolean approved = false;

    @Column(name = "business_latitude")
    private Double businessLatitude;

    @Column(name = "business_longitude")
    private Double businessLongitude;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id", nullable = false)
    private AddressModel businessAddress;

    @Column(name = "is_temporarily_closed", nullable = false)
    private Boolean isTemporarilyClosed = false;

    @Column(name = "creation_date", nullable = false, updatable = false)
    private LocalDateTime creationDate;

    @PrePersist
    protected void onCreate() {
        this.creationDate = LocalDateTime.now();
    }
}
