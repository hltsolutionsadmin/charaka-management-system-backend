package com.juvarya.delivery.service.impl;

import com.juvarya.auth.exception.handling.ErrorCode;
import com.juvarya.auth.exception.handling.JuvaryaCustomerException;
import com.juvarya.delivery.client.OrderClient;
import com.juvarya.delivery.dto.DeliveryPartnerDTO;
import com.juvarya.delivery.dto.OrderSummaryDTO;
import com.juvarya.delivery.dto.enums.OrderStatus;
import com.juvarya.delivery.firebase.dto.Notification;
import com.juvarya.delivery.firebase.dto.NotificationEventType;
import com.juvarya.delivery.firebase.listeners.NotificationPublisher;
import com.juvarya.delivery.model.DeliveryPartnerModel;
import com.juvarya.delivery.populator.DeliveryPartnerPopulator;
import com.juvarya.delivery.repository.DeliveryPartnerRepository;
import com.juvarya.delivery.service.DeliveryPartnerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryPartnerServiceImpl implements DeliveryPartnerService {

    private static final Random RANDOM = new Random();

    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final DeliveryPartnerPopulator populator;
    private final OrderClient orderClient;
    private final NotificationPublisher notificationPublisher;



    @Override
    public DeliveryPartnerDTO registerPartner(DeliveryPartnerDTO dto) {
        deliveryPartnerRepository.findByUserId(dto.getUserId()).ifPresent(existing -> {
            throw new JuvaryaCustomerException(ErrorCode.DELIVERY_PARTNER_ALREADY_REGISTERED);
        });

        DeliveryPartnerModel model = DeliveryPartnerModel.builder()
                .userId(dto.getUserId())
                .vehicleNumber(dto.getVehicleNumber())
                .deliveryPartnerId(generateDeliveryPartnerId(dto.getUserId()))
                .available(true)
                .active(true)
                .createdAt(currentTime())
                .updatedAt(currentTime())
                .build();

        return convertToDTO(deliveryPartnerRepository.save(model));
    }

    @Override
    public DeliveryPartnerDTO getPartnerById(Long id) {
        return convertToDTO(findByUserIdOrThrow(id));
    }

    @Override
    public Page<DeliveryPartnerDTO> getAllActivePartners(Pageable pageable) {
        return deliveryPartnerRepository.findByActiveTrue(pageable).map(this::convertToDTO);
    }

    @Override
    public Page<DeliveryPartnerDTO> getAllAvailablePartners(Pageable pageable) {
        return deliveryPartnerRepository.findByActiveTrueAndAvailableTrue(pageable).map(this::convertToDTO);
    }

    @Override
    public DeliveryPartnerDTO updatePartner(Long id, DeliveryPartnerDTO dto) {
        DeliveryPartnerModel model = findPartnerByIdOrThrow(id);
        model.setVehicleNumber(dto.getVehicleNumber());
        model.setAvailable(dto.getAvailable());
        model.setActive(dto.getActive());
        model.setUpdatedAt(currentTime());
        return convertToDTO(deliveryPartnerRepository.save(model));
    }

    @Override
    public void deactivatePartner(Long id) {
        DeliveryPartnerModel model = findPartnerByIdOrThrow(id);
        model.setActive(false);
        model.setAvailable(false);
        model.setUpdatedAt(currentTime());
        deliveryPartnerRepository.save(model);
    }

    @Override
    public DeliveryPartnerDTO updateAvailability(String deliveryPartnerId, Boolean available) {
        DeliveryPartnerModel model = findByPartnerIdOrThrow(deliveryPartnerId);
        model.setAvailable(available);
        model.setUpdatedAt(currentTime());
        return convertToDTO(deliveryPartnerRepository.save(model));
    }

    @Override
    public void updateOrderStatus(String orderNumber, Long deliveryPartnerId, OrderStatus status) {
        String note = switch (status) {
            case PICKED_UP -> "Accepted by delivery partner";
            case DELIVERED -> "Delivered by delivery partner";
            default -> "Updated by delivery partner";
        };

        try {
            orderClient.updateOrderStatus(orderNumber, status, note, deliveryPartnerId.toString());
        } catch (Exception e) {
            throw new JuvaryaCustomerException(ErrorCode.REMOTE_ORDER_UPDATE_FAILED);
        }
    }

    @Override
    public String assignNewDelivery(OrderSummaryDTO order) {
        List<DeliveryPartnerModel> partners = deliveryPartnerRepository
                .findByAvailableTrueOrderByLastAssignedTimeAsc();

        if (partners.isEmpty()) {
            throw new JuvaryaCustomerException(ErrorCode.NO_DELIVERY_PARTNER_AVAILABLE);
        }

        DeliveryPartnerModel selected = partners.get(0);
        selected.setAvailable(false);
        selected.setLastAssignedTime(currentTime());
        deliveryPartnerRepository.save(selected);

        sendPartnerAssignmentNotification(order, selected.getUserId());

        log.info("Assigned delivery partner [{}] to order [{}]", selected.getId(), order.getOrderNumber());
        return selected.getDeliveryPartnerId();
    }

    @Override
    public DeliveryPartnerDTO updateAvailabilityByUserId(Long userId, Boolean available) {
        DeliveryPartnerModel delivery = findByUserIdOrThrow(userId);

        // If marking as available, ensure not already assigned
        if (Boolean.TRUE.equals(available)) {
            Boolean deliveryPartnerAssigned = orderClient.isDeliveryPartnerAssigned(delivery.getDeliveryPartnerId());
            if (Boolean.TRUE.equals(deliveryPartnerAssigned)) {
                throw new JuvaryaCustomerException(
                        ErrorCode.NOT_ALLOWED,
                        "Cannot mark as available. Partner is currently assigned to an active order."
                );
            }
        }

        delivery.setActive(available);
        delivery.setUpdatedAt(currentTime());

        DeliveryPartnerModel saved = deliveryPartnerRepository.save(delivery);
        return convertToDTO(saved);
    }



    // ------------------------------ Utility Methods ------------------------------

    public String generateDeliveryPartnerId(Long userId) {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String userPart = String.valueOf(userId);
        String randomPart = getRandomAlphaNumeric(2);
        return "DP" + datePart + "-U" + userPart + randomPart;
    }

    private String getRandomAlphaNumeric(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private void sendPartnerAssignmentNotification(OrderSummaryDTO order, Long partnerUserId) {
        try {
            Map<String, String> params = Map.of(
                    "orderNumber", order.getOrderNumber(),
                    "restaurantName", order.getBusinessName(),
                    "totalAmount", order.getTotalAmount().toPlainString()
            );

            Notification notification = Notification.buildNotification(
                    partnerUserId,
                    order.getBusinessId(),
                    NotificationEventType.ORDER_ASSIGNED_PARTNER,
                    params
            );

            notificationPublisher.sendNotifications(notification, NotificationEventType.ORDER_ASSIGNED_PARTNER);
            log.info("Sent ORDER_ASSIGNED_PARTNER notification to userId={}, order={}", partnerUserId, order.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to send notification to userId={}, order={}", partnerUserId, order.getOrderNumber(), e);
        }
    }

    private DeliveryPartnerDTO convertToDTO(DeliveryPartnerModel model) {
        DeliveryPartnerDTO dto = new DeliveryPartnerDTO();
        populator.populate(model, dto);
        return dto;
    }

    private DeliveryPartnerModel findByPartnerIdOrThrow(String deliveryPartnerId) {
        return deliveryPartnerRepository.findByDeliveryPartnerId(deliveryPartnerId)
                .orElseThrow(() -> new JuvaryaCustomerException(ErrorCode.DELIVERY_PARTNER_NOT_FOUND));
    }

    private DeliveryPartnerModel findByUserIdOrThrow(Long userId) {
        return deliveryPartnerRepository.findByUserId(userId)
                .orElseThrow(() -> new JuvaryaCustomerException(ErrorCode.DELIVERY_PARTNER_NOT_FOUND));
    }

    private DeliveryPartnerModel findPartnerByIdOrThrow(Long id) {
        return deliveryPartnerRepository.findById(id)
                .orElseThrow(() -> new JuvaryaCustomerException(ErrorCode.DELIVERY_PARTNER_NOT_FOUND));
    }

    private static LocalDateTime currentTime() {
        return LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
    }
}
