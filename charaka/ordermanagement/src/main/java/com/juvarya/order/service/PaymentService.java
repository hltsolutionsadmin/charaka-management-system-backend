package com.juvarya.order.service;


import com.hlt.commonservice.user.UserDetailsImpl;
import com.juvarya.order.dto.PaymentTransactionDTO;
import com.juvarya.order.dto.response.PaymentTransactionResponse;
import org.json.JSONArray;

public interface PaymentService {

    PaymentTransactionResponse processPayment(PaymentTransactionDTO dto, UserDetailsImpl user);

    PaymentTransactionResponse refundPayment(String paymentId, Long userId);

    JSONArray getRefundDetails(String paymentId);
}
