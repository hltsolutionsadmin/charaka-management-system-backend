package com.juvarya.order.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "status", nullable = false)
    private String status;

    @OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL)
    @ToString.Exclude
    private PaymentModel payment;
}
