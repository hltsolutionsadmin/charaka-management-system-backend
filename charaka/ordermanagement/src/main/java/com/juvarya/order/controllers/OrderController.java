package com.juvarya.order.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.commonservice.dto.StandardResponse;
import com.hlt.commonservice.dto.UserDTO;
import com.hlt.commonservice.user.UserDetailsImpl;
import com.juvarya.order.client.UserMgmtClient;
import com.juvarya.order.dto.OrderDTO;
import com.juvarya.order.dto.enums.DeliveryStatus;
import com.juvarya.order.dto.enums.ReportFrequency;
import com.juvarya.order.dto.request.OrderRequest;
import com.juvarya.order.dto.response.ApiResponse;
import com.juvarya.order.service.OrderService;
import com.juvarya.order.dto.enums.OrderStatus;
import com.hlt.utils.SecurityUtils;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static com.juvarya.order.dto.enums.OrderStatus.DELIVERED;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    private UserMgmtClient userMgmtClient;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody OrderRequest request) throws JsonProcessingException {

        log.info("Creating order for user: {}, transactionId: {}", user.getUsername(), request.getPaymentTransactionId());
        OrderDTO order = orderService.createOrder(user, request.getPaymentTransactionId());
        return ResponseEntity.ok(ApiResponse.single("Order created successfully", order));
    }


    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getOrderHistory(@AuthenticationPrincipal UserDetailsImpl user, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "DESC") String direction, @RequestParam(required = false) String query) {

        log.info("Fetching order history for user: {}, page: {}, size: {}, sort: {} {}, query: {}", user.getUsername(), page, size, sortBy, direction, query);

        Page<OrderDTO> orders = orderService.getOrderHistory(user, page, size, sortBy, direction);

        if (StringUtils.isNotBlank(query)) {
            orders = filterOrders(orders, query);
        }

        return ResponseEntity.ok(ApiResponse.page("Order history fetched", orders));
    }

    @PostMapping("/reorder")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<StandardResponse<OrderDTO>> reorder(
            @RequestBody @Valid OrderRequest request,
            @AuthenticationPrincipal UserDetailsImpl user) {
        if (request.getPreviousOrderId() == null) {
            throw new HltCustomerException(ErrorCode.INVALID_ORDER_REQUEST);
        }
        if (request.getPaymentTransactionId() == null || request.getPaymentTransactionId().isBlank()) {
            throw new HltCustomerException(ErrorCode.PAYMENT_TRANSACTION_ID_REQUIRED);
        }
        OrderDTO dto = orderService.reorder(request.getPaymentTransactionId(), request.getPreviousOrderId(), request.getUpdates(), user);
        return ResponseEntity.ok(StandardResponse.single("Reorder successful", dto));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(@PathVariable Long orderId) {
        log.info("Fetching order details for orderId: {}", orderId);
        OrderDTO order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(ApiResponse.single("Order retrieved", order));
    }


    @PostMapping("/trigger-delivery-otp")
    public ResponseEntity<StandardResponse<Boolean>> triggerDeliveryOtp(
            @RequestParam String orderNumber,
            @RequestParam String type) {

        boolean success = orderService.triggerOtpForOrder(orderNumber, type);
        return ResponseEntity.ok(StandardResponse.single("OTP triggered successfully", success));
    }

    @PostMapping("/validate-delivery-otp")
    public ResponseEntity<StandardResponse<Boolean>> validateDeliveryOtp(
            @RequestParam String orderNumber,
            @RequestParam String otp
    ) {
        boolean isValid = orderService.validateDeliveryOtp(orderNumber, otp);
        return ResponseEntity.ok(StandardResponse.single("OTP validation result", isValid));
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getOrdersByRestaurant(@PathVariable Long businessId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

        log.info("Fetching paginated orders for businessId: {}, page: {}, size: {}", businessId, page, size);
        Page<OrderDTO> pagedOrders = orderService.getOrdersByBusiness(businessId, page, size);
        return ResponseEntity.ok(ApiResponse.page("Restaurant orders fetched", pagedOrders));
    }

    @GetMapping("/status")
    public ResponseEntity<StandardResponse<Page<OrderDTO>>> getOrdersByStatus(
            @RequestParam OrderStatus status,
            @RequestParam(required = false) Long businessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());

        Page<OrderDTO> orders = orderService.getOrdersByStatus(status, businessId, pageable);

        return ResponseEntity.ok(StandardResponse.page("Fetched orders with status: " + status, orders));
    }


    @GetMapping("/by-partner")
    public ResponseEntity<StandardResponse<Page<OrderDTO>>> getOrdersByPartnerAndStatus(
            @RequestParam String partnerId,
            @RequestParam(required = false) DeliveryStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<OrderDTO> orders;

        if (status != null) {
            orders = orderService.getOrdersByPartnerAndStatus(partnerId, status, pageable);
            return ResponseEntity.ok(StandardResponse.page("Fetched orders for partner ID with status: " + status, orders));
        } else {
            orders = orderService.getOrdersByPartner(partnerId, pageable);
            return ResponseEntity.ok(StandardResponse.page("Fetched recent orders for partner ID: " + partnerId, orders));
        }
    }


    @PostMapping("/status/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(@PathVariable String orderNumber, @RequestParam OrderStatus status, @RequestParam(required = false) String notes, @RequestParam String updatedBy) {

        UserDetailsImpl loggedInUser = SecurityUtils.getCurrentUserDetails();
        log.info("Updating status for orderNumber: {}, to: {}, by user: {}", orderNumber, status, updatedBy);

        // Validate access for delivery-related statuses
        if (isDeliveryStatus(status) && !hasRole(loggedInUser, "ROLE_DELIVERY_PARTNER")) {
            throw new HltCustomerException(ErrorCode.INVALID_ORDER_REQUEST,
                    "Only delivery partners can update to delivery-related statuses.");
        }
        OrderDTO order = orderService.updateOrderStatus(orderNumber, status, notes, updatedBy);
        return ResponseEntity.ok(ApiResponse.single("Order status updated", order));
    }

    private boolean isDeliveryStatus(OrderStatus status) {
        return status == OrderStatus.DELIVERED ||
                status==OrderStatus.RETURNED ||
                status == OrderStatus.DELIVERY_REJECTED ||
                status == OrderStatus.PICKED_UP;
    }

    private boolean hasRole(UserDetailsImpl user, String role) {
        return user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }


    @GetMapping("orderNumber/{orderNumber}")
    public ResponseEntity<StandardResponse<OrderDTO>> getOrderByOrderNumber(@PathVariable String orderNumber,
                                                                            @RequestParam String password) {
        validateUserPassword(password);
        OrderDTO dto = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(StandardResponse.single("Order fetched successfully", dto));
    }


    @GetMapping("/{orderId}/track")
    public ResponseEntity<ApiResponse<OrderDTO>> trackOrder(@PathVariable Long orderId, @AuthenticationPrincipal UserDetailsImpl user) {
        log.info("Tracking orderId: {} for user: {}", orderId, user.getUsername());
        OrderDTO order = orderService.trackOrder(orderId, user);
        return ResponseEntity.ok(ApiResponse.single("Tracking details fetched", order));
    }

    private Page<OrderDTO> filterOrders(Page<OrderDTO> orders, String query) {
        String lowerCaseQuery = query.toLowerCase();

        List<OrderDTO> filteredOrders = orders.getContent().stream().filter(order -> (order.getOrderNumber() != null && order.getOrderNumber().toLowerCase().contains(lowerCaseQuery)) ||

                (order.getBusinessName() != null && order.getBusinessName().toLowerCase().contains(lowerCaseQuery)) ||

                isMatchingStatus(order, lowerCaseQuery) ||

                (order.getOrderItems() != null && order.getOrderItems().stream().anyMatch(item -> item.getProductName() != null && item.getProductName().toLowerCase().contains(lowerCaseQuery)))).collect(Collectors.toList());

        if (filteredOrders.isEmpty()) {
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(0, filteredOrders.size());
        return new PageImpl<>(filteredOrders, pageable, filteredOrders.size());
    }


    private boolean isMatchingStatus(OrderDTO order, String query) {
        try {
            OrderStatus status = OrderStatus.valueOf(query.toUpperCase());
            return order.getOrderStatus() == status;
        } catch (IllegalArgumentException e) {
            log.debug("Invalid order status in query: {}", query);
            return false;
        }
    }

    @GetMapping("/productOrder")
    public Boolean hasUserOrderedProductFromBusiness(@RequestParam Long userId,
                                                     @RequestParam Long productId,
                                                     @RequestParam Long businessId) {
        return orderService.hasUserOrderedProductFromBusiness(userId, productId, businessId);
    }

    @GetMapping("/businessOrder")
    public Boolean hasUserOrderedFromBusiness(@RequestParam Long userId,
                                              @RequestParam Long businessId) {
        return orderService.hasUserOrderedFromBusiness(userId, businessId);
    }

    @GetMapping("/delivery-partner-status/{deliveryPartnerId}")
    public ResponseEntity<Boolean> isDeliveryPartnerAssigned(@PathVariable("deliveryPartnerId") String deliveryPartnerId) {
        boolean assigned = orderService.isDeliveryPartnerAssigned(deliveryPartnerId);
        return ResponseEntity.ok(assigned);
    }

    private void validateUserPassword(String rawPassword) {
        UserDetailsImpl userDetails = SecurityUtils.getCurrentUserDetails();
        UserDTO userDTO = null;
        try {
            userDTO = userMgmtClient.getUserById(userDetails.getId());
        } catch (Exception e) {
            throw new HltCustomerException(ErrorCode.USER_NOT_FOUND, "password not found");
        }

        String encryptedPassword = userDTO.getPassword();
        if (encryptedPassword == null || !passwordEncoder.matches(rawPassword, encryptedPassword)) {
            throw new HltCustomerException(ErrorCode.INVALID_PASSWORD, "Invalid password for access");
        }
    }


    @GetMapping("/user/filter")
    public StandardResponse<Page<OrderDTO>> filterOrders(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long businessId,
            @RequestParam(required = false) String orderStatus,
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) ReportFrequency frequency,
            Pageable pageable
    ) {
        Page<OrderDTO> page = orderService.filterOrders(
                userId, businessId, orderStatus, fromDate, toDate, orderNumber, frequency, pageable
        );
        return StandardResponse.page("Orders fetched successfully", page);
    }

    //  Export Orders Excel
    @GetMapping("/user/filter/excel")
    public void downloadFilteredOrdersExcel(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long restaurantId,
            @RequestParam(required = false) String orderStatus,
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) ReportFrequency frequency,
            HttpServletResponse response
    ) throws IOException {

        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<OrderDTO> page = orderService.filterOrders(userId, restaurantId, orderStatus, fromDate, toDate, orderNumber, frequency, pageable);
        List<OrderDTO> orders = page.getContent();

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Filtered Orders");

            String[] headers = {
                    "Order Number", "User ID", "Business ID", "Business Name",
                    "Order Status", "Total Amount", "Payment Txn ID", "Shipping Address ID",
                    "Created Date", "Updated Date", "Order Items"
            };

            CellStyle headerStyle = createHeaderCellStyle(workbook);
            CellStyle dataStyle = createDataCellStyle(workbook);

            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

            for (OrderDTO dto : orders) {
                Row row = sheet.createRow(rowNum++);
                createStyledCell(row, 0, dto.getOrderNumber(), dataStyle);
                createStyledCell(row, 1, String.valueOf(dto.getUserId()), dataStyle);
                createStyledCell(row, 2, String.valueOf(dto.getBusinessId()), dataStyle);
                createStyledCell(row, 3, dto.getBusinessName(), dataStyle);
                createStyledCell(row, 4, dto.getOrderStatus() != null ? dto.getOrderStatus().name() : "", dataStyle);
                createStyledCell(row, 5, dto.getTotalAmount() != null ? dto.getTotalAmount().toString() : "", dataStyle);
                createStyledCell(row, 6, dto.getPaymentTransactionId(), dataStyle);
                createStyledCell(row, 7, dto.getShippingAddressId() != null ? dto.getShippingAddressId().toString() : "", dataStyle);
                createStyledCell(row, 8, dto.getCreatedDate() != null ? dto.getCreatedDate().format(formatter) : "", dataStyle);
                createStyledCell(row, 9, dto.getUpdatedDate() != null ? dto.getUpdatedDate().format(formatter) : "", dataStyle);

                String items = dto.getOrderItems() != null
                        ? dto.getOrderItems().stream()
                        .map(i -> i.getProductName() + " (" + i.getQuantity() + ")")
                        .collect(Collectors.joining(", "))
                        : "";
                createStyledCell(row, 10, items, dataStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=filtered_orders.xlsx");
            workbook.write(response.getOutputStream());
        }
    }


    //  Helpers for Styling
    private CellStyle createHeaderCellStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        addBorders(style);
        return style;
    }

    private CellStyle createDataCellStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        addBorders(style);
        return style;
    }

    private void addBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private void createStyledCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

}