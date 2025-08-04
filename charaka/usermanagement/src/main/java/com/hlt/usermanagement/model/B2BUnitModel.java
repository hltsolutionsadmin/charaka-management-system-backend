package com.hlt.usermanagement.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.units.qual.A;

import java.time.LocalDateTime;
import java.util.Set;

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

    @ManyToOne
    @JoinColumn(name = "category_id")
    private BusinessCategoryModel category;

    @Column(name = "contact_number", nullable = false)
    private String contactNumber;

    @Column(name = "business_latitude")
    private Double businessLatitude;

    @Column(name = "business_longitude")
    private Double businessLongitude;

    @OneToMany(mappedBy = "b2bUnitModel", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BusinessAttributeModel> attributes;

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
