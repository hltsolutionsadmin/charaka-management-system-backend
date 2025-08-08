package com.hlt.usermanagement.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "B2B_UNIT")
@Getter
@Setter
public class B2BUnitModel extends AuditableModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "OWNER_USER_ID", nullable = false)
    private UserModel owner;

    @Column(name = "BUSINESS_NAME", nullable = false, length = 150)
    private String businessName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ID")
    private BusinessCategoryModel category;

    @Column(name = "CONTACT_NUMBER", nullable = false, length = 20)
    private String contactNumber;

    @Column(name = "BUSINESS_LATITUDE", precision = 10, scale = 6)
    private BigDecimal businessLatitude;

    @Column(name = "BUSINESS_LONGITUDE", precision = 10, scale = 6)
    private BigDecimal businessLongitude;


    @OneToMany(mappedBy = "b2bUnitModel", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BusinessAttributeModel> attributes;

    @Column(name = "ENABLED", nullable = false)
    private boolean enabled = true;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ADDRESS_ID", nullable = false)
    private AddressModel businessAddress;

    @Column(name = "IS_TEMPORARILY_CLOSED", nullable = false)
    private boolean isTemporarilyClosed = false;

    @Column(name = "CREATION_DATE", nullable = false, updatable = false)
    private LocalDateTime creationDate;

    @PrePersist
    protected void onCreate() {
        this.creationDate = LocalDateTime.now();
    }
}
