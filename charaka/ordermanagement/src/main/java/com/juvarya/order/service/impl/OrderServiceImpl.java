package com.juvarya.order.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.commonservice.dto.LoginRequest;
import com.hlt.commonservice.dto.UserOTPDTO;
import com.hlt.commonservice.user.UserDetailsImpl;
import com.juvarya.order.client.DeliveryClient;
import com.juvarya.order.client.ProductClient;
import com.juvarya.order.client.RestaurantFeignClient;
import com.juvarya.order.client.UserMgmtClient;
import com.juvarya.order.dao.CartRepository;
import com.juvarya.order.dao.OrderRepository;
import com.juvarya.order.dto.OrderDTO;
import com.juvarya.order.dto.OrderSummaryDTO;
import com.juvarya.order.dto.ProductDTO;
import com.juvarya.order.dto.enums.*;
import com.juvarya.order.dto.request.UpdateOrderStatusRequest;
import com.juvarya.order.dto.request.ComplaintCreateRequest;
import com.juvarya.order.dto.request.OrderItemUpdateRequest;
import com.juvarya.order.dto.response.PaymentTransactionResponse;
import com.juvarya.order.entity.CartItemModel;
import com.juvarya.order.entity.CartModel;
import com.juvarya.order.entity.OrderItemModel;
import com.juvarya.order.entity.OrderModel;
import com.juvarya.order.firebase.dto.Notification;
import com.juvarya.order.firebase.dto.NotificationEventType;
import com.juvarya.order.firebase.listeners.NotificationPublisher;
import com.juvarya.order.populator.OrderPopulator;
import com.juvarya.order.service.ComplaintService;
import com.juvarya.order.service.OrderService;
import com.juvarya.order.service.PaymentService;
import com.juvarya.order.utils.OrderSpecification;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final String DEFAULT_SORT_FIELD = "createdDate";
    private static final Random RANDOM = new Random();

    private final PaymentService paymentService;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderPopulator orderPopulator;
    private final ComplaintService complaintService;
    private final RestaurantFeignClient restaurantFeignClient;
    private final ProductClient productClient;
    private final NotificationPublisher notificationPublisher;
    private final DeliveryClient deliveryClient;
    private final UserMgmtClient userMgmtClient;

    @Override
    @Transactional
    public OrderDTO createOrder(UserDetailsImpl user, String paymentTransactionId) throws JsonProcessingException {
        validatePaymentTransactionId(paymentTransactionId);
        Long businessId = null;

        try {
            CartModel cart = getCartForUser(user.getId());
            Map<Long, ProductDTO> productMap = fetchProductDetails(cart);
            OrderModel order = buildOrderFromCart(user, paymentTransactionId, cart, productMap);
            businessId = order.getBusinessId();

            OrderModel savedOrder = orderRepository.save(order);
            OrderDTO response = populateOrderDTO(savedOrder);
            sendOrderNotifications(response, user.getId());

            return response;
        } catch (Exception e) {
            log.error("Order creation failed for userId={}, paymentId={}", user.getId(), paymentTransactionId, e);
            sendOrderFailureAfterPayment(user.getId(), businessId);
            throw e;
        }
    }

    private void validatePaymentTransactionId(String paymentTransactionId) {
        if (StringUtils.isBlank(paymentTransactionId)) {
            throw new HltCustomerException(ErrorCode.PAYMENT_TRANSACTION_ID_REQUIRED);
        }
    }

    private CartModel getCartForUser(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.CART_NOT_FOUND));
    }

    private Map<Long, ProductDTO> fetchProductDetails(CartModel cart) {
        return cart.getCartItems().stream().collect(Collectors.toMap(
                CartItemModel::getProductId,
                item -> {
                    try {
                        return productClient.getProductById(item.getProductId());
                    } catch (Exception e) {
                        log.warn("Product fetch failed for productId={}: {}", item.getProductId(), e.getMessage());
                        return defaultFallbackProduct();
                    }
                },
                (existing, replacement) -> existing
        ));
    }

    private ProductDTO defaultFallbackProduct() {
        ProductDTO fallback = new ProductDTO();
        fallback.setBusinessId(1L);
        fallback.setBusinessName("Unknown");
        fallback.setIgnoreTax(true);
        fallback.setTaxPercentage(BigDecimal.ZERO);
        return fallback;
    }

    private OrderModel buildOrderFromCart(UserDetailsImpl user, String paymentTransactionId, CartModel cart, Map<Long, ProductDTO> productMap) {
        OrderModel order = new OrderModel();
        order.setUserId(user.getId());
        order.setShippingAddressId(cart.getShippingAddressId());
        order.setOrderStatus(OrderStatus.PLACED);
        order.setPaymentStatus("PAID");
        order.setPaymentTransactionId(paymentTransactionId);
        order.setTaxInclusive(false);
        order.setNotes(cart.getNotes());

        CartItemModel firstCartItem = cart.getCartItems().get(0);
        ProductDTO firstProduct = productMap.get(firstCartItem.getProductId());

        order.setBusinessId(firstProduct.getBusinessId());
        order.setBusinessName(firstProduct.getBusinessName());
        order.setOrderNumber(generateOrderNumber(firstProduct.getBusinessId(), user.getId()));

        List<OrderItemModel> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        for (CartItemModel cartItem : cart.getCartItems()) {
            ProductDTO product = productMap.get(cartItem.getProductId());
            OrderItemModel item = buildOrderItem(order, cartItem, product);
            orderItems.add(item);
            totalAmount = totalAmount.add(item.getTotalAmount());
            totalTax = totalTax.add(item.getTaxAmount());
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);
        order.setTotalTaxAmount(totalTax);
        return order;
    }

    private OrderItemModel buildOrderItem(OrderModel order, CartItemModel cartItem, ProductDTO product) {
        BigDecimal quantity = BigDecimal.valueOf(cartItem.getQuantity());
        BigDecimal price = cartItem.getPrice();
        BigDecimal baseAmount = price.multiply(quantity);

        boolean ignoreTax = Boolean.TRUE.equals(product.getIgnoreTax());
        BigDecimal taxPercentage = Optional.ofNullable(product.getTaxPercentage()).orElse(BigDecimal.valueOf(5));
        BigDecimal taxAmount = ignoreTax ? BigDecimal.ZERO : baseAmount.multiply(taxPercentage).divide(BigDecimal.valueOf(100));
        BigDecimal totalAmount = baseAmount.add(taxAmount);

        OrderItemModel item = new OrderItemModel();
        item.setOrder(order);
        item.setProductId(cartItem.getProductId());
        item.setQuantity(cartItem.getQuantity());
        item.setPrice(price);
        item.setTaxIgnored(ignoreTax);
        item.setTaxPercentage(ignoreTax ? BigDecimal.ZERO : taxPercentage);
        item.setTaxAmount(taxAmount);
        item.setTotalAmount(totalAmount);
        item.setEntryNumber(cartItem.getId());

        return item;
    }

    private OrderDTO populateOrderDTO(OrderModel order) {
        return getOrderDTO(order);
    }

    private void sendOrderFailureAfterPayment(Long userId, Long businessId) {
        try {
            Notification notification = Notification.buildNotification(
                    userId,
                    businessId != null ? businessId : 0L,
                    NotificationEventType.ORDER_PAYMENT_SUCCESS_BUT_FAILED,
                    Collections.emptyMap()
            );
            notificationPublisher.sendNotifications(notification, NotificationEventType.ORDER_PAYMENT_SUCCESS_BUT_FAILED);
            log.info("Sent ORDER_PAYMENT_SUCCESS_BUT_FAILED notification to userId={}", userId);
        } catch (Exception e) {
            log.error("Failed to send ORDER_PAYMENT_SUCCESS_BUT_FAILED notification to userId={}", userId, e);
        }
    }

    private void sendOrderNotifications(OrderDTO orderDTO, Long customerId) {
        try {
            Map<String, String> params = Map.of(
                    "orderNumber", orderDTO.getOrderNumber(),
                    "restaurantName", orderDTO.getBusinessName(),
                    "totalAmount", orderDTO.getTotalAmount().toPlainString()
            );

            Notification customerNotification = Notification.buildNotification(
                    customerId,
                    orderDTO.getBusinessId(),
                    NotificationEventType.ORDER_PLACED_CUSTOMER,
                    params
            );

            notificationPublisher.sendNotifications(customerNotification, NotificationEventType.ORDER_PLACED_CUSTOMER);
            log.info("Sent ORDER_PLACED_CUSTOMER notification for userId={}, orderId={}", customerId, orderDTO.getId());
        } catch (Exception e) {
            log.error("Failed to send ORDER_PLACED_CUSTOMER notification for userId={}, orderId={}", customerId, orderDTO.getId(), e);
        }
    }

    private void sendOrderDeliverNotifications(OrderDTO orderDTO, Long customerId) {
        try {
            Map<String, String> params = Map.of(
                    "orderNumber", orderDTO.getOrderNumber(),
                    "restaurantName", orderDTO.getBusinessName(),
                    "totalAmount", orderDTO.getTotalAmount().toPlainString()
            );

            Notification customerNotification = Notification.buildNotification(
                    customerId,
                    orderDTO.getBusinessId(),
                    NotificationEventType.ORDER_DELIVERED_CUSTOMER,
                    params
            );

            notificationPublisher.sendNotifications(customerNotification, NotificationEventType.ORDER_PLACED_CUSTOMER);
            log.info("Sent ORDER_PLACED_CUSTOMER notification for userId={}, orderId={}", customerId, orderDTO.getId());
        } catch (Exception e) {
            log.error("Failed to send ORDER_PLACED_CUSTOMER notification for userId={}, orderId={}", customerId, orderDTO.getId(), e);
        }
    }

    private String generateOrderNumber(Long restaurantId, Long userId) {
        final String restaurantCode = "RS" + restaurantId;
        final String userCode = "U" + userId;
        final String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmm"));
        final int random = RANDOM.nextInt(9000) + 1000;
        return String.format("%s-%s-%s-%d", restaurantCode, timestamp, userCode, random);
    }

    @Override
    public Page<OrderDTO> getOrderHistory(UserDetailsImpl user, int page, int size, String sortBy, String direction) {
        List<String> allowedSortFields = List.of(DEFAULT_SORT_FIELD, "id", "totalAmount", "orderNumber");
        if (!allowedSortFields.contains(sortBy)) {
            log.warn("Invalid sort field '{}', defaulting to 'createdDate'", sortBy);
            sortBy = DEFAULT_SORT_FIELD;
        }

        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid sort direction '{}', defaulting to DESC", direction);
            sortDirection = Sort.Direction.DESC;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<OrderModel> orderPage = orderRepository.findByUserId(user.getId(), pageable);

        return orderPage.map(this::getOrderDTO);
    }

    @Override
    @Transactional
    public OrderDTO reorder(String paymentTransactionId, Long previousOrderId, List<OrderItemUpdateRequest> updates, UserDetailsImpl user) {
        OrderModel previousOrder = orderRepository.findById(previousOrderId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.ORDER_NOT_FOUND));

        if (!Objects.equals(previousOrder.getUserId(), user.getId())) {
            throw new HltCustomerException(ErrorCode.UNAUTHORIZED);
        }

        Map<Long, Integer> updatedQuantities = updates.stream()
                .collect(Collectors.toMap(OrderItemUpdateRequest::getProductId, OrderItemUpdateRequest::getQuantity));

        OrderModel newOrder = buildReorder(previousOrder, updatedQuantities, paymentTransactionId);
        OrderModel savedOrder = orderRepository.save(newOrder);

        return populateOrderDTO(savedOrder);
    }

    private OrderModel buildReorder(OrderModel previousOrder, Map<Long, Integer> updatedQuantities, String paymentTransactionId) {
        OrderModel newOrder = new OrderModel();
        newOrder.setUserId(previousOrder.getUserId());
        newOrder.setShippingAddressId(previousOrder.getShippingAddressId());
        newOrder.setOrderStatus(OrderStatus.PLACED);
        newOrder.setPaymentStatus("PAID");
        newOrder.setBusinessId(previousOrder.getBusinessId());
        newOrder.setBusinessName(previousOrder.getBusinessName());
        newOrder.setPaymentTransactionId(paymentTransactionId);
        newOrder.setOrderNumber(generateOrderNumber(previousOrder.getBusinessId(), previousOrder.getUserId()));

        List<OrderItemModel> newItems = previousOrder.getOrderItems().stream()
                .filter(item -> updatedQuantities.containsKey(item.getProductId()))
                .map(item -> {
                    OrderItemModel newItem = new OrderItemModel();
                    newItem.setOrder(newOrder);
                    newItem.setProductId(item.getProductId());
                    newItem.setPrice(item.getPrice());
                    newItem.setQuantity(updatedQuantities.get(item.getProductId()));
                    newItem.setTotalAmount(item.getPrice().multiply(BigDecimal.valueOf(updatedQuantities.get(item.getProductId()))));
                    return newItem;
                })
                .toList();

        BigDecimal totalAmount = newItems.stream()
                .map(OrderItemModel::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        newOrder.setOrderItems(newItems);
        newOrder.setTotalAmount(totalAmount);
        return newOrder;
    }


    @Override
    @Transactional
    public OrderDTO updateOrderStatus(String orderNumber, OrderStatus status, String notes, String updatedBy) {
        log.info("Updating order status: orderNumber={}, status={}, notes={}, updatedBy={}", orderNumber, status, notes, updatedBy);

        OrderModel order = getOrderModel(orderNumber);

        order.setOrderStatus(status);
        order.setUpdatedDate(LocalDateTime.now());
        order.setTimmimgs(notes);
        long minutes = Long.parseLong(notes.trim());
        scheduleTimeCompletion(order, minutes);
        switch (status) {
            case PREPARING -> handleOrderConfirmed(order);
            case REJECTED -> handleOrderRejected(order, notes, updatedBy);
            case DELIVERY_REJECTED -> handleDeliveryReject(order);
            case RETURNED -> handleReturnDelivery(order);
            case DELIVERED -> handleOrderDeliver(order);
            default -> log.info("No additional actions defined for status: {}", status);
        }

        OrderModel savedOrder = orderRepository.save(order);
        notifyRestaurantOrderStatusChange(savedOrder.getOrderNumber(), status);

        return populateOrderDTO(savedOrder);
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Transactional
    private void scheduleTimeCompletion(OrderModel order, long minutes) {
        scheduler.schedule(() -> {
            try {
                OrderModel latest = orderRepository.findById(order.getId()).orElse(null);
                if (latest != null && !"COMPLETED".equalsIgnoreCase(latest.getTimmimgs())) {
                    latest.setTimmimgs("COMPLETED");
                    orderRepository.save(latest);
                    log.info("Order {} automatically marked as COMPLETED after {} minutes",
                            latest.getOrderNumber(), minutes);
                }
            } catch (Exception e) {
                log.error("Error auto-completing order {}", order.getOrderNumber(), e);
            }
        }, minutes, TimeUnit.MINUTES);
    }


    private void handleReturnDelivery(OrderModel order) {
        postDeliveryAction(order, DeliveryStatus.RETURNED);
    }

    private void handleDeliveryReject(OrderModel order) {
        postDeliveryAction(order, DeliveryStatus.DELIVERY_REJECTED);
    }

    private void handleOrderDeliver(OrderModel order) {
        sendOrderDeliverNotifications(getOrderDTO(order), order.getUserId());
        postDeliveryAction(order, DeliveryStatus.DELIVERED);
    }

    private void postDeliveryAction(OrderModel order, DeliveryStatus status) {
        boolean isAvailable = Boolean.TRUE.equals(handleAvaileDelivery(order));
        if (isAvailable) {
            assignAvailablePartnerToPendingOrder();
        }
        order.setDeliveryStatus(status);
    }


    private void assignAvailablePartnerToPendingOrder() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(DEFAULT_SORT_FIELD).ascending());

        //get pending orders and get 1st one
        Page<OrderDTO> orderDTOPage = getOrdersByDeliveryStatus(DeliveryStatus.PENDING, pageable);

        if (!orderDTOPage.isEmpty()) {
            OrderDTO orderDTO = orderDTOPage.getContent().get(0);
            OrderModel order = getOrderModel(orderDTO.getOrderNumber());

            attemptDeliveryAssignment(order);

            log.info("Assigned pending order {} to delivery partner {}", order.getOrderNumber());
        } else {
            log.info(" No pending orders available to assign for delivery partner {}");
        }
    }

    @Transactional
    private Boolean handleAvaileDelivery(OrderModel order) {
        String deliveryPartnerId = order.getDeliveryPartnerId();

        if (deliveryPartnerId == null) {
            log.warn("No delivery partner assigned to order {}, skipping availability update.", order.getOrderNumber());
            return false;
        }

        try {
            deliveryClient.updateAvailability(deliveryPartnerId, true);
            log.info("Marked delivery partner {} as available after order delivery.", deliveryPartnerId);
            return true;
        } catch (Exception e) {
            log.error("Failed to update availability for delivery partner {}: {}", deliveryPartnerId, e.getMessage(), e);
            return false;
        }
    }


    private OrderSummaryDTO buildOrderSummary(OrderModel order) {
        OrderSummaryDTO dto = new OrderSummaryDTO();
        dto.setOrderId(order.getId());
        dto.setBusinessName(order.getBusinessName());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setUserId(order.getUserId());
        dto.setBusinessId(order.getBusinessId());
        dto.setShippingAddressId(order.getShippingAddressId());
        dto.setTotalAmount(order.getTotalAmount());
        return dto;
    }

    private void handleOrderConfirmed(OrderModel order) {
        attemptDeliveryAssignment(order);
    }

    private void attemptDeliveryAssignment(OrderModel order) {
        try {
            String partnerId = assignDeliveryPartner(order);
            if (partnerId != null) {
                order.setDeliveryPartnerId(partnerId);
                order.setDeliveryStatus(DeliveryStatus.ASSIGNED);
            } else {
                order.setDeliveryStatus(DeliveryStatus.PENDING);
            }
        } catch (Exception e) {
            log.warn("Failed to assign delivery partner for order [{}]: {}", order.getOrderNumber(), e.getMessage());
            order.setDeliveryStatus(DeliveryStatus.PENDING);
        }
    }


    @Transactional
    public String assignDeliveryPartner(OrderModel order) {
        try {
            OrderSummaryDTO summary = buildOrderSummary(order);
            ResponseEntity<String> response = deliveryClient.assignDelivery(summary);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully triggered delivery creation for order {}", order.getOrderNumber());

                // Assuming delivery partner ID is returned in the response body as a String
                return (response.getBody());
            } else {
                log.error("Delivery service responded with status: {}", response.getStatusCode());
                throw new HltCustomerException(ErrorCode.DELIVERY_ASSIGNMENT_FAILED, "Delivery service failed");
            }
        } catch (Exception e) {
            log.error("Delivery creation failed for order {}: {}", order.getOrderNumber(), e.getMessage(), e);
            throw new HltCustomerException(ErrorCode.DELIVERY_ASSIGNMENT_FAILED, e.getMessage());
        }
    }

    private void handleOrderRejected(OrderModel order, String notes, String updatedBy) {
        createComplaintForRejectedOrder(order, notes, updatedBy);
        sendOrderRejectedByRestaurantNotification(order);

        String paymentId = order.getPaymentTransactionId();
        Long userId = order.getUserId();

        if (paymentId != null && userId != null) {
            try {
                PaymentTransactionResponse refundResponse = paymentService.refundPayment(paymentId, userId);
                log.info("Refund triggered for paymentId={}, response={}", paymentId, refundResponse);
            } catch (Exception e) {
                log.error("Refund failed for paymentId={}: {}", paymentId, e.getMessage(), e);
            }
        } else {
            log.warn("Skipping refund: paymentId or userId is null for orderNumber={}", order.getOrderNumber());
        }
    }

    private void notifyRestaurantOrderStatusChange(String orderNumber, OrderStatus status) {
        UpdateOrderStatusRequest updateRequest = new UpdateOrderStatusRequest();
        updateRequest.setOrderNumber(orderNumber);
        updateRequest.setOrderStatus(status.name());

        try {
            restaurantFeignClient.updateOrderStatus(updateRequest);
            log.info("Restaurant order status updated via Feign for orderNumber={}", orderNumber);
        } catch (Exception e) {
            log.error("Failed to update restaurant order status via Feign: {}", e.getMessage(), e);
        }
    }

    private void sendOrderRejectedByRestaurantNotification(OrderModel order) {
        try {
            Notification notification = Notification.buildNotification(
                    order.getUserId(),
                    Optional.ofNullable(order.getBusinessId()).orElse(0L),
                    NotificationEventType.ORDER_REJECTED_BY_RESTAURANT,
                    Collections.emptyMap()
            );
            notificationPublisher.sendNotifications(notification, NotificationEventType.ORDER_REJECTED_BY_RESTAURANT);
            log.info("Sent ORDER_REJECTED_BY_RESTAURANT notification for orderNumber={}", order.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to send ORDER_REJECTED_BY_RESTAURANT notification for orderNumber={}", order.getOrderNumber(), e);
        }
    }

    private void createComplaintForRejectedOrder(OrderModel order, String notes, String updatedBy) {
        ComplaintCreateRequest complaintRequest = new ComplaintCreateRequest();
        complaintRequest.setTitle("Order Rejected");
        complaintRequest.setDescription(notes != null ? notes : "Order was rejected.");
        complaintRequest.setComplaintType(ComplaintType.ORDER_REJECTED);
        complaintRequest.setOrderId(order.getOrderNumber());

        try {
            complaintRequest.setCreatedBy(Long.parseLong(updatedBy));
        } catch (NumberFormatException e) {
            log.warn("Invalid updatedBy value: '{}', skipping creator ID", updatedBy);
        }

        try {
            complaintService.createComplaint(complaintRequest);
            log.info("Complaint auto-created for rejected order: {}", order.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to create complaint for rejected order: {}", e.getMessage(), e);
        }
    }

    @Override
    public OrderDTO trackOrder(Long orderId, UserDetailsImpl userDetails) {
        log.info("Tracking order: {} for user: {}", orderId, userDetails.getId());

        OrderModel order = orderRepository.findById(orderId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.ORDER_NOT_FOUND));

        if (!Objects.equals(order.getUserId(), userDetails.getId())) {
            throw new HltCustomerException(ErrorCode.UNAUTHORIZED);
        }

        OrderDTO dto = new OrderDTO();
        orderPopulator.populate(order, dto);
        log.info("Order {} is currently in status: {}", orderId, order.getOrderStatus());
        return dto;
    }

    @Override
    public OrderDTO getOrderById(Long orderId) {
        log.info("Getting order by ID: {}", orderId);

        OrderModel order = orderRepository.findById(orderId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.ORDER_NOT_FOUND));

        return getOrderDTO(order);
    }

    @NotNull
    private OrderDTO getOrderDTO(OrderModel order) {
        OrderDTO dto = new OrderDTO();
        orderPopulator.populate(order, dto);
        return dto;
    }

    @Override
    public OrderDTO getOrderByOrderNumber(String orderNumber) {
        OrderModel order = getOrderModel(orderNumber);

        return getOrderDTO(order);
    }

    @Override
    public Page<OrderDTO> getOrdersByStatus(OrderStatus status, Long businessId, Pageable pageable) {
        Page<OrderModel> orders;

        if (businessId != null) {
            orders = orderRepository.findByBusinessIdAndOrderStatus(businessId, status, pageable);
        } else {
            orders = orderRepository.findByOrderStatus(status, pageable);
        }

        return orders.map(this::getOrderDTO);
    }



    private Page<OrderDTO> getOrdersByDeliveryStatus(DeliveryStatus status, Pageable pageable) {
        Page<OrderModel> orders = orderRepository.findByDeliveryStatus(status, pageable);
        return orders.map(this::getOrderDTO);
    }

    @Override
    public Page<OrderDTO> getOrdersByPartner(String partnerId, Pageable pageable) {
        Page<OrderModel> orders = orderRepository.findByDeliveryPartnerId(partnerId, pageable);
        return orders.map(this::getOrderDTO);
    }


    @Override
    public Page<OrderDTO> getOrdersByPartnerAndStatus(String deliveryPartnerId, DeliveryStatus status, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        Page<OrderModel> orders = orderRepository.findByPartnerAndStatusAndToday(
                deliveryPartnerId,
                status,
                startOfDay,
                endOfDay,
                pageable
        );
        return orders.map(this::getOrderDTO);
    }


    @Override
    public Page<OrderDTO> getOrdersByBusiness(Long businessId, int page, int size) {
        log.info("Getting paginated orders for businessId={}, page={}, size={}", businessId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT_FIELD).descending());
        Page<OrderModel> orderPage = orderRepository.findByBusinessId(businessId, pageable);

        List<OrderDTO> dtoList = orderPage.stream()
                .map(this::getOrderDTO)
                .toList();

        return new PageImpl<>(dtoList, pageable, orderPage.getTotalElements());
    }

    @Override
    public Page<OrderDTO> filterOrders(Long userId, Long businessId, String orderStatus,
                                       LocalDate fromDate, LocalDate toDate, String orderNumber,
                                       ReportFrequency frequency, Pageable pageable) {
        LocalDate now = LocalDate.now();

        if (frequency != null) {
            switch (frequency) {
                case MONTHLY -> fromDate = now.minusMonths(1);
                case QUARTERLY -> fromDate = now.minusMonths(3);
                case ANNUALLY -> fromDate = now.minusYears(1);
                default -> {
                    log.warn("Unknown frequency '{}', skipping date filter adjustment", frequency);
                    fromDate = null;
                }
            }
            toDate = now;
        }


        Specification<OrderModel> spec = OrderSpecification.filter(
                userId, businessId, orderStatus, fromDate, toDate, orderNumber
        );

        Page<OrderModel> orderPage = orderRepository.findAll(spec, pageable);

        List<OrderDTO> dtoList = orderPage.stream()
                .map(this::getOrderDTO).toList();

        return new PageImpl<>(dtoList, pageable, orderPage.getTotalElements());
    }

    @Override
    public boolean hasUserOrderedProductFromBusiness(Long userId, Long productId, Long businessId) {
        return orderRepository.existsProductOrder(
                userId, productId, businessId, OrderStatus.validForReview()
        );
    }

    @Override
    public boolean hasUserOrderedFromBusiness(Long userId, Long businessId) {
        return orderRepository.existsBusinessOrder(
                userId, businessId, OrderStatus.validForReview()
        );
    }

    @Override
    public boolean isDeliveryPartnerAssigned(String deliveryPartnerId) {
        return orderRepository.existsByDeliveryPartnerIdAndDeliveryStatus(deliveryPartnerId, DeliveryStatus.ASSIGNED);
    }

    @Override
    public boolean triggerOtpForOrder(String orderNumber, String type) {
        log.info("Triggering OTP for order: {}, type: {}", orderNumber, type);

        OrderDTO orderDTO = getOrderByOrderNumber(orderNumber);
        OtpType otpType = OtpType.from(type);

        String primaryContact = switch (otpType) {
            case RETURN -> getRestaurantMobileNumber(orderDTO);
            case DELIVERY -> getMobileNumber(orderDTO);
        };

        UserOTPDTO userOtpDto = new UserOTPDTO();
        userOtpDto.setPrimaryContact(primaryContact);
        userOtpDto.setOtpType(otpType.name());

        UserOTPDTO userOTPDTO = userMgmtClient.triggerSignupOtp(userOtpDto, false);
        sendOtpNotification(primaryContact, userOTPDTO.getOtp(), orderDTO.getBusinessId(), orderDTO.getUserId());

        return true;
    }


    @Transactional
    private void sendOtpNotification(String primaryContact, String otp, Long businessId, Long userId) {
        try {
            Map<String, String> params = Map.of(
                    "otp", otp,
                    "primaryContact", primaryContact
            );

            Notification otpNotification = Notification.buildNotification(
                    userId,
                    businessId,
                    NotificationEventType.OTP_TRIGGERED_USER,
                    params
            );
            notificationPublisher.sendNotifications(otpNotification, NotificationEventType.OTP_TRIGGERED_USER);
            log.info("Sent OTP_TRIGGERED_USER notification for userId={}, contact={}", userId, primaryContact);
        } catch (Exception e) {
            log.error("Failed to send OTP_TRIGGERED_USER notification for userId={}, contact={}", userId, primaryContact, e);
        }
    }


    @NotNull
    private static String getMobileNumber(OrderDTO orderDTO) {
        String primaryContact = orderDTO.getMobileNumber();
        if (primaryContact == null || primaryContact.isBlank()) {
            throw new HltCustomerException(ErrorCode.INVALID_ORDER_REQUEST, "Primary contact not found for order");
        }
        return primaryContact;
    }

    @NotNull
    private static String getRestaurantMobileNumber(OrderDTO orderDTO) {
        String primaryContact = orderDTO.getBusinessContactNumber();
        if (primaryContact == null || primaryContact.isBlank()) {
            throw new HltCustomerException(ErrorCode.INVALID_ORDER_REQUEST, "Primary contact not found for order");
        }
        return primaryContact;
    }


    @Override
    public boolean validateDeliveryOtp(String orderNumber, String otp) {
        log.info("Validating OTP for order: {}", orderNumber);

        OrderDTO orderDTO = getOrderByOrderNumber(orderNumber);

        String primaryContact = getMobileNumber(orderDTO);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setPrimaryContact(primaryContact);
        loginRequest.setOtp(otp);

        Boolean verified = userMgmtClient.verifyOtp(loginRequest);
        if (!Boolean.TRUE.equals(verified)) {
            throw new HltCustomerException(ErrorCode.OTP_EXPIRED, "Invalid or expired OTP");
        }
        return true;
    }

    private OrderModel getOrderModel(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.ORDER_NOT_FOUND));

    }

}