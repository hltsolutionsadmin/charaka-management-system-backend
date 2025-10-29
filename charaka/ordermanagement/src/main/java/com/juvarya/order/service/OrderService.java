package com.juvarya.order.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.hlt.commonservice.user.UserDetailsImpl;
import com.juvarya.order.dto.OrderDTO;
import com.juvarya.order.dto.enums.DeliveryStatus;
import com.juvarya.order.dto.enums.ReportFrequency;
import com.juvarya.order.dto.request.OrderItemUpdateRequest;
import com.juvarya.order.dto.enums.OrderStatus;
import com.juvarya.order.entity.OrderModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {

    OrderDTO createOrder(UserDetailsImpl userDetails, String paymentTransactionId) throws JsonProcessingException;

    Page<OrderDTO> getOrderHistory(UserDetailsImpl user, int page, int size, String sortBy, String direction);

    OrderDTO reorder(String paymentTransactionId, Long previousOrderId, List<OrderItemUpdateRequest> updates, UserDetailsImpl user);

    OrderDTO updateOrderStatus(String orderNumber, OrderStatus status, String notes, String updatedBy);

    OrderDTO getOrderById(Long orderId);

    Page<OrderDTO> getOrdersByBusiness(Long businessId, int page, int size);

    OrderDTO trackOrder(Long orderId, UserDetailsImpl userDetails);

    Page<OrderDTO> filterOrders(Long userId, Long businessId, String orderStatus, LocalDate fromDate, LocalDate toDate, String orderNumber, ReportFrequency frequency, Pageable pageable);


    boolean hasUserOrderedProductFromBusiness(Long userId, Long productId, Long businessId);

    boolean hasUserOrderedFromBusiness(Long userId, Long businessId);

    OrderDTO getOrderByOrderNumber(String orderNumber);

    Page<OrderDTO> getOrdersByStatus(OrderStatus status, Long businessId, Pageable pageable);

    Page<OrderDTO> getOrdersByPartner(String partnerId, Pageable pageable);

    Page<OrderDTO> getOrdersByPartnerAndStatus(String partnerId, DeliveryStatus status, Pageable pageable);

    boolean isDeliveryPartnerAssigned(String deliveryPartnerId);

    boolean validateDeliveryOtp(String orderNumber, String otp);

    boolean triggerOtpForOrder(String orderNumber, String type);
}


