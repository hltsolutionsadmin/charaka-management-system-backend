package com.juvarya.order.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "carts")
public class CartModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "cart_id")
    private String cartId;

    @Column(name = "status")
    private String status;

    @Column(name = "shipping_address_id")
    private Long shippingAddressId;

    @Column(name = "shopify_cart_id")
    private String shopifyCartId;

    @Column(name = "business_id")
    private Long businessId;

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "notes")
    private String notes;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonManagedReference
    private List<CartItemModel> cartItems;


    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_time", insertable = false)
    private LocalDateTime updatedDate;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL)
    @ToString.Exclude
    @JsonManagedReference
    private List<PaymentModel> payments = new ArrayList<>();


    public void addCartItem(CartItemModel item) {
        item.setCart(this);
        this.cartItems.add(item);
    }

    public void removeCartItem(CartItemModel item) {
        item.setCart(null);
        this.cartItems.remove(item);
    }
}
