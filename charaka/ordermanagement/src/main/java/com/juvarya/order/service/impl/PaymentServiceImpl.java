package com.juvarya.order.service.impl;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.commonservice.user.UserDetailsImpl;
import com.juvarya.order.dao.CartRepository;
import com.juvarya.order.dao.PaymentRepository;
import com.juvarya.order.dao.TransactionRepository;
import com.juvarya.order.dto.PaymentTransactionDTO;
import com.juvarya.order.dto.response.PaymentTransactionResponse;
import com.juvarya.order.entity.CartModel;
import com.juvarya.order.entity.PaymentModel;
import com.juvarya.order.entity.TransactionModel;
import com.juvarya.order.firebase.listeners.NotificationPublisher;
import com.juvarya.order.service.OrderService;
import com.juvarya.order.service.PaymentService;
import com.hlt.utils.JuavaryaConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final CartRepository cartRepository;
    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationPublisher notificationPublisher;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${razorpay.key_id}")
    private String razorpayKeyId;

    @Value("${razorpay.key_secret}")
    private String razorpayKeySecret;

    private static final String RAZORPAY_API_URL = "https://api.razorpay.com/v1/payments/";

    private OrderService orderService;

    @Autowired
    public void setOrderService(@Lazy OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    @Transactional
    public PaymentTransactionResponse processPayment(PaymentTransactionDTO dto, UserDetailsImpl user) {
        log.info("Processing payment for cartId={}, userId={}", dto.getCartId(), user.getId());

        CartModel cart = cartRepository.findById(dto.getCartId())
                .orElseThrow(() -> new HltCustomerException(ErrorCode.CART_NOT_FOUND, "Cart not found"));

        if (paymentRepository.existsByPaymentId(dto.getPaymentId())) {
            throw new HltCustomerException(ErrorCode.DUPLICATE_PAYMENT, "Payment already processed");
        }

        validateRazorpayAmount(dto);

        TransactionModel transaction = saveTransaction(user.getId(), cart.getBusinessId());

        PaymentModel payment = PaymentModel.builder()
                .status(JuavaryaConstants.VERIFIED)
                .amount(dto.getAmount())
                .creationTime(LocalDateTime.now())
                .paymentId(dto.getPaymentId())
                .razorpayOrderId(dto.getRazorpayOrderId())
                .razorpaySignature(dto.getRazorpaySignature())
                .transaction(transaction)
                .cart(cart)
                .build();
        paymentRepository.save(payment);

        return new PaymentTransactionResponse("Payment processed successfully", JuavaryaConstants.VERIFIED, null);
    }

    @Override
    @Transactional
    public PaymentTransactionResponse refundPayment(String paymentId, Long userId) {
        log.info("Initiating refund for paymentId={}, userId={}", paymentId, userId);

        PaymentModel payment = paymentRepository.findByPaymentId(paymentId)
                .orElseGet(() -> fetchAndPersistPaymentFromRazorpay(paymentId, userId));

        if (JuavaryaConstants.REFUNDED.equalsIgnoreCase(payment.getStatus())) {
            throw new HltCustomerException(ErrorCode.ALREADY_REFUNDED, "Payment already refunded");
        }

        try {
            JSONObject request = new JSONObject()
                    .put("amount", payment.getAmount().multiply(BigDecimal.valueOf(100)).intValue());

            ResponseEntity<String> response = restTemplate.postForEntity(
                    RAZORPAY_API_URL + paymentId + "/refund",
                    new HttpEntity<>(request.toString(), createHeaders()),
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new HltCustomerException(ErrorCode.REFUND_FAILED,
                        "Refund failed with status: " + response.getStatusCode());
            }

            JSONObject refund = new JSONObject(response.getBody());
            updateRefundDetails(payment, refund);

            return new PaymentTransactionResponse("Refund successful", JuavaryaConstants.REFUNDED, null);
        } catch (Exception e) {
            log.error("Refund error for {}: {}", paymentId, e.getMessage(), e);
            throw new HltCustomerException(ErrorCode.INTERNAL_SERVER_ERROR, "Refund failed: " + e.getMessage());
        }
    }

    @Override
    public JSONArray getRefundDetails(String paymentId) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    RAZORPAY_API_URL + paymentId + "/refunds",
                    HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new HltCustomerException(ErrorCode.REFUND_FAILED, "Failed to fetch refund details");
            }

            return new JSONObject(response.getBody()).getJSONArray("items");
        } catch (Exception e) {
            log.error("Error while fetching refund details for {}: {}", paymentId, e.getMessage(), e);
            throw new HltCustomerException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Unable to retrieve refund details at this time.");
        }
    }

    private void validateRazorpayAmount(PaymentTransactionDTO dto) {
        Integer razorpayAmount = fetchRazorpayAmount(dto.getPaymentId());
        if (razorpayAmount == null || dto.getAmount().compareTo(BigDecimal.valueOf(razorpayAmount)) != 0) {
            throw new HltCustomerException(ErrorCode.PAYMENT_AMOUNT_MISMATCH, "Amount mismatch with Razorpay");
        }
    }

    private Integer fetchRazorpayAmount(String paymentId) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    RAZORPAY_API_URL + paymentId,
                    HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    String.class
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                return new JSONObject(response.getBody()).getInt("amount") / 100;
            }
        } catch (Exception e) {
            log.error("Failed to fetch payment amount for {}: {}", paymentId, e.getMessage(), e);
        }
        return null;
    }

    private PaymentModel fetchAndPersistPaymentFromRazorpay(String paymentId, Long userId) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    RAZORPAY_API_URL + paymentId,
                    HttpMethod.GET,
                    new HttpEntity<>(createHeaders()),
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new HltCustomerException(ErrorCode.PAYMENT_NOT_FOUND,
                        "Razorpay responded with: " + response.getStatusCode());
            }

            JSONObject rp = new JSONObject(response.getBody());
            BigDecimal amount = BigDecimal.valueOf(rp.getInt("amount")).divide(BigDecimal.valueOf(100));
            String status = rp.optString("status");
            String orderId = rp.optString("order_id");

            CartModel cart = cartRepository.findByUserId(userId)
                    .orElseThrow(() -> new HltCustomerException(ErrorCode.CART_NOT_FOUND,
                            "Cart not found for userId=" + userId));

            TransactionModel transaction = saveTransaction(cart.getUserId(), cart.getBusinessId());

            PaymentModel payment = PaymentModel.builder()
                    .paymentId(paymentId)
                    .razorpayOrderId(orderId)
                    .amount(amount)
                    .status(status)
                    .creationTime(LocalDateTime.now())
                    .cart(cart)
                    .transaction(transaction)
                    .build();

            return paymentRepository.save(payment);
        } catch (Exception e) {
            log.error("Failed to fetch Razorpay payment for {}: {}", paymentId, e.getMessage(), e);
            throw new HltCustomerException(ErrorCode.PAYMENT_NOT_FOUND,
                    "Unable to fetch payment from Razorpay for ID=" + paymentId);
        }
    }

    private TransactionModel saveTransaction(Long customerId, Long ownerId) {
        return transactionRepository.save(TransactionModel.builder()
                .transactionDate(LocalDateTime.now())
                .customerId(customerId)
                .ownerId(ownerId)
                .status(JuavaryaConstants.PENDING)
                .build());
    }

    private void updateRefundDetails(PaymentModel payment, JSONObject refund) {
        payment.setStatus(JuavaryaConstants.REFUNDED);
        payment.setRefundedAt(LocalDateTime.now());
        payment.setRefundAmount(payment.getAmount());
        payment.setRefundId(refund.optString("id"));
        paymentRepository.save(payment);
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(razorpayKeyId, razorpayKeySecret, StandardCharsets.UTF_8);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
