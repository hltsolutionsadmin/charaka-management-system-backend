package com.juvarya.order.controllers;

import com.hlt.commonservice.user.UserDetailsImpl;
import com.juvarya.order.aspect.UserLock;
import com.juvarya.order.dto.PaymentTransactionDTO;
import com.juvarya.order.dto.enums.LockType;
import com.juvarya.order.dto.response.PaymentTransactionResponse;
import com.juvarya.order.service.OrderService;
import com.juvarya.order.service.PaymentService;
import com.hlt.utils.JTBaseEndpoint;
import com.hlt.utils.JuavaryaConstants;
import com.hlt.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/payments")
public class PaymentController extends JTBaseEndpoint {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    @UserLock(LockType.PAYMENT)
    @PostMapping("/process")
    public ResponseEntity<PaymentTransactionResponse> processPayment(@RequestBody PaymentTransactionDTO dto) {
        try {
            UserDetailsImpl user = SecurityUtils.getCurrentUserDetails();
            log.info("Processing payment for user {}: {}", user.getId(), dto);

            return ResponseEntity.ok(paymentService.processPayment(dto, user));
        } catch (IllegalArgumentException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (DataIntegrityViolationException e) {
            String message = "Duplicate payment ID or data integrity error: " + e.getRootCause().getMessage();
            return buildErrorResponse(message, HttpStatus.CONFLICT);
        } catch (Exception e) {
            log.error("Unexpected error during payment processing", e);
            return buildErrorResponse("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/refund/{paymentId}")
    public ResponseEntity<PaymentTransactionResponse> refund(@PathVariable String paymentId) {
        var user = SecurityUtils.getCurrentUserDetails();
        return ResponseEntity.ok(paymentService.refundPayment(paymentId, user.getId()));
    }

    @GetMapping("/refunds/{paymentId}")
    public ResponseEntity<PaymentTransactionResponse> getRefundStatus(@PathVariable String paymentId) {
        try {
            JSONArray refunds = paymentService.getRefundDetails(paymentId);

            if (refunds.length() == 0) {
                return buildSuccessResponse("Refund not yet initiated or not found.", JuavaryaConstants.PENDING);
            }

            JSONObject latest = refunds.getJSONObject(0);
            String status = latest.optString("status", "unknown").toLowerCase();

            return switch (status) {
                case "processed", "completed" -> buildSuccessResponse(
                        "Refund processed. It should reflect in your account within 1–3 business days.",
                        JuavaryaConstants.REFUNDED);
                case "pending" -> buildSuccessResponse(
                        "Refund initiated. Expected to complete within 5 business days.",
                        JuavaryaConstants.PENDING);
                default -> buildSuccessResponse(
                        "Refund status: " + status + ". Please check again later.",
                        JuavaryaConstants.PROCESSING);
            };
        } catch (Exception e) {
            log.error("Refund tracking failed for paymentId={}", paymentId, e);
            return buildErrorResponse("Unable to track refund status at this time.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<PaymentTransactionResponse> buildErrorResponse(String message, HttpStatus status) {
        return ResponseEntity.status(status).body(
                new PaymentTransactionResponse(message, JuavaryaConstants.FAILED, null)
        );
    }

    private ResponseEntity<PaymentTransactionResponse> buildSuccessResponse(String message, String status) {
        return ResponseEntity.ok(
                new PaymentTransactionResponse(message, status, null)
        );
    }
}
