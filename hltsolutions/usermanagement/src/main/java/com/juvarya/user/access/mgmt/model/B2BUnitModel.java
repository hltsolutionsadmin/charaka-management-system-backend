package com.juvarya.user.access.mgmt.model;

import com.juvarya.user.access.mgmt.dto.enums.EnabledStatusSource;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "b2b_unit")
@Getter
@Setter
public class B2BUnitModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserModel userModel;

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "approved")
    private boolean approved;

    @Column(name = "business_latitude")
    private Double businessLatitude;

    @Column(name = "business_longitude")
    private Double businessLongitude;

    @Column(name = "enabled")
    private boolean enabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "enabled_status_source", nullable = false)
    private EnabledStatusSource enabledStatusSource = EnabledStatusSource.SCHEDULER;   // prevent scheduler override

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private AddressModel businessAddress;

    @Column(name = "is_temporarily_closed", nullable = false)
    private Boolean isTemporarilyClosed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_key_id")
    private ApiKeyModel apiKeyOwner;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private BusinessCategoryModel category;

    @OneToMany(mappedBy = "b2bUnitModel", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductAttributeModel> attributes;

    @Column(nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime creationDate;

    @OneToMany(mappedBy = "b2bUnitModel", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<MediaModel> media;

    @PrePersist
    protected void onCreate() {
        this.creationDate = LocalDateTime.now();
    }


}
