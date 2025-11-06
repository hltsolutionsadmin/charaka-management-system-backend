package com.hlt.productmanagement.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "product")
@Data
public class ProductModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "short_code")
    private String shortCode;

    @Column(name = "ignore_tax")
    private boolean ignoreTax;

    @Column(name = "discount")
    private boolean discount;

    @Column(name = "description")
    private String description;

    @Column(name = "business_id")
    private Long businessId;


    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryModel category;

    private Double price;

    private boolean available;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private List<MediaModel> media;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ProductAttributeModel> attributes;

    @Column(name = "shopify_product_id")
    private String shopifyProductId;

    @Column(name = "shopify_variant_id")
    private String shopifyVariantId;

    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @PrePersist
    public void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}
