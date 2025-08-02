package com.juvarya.delivery.controllers;

import com.juvarya.commonservice.dto.StandardResponse;
import com.juvarya.commonservice.user.UserDetailsImpl;
import com.juvarya.delivery.dto.DeliveryPartnerDTO;
import com.juvarya.delivery.dto.OrderSummaryDTO;
import com.juvarya.delivery.dto.enums.OrderStatus;
import com.juvarya.delivery.service.DeliveryPartnerService;
import com.juvarya.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/partners")
@RequiredArgsConstructor
public class DeliveryPartnerController {

    private final DeliveryPartnerService deliveryPartnerService;

    /**
     * Registers a new delivery partner.
     */

    // getAllAssignedOrders  -from ordermgmgt service
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_USER_ADMIN', 'ROLE_DELIVERY_PARTNER')")
    public ResponseEntity<StandardResponse<DeliveryPartnerDTO>> registerPartner(@RequestBody DeliveryPartnerDTO dto) {
        UserDetailsImpl user = SecurityUtils.getCurrentUserDetails();
        dto.setUserId(user.getId());
        DeliveryPartnerDTO registered = deliveryPartnerService.registerPartner(dto);
        return ResponseEntity.ok(StandardResponse.single("Delivery Partner Registered", registered));
    }

    /**
     * Get a delivery partner by ID
     */
    @GetMapping("/getPartner")
    public ResponseEntity<StandardResponse<DeliveryPartnerDTO>> getPartnerById() {
        UserDetailsImpl user = SecurityUtils.getCurrentUserDetails();
        DeliveryPartnerDTO partner = deliveryPartnerService.getPartnerById(user.getId());
        return ResponseEntity.ok(StandardResponse.single("Delivery Partner Found", partner));
    }

    /**
     * Get all active delivery partners.(offLine or Online)
     */
    @GetMapping("/active/paged")
    public ResponseEntity<StandardResponse<Page<DeliveryPartnerDTO>>> getAllActivePartnersPaged(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Page<DeliveryPartnerDTO> response = deliveryPartnerService.getAllActivePartners(pageable);
        return ResponseEntity.ok(StandardResponse.page("Active delivery partners", response));
    }

    /**
     * Update delivery partner details by ID.
     */
    @PutMapping("/{id}")
    public ResponseEntity<StandardResponse<DeliveryPartnerDTO>> updatePartner(@PathVariable Long id, @RequestBody DeliveryPartnerDTO dto) {
        DeliveryPartnerDTO updated = deliveryPartnerService.updatePartner(id, dto);
        return ResponseEntity.ok(StandardResponse.single("Delivery Partner Updated", updated));
    }

    /**
     * Soft delete (deactivate) delivery partner by ID.
     */

    @DeleteMapping("/{id}")
    public ResponseEntity<StandardResponse<String>> deactivatePartner(@PathVariable Long id) {
        deliveryPartnerService.deactivatePartner(id);
        return ResponseEntity.ok(StandardResponse.message("Delivery Partner Deactivated"));
    }

    /**
     * Update availability status of the logged-in delivery partner.
     */

    @PutMapping("/availability/{deliveryPartnerId}")
    public ResponseEntity<StandardResponse<DeliveryPartnerDTO>> updateAvailability(
            @PathVariable String deliveryPartnerId,
            @RequestParam Boolean available) {
        DeliveryPartnerDTO updatedPartner = deliveryPartnerService.updateAvailability(deliveryPartnerId, available);
        String message = available
                ? "Delivery partner marked as available successfully."
                : "Delivery partner marked as unavailable successfully.";
        return ResponseEntity.ok(StandardResponse.single(message, updatedPartner));
    }

    @PutMapping("/activeByToken")
    public ResponseEntity<StandardResponse<DeliveryPartnerDTO>> updateActibeByToken(
            @RequestParam Boolean available
    ) {
        UserDetailsImpl userDetails = SecurityUtils.getCurrentUserDetails();
        DeliveryPartnerDTO updated = deliveryPartnerService.updateAvailabilityByUserId(userDetails.getId(), available);
        String message = available ? "Marked as available." : "Marked as unavailable.";
        return ResponseEntity.ok(StandardResponse.single(message, updated));
    }

    @GetMapping("/available")
    public ResponseEntity<StandardResponse<Page<DeliveryPartnerDTO>>> getAvailablePartners(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<DeliveryPartnerDTO> availablePartners = deliveryPartnerService.getAllAvailablePartners(pageable);
        return ResponseEntity.ok(StandardResponse.page("Available delivery partners", availablePartners));
    }

    @PostMapping("/orders/status/{orderNumber}")
    public ResponseEntity<StandardResponse<String>> updateOrderStatus(@AuthenticationPrincipal UserDetailsImpl user, @PathVariable String orderNumber, @RequestParam OrderStatus status) {

        Long deliveryPartnerId = user.getId();
        deliveryPartnerService.updateOrderStatus(orderNumber, deliveryPartnerId, status);
        return ResponseEntity.ok(StandardResponse.message("Order status updated to " + status.name()));
    }


    @PostMapping("/assign-partner")
    public ResponseEntity<String> assignDeliveryPartner(@RequestBody OrderSummaryDTO order) {
        log.info("Assigning delivery partner for order: {}", order.getOrderNumber());

        String assignedPartnerInfo = deliveryPartnerService.assignNewDelivery(order);

        return ResponseEntity.ok(assignedPartnerInfo);
    }

}
