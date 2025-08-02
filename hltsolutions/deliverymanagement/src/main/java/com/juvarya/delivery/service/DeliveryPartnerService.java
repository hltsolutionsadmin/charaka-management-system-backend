package com.juvarya.delivery.service;

import com.juvarya.delivery.dto.DeliveryPartnerDTO;

import com.juvarya.delivery.dto.OrderSummaryDTO;
import com.juvarya.delivery.dto.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DeliveryPartnerService {

    DeliveryPartnerDTO registerPartner(DeliveryPartnerDTO dto);

    DeliveryPartnerDTO getPartnerById(Long id);

    Page<DeliveryPartnerDTO> getAllActivePartners(Pageable pageable);


    DeliveryPartnerDTO updatePartner(Long id, DeliveryPartnerDTO dto);

    void deactivatePartner(Long id);

    DeliveryPartnerDTO updateAvailability(String deliveryPartnerId, Boolean available);

    Page<DeliveryPartnerDTO> getAllAvailablePartners(Pageable pageable);

    void updateOrderStatus(String orderNumber, Long deliveryPartnerId, OrderStatus status);

     String assignNewDelivery(OrderSummaryDTO order);

    DeliveryPartnerDTO updateAvailabilityByUserId(Long id, Boolean available);


    // getAllAssignedOrders  - from ordermgmgt service
}


