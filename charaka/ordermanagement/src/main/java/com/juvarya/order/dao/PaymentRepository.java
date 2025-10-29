package com.juvarya.order.dao;


import com.juvarya.order.entity.PaymentModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentModel, Long> {
    Optional<PaymentModel> findByRazorpayOrderId(String razorpayOrderId);

    Optional<PaymentModel> findByPaymentId(String paymentId);

    boolean existsByPaymentId(String paymentId);
}

