package com.juvarya.order.populator;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.commonservice.dto.B2BUnitDTO;
import com.hlt.commonservice.dto.UserDTO;
import com.juvarya.order.client.UserMgmtClient;
import com.juvarya.order.dto.AddressDTO;
import com.juvarya.order.dto.OrderDTO;
import com.juvarya.order.dto.OrderItemDTO;
import com.juvarya.order.entity.OrderModel;
import com.hlt.utils.Populator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class OrderPopulator implements Populator<OrderModel, OrderDTO> {

    @Autowired
    private OrderItemPopulator orderItemPopulator;

    @Autowired
    private UserMgmtClient userMgmtClient;

    @Override
    public void populate(OrderModel source, OrderDTO target) {
        if (source == null || target == null) return;

        // Populate customer
        UserDTO userDTO = fetchUser(source.getUserId());
        target.setUsername(userDTO.getFullName());
        target.setMobileNumber(userDTO.getPrimaryContact());

        // Populate customer shipping address
        AddressDTO userAddress = fetchAddressById(source.getShippingAddressId());
        target.setUserAddress(userAddress);

        // Populate business address
        AddressDTO businessAddress = fetchAddressByBusinessId(source.getBusinessId());
        target.setBusinessAddress(businessAddress);

        // Populate delivery partner info (parsed from deliveryPartnerId)
        if (source.getDeliveryPartnerId() != null) {
            try {
                Long deliveryPartnerUserId = extractUserIdFromPartnerId(source.getDeliveryPartnerId());
                UserDTO deliveryPartner = fetchUser(deliveryPartnerUserId);
                target.setDeliveryPartnerName(deliveryPartner.getFullName());
                target.setDeliveryPartnerMobileNumber(deliveryPartner.getPrimaryContact());
            } catch (Exception e) {
                target.setDeliveryPartnerName("Not Assigned");
                target.setDeliveryPartnerMobileNumber("N/A");
            }
        }

        // Direct mappings
        target.setId(source.getId());
        target.setOrderNumber(source.getOrderNumber());
        target.setUserId(source.getUserId());
        target.setDeliveryPartnerId(source.getDeliveryPartnerId());
        target.setBusinessId(source.getBusinessId());
        target.setBusinessName(source.getBusinessName());
        try {
            B2BUnitDTO b2BUnitDTO = fetchBusiness(source.getBusinessId());
            target.setBusinessContactNumber(b2BUnitDTO.getBusinessContactNumber());
        } catch (Exception e) {
            log.info("Failed to fetch business contact number for businessId: {}", source.getBusinessId(), e);
            target.setBusinessContactNumber("N/A");
        }

        target.setShippingAddressId(source.getShippingAddressId());
        target.setNotes(source.getNotes());
        target.setTotalAmount(source.getTotalAmount());
        target.setTotalTaxAmount(source.getTotalTaxAmount());
        target.setTaxInclusive(source.getTaxInclusive());
        target.setPaymentStatus(source.getPaymentStatus());
        target.setOrderStatus(source.getOrderStatus());
        target.setPaymentTransactionId(source.getPaymentTransactionId());
        target.setCreatedDate(source.getCreatedDate());
        target.setTimmimgs(source.getTimmimgs());
        target.setUpdatedDate(source.getUpdatedDate());
        target.setDeliveryStatus(source.getDeliveryStatus());

        // Populate order items
        List<OrderItemDTO> itemDTOs = Optional.ofNullable(source.getOrderItems())
                .orElse(Collections.emptyList())
                .stream()
                .map(item -> {
                    OrderItemDTO dto = new OrderItemDTO();
                    orderItemPopulator.populate(item, dto);
                    return dto;
                })
                .collect(Collectors.toList());

        target.setOrderItems(itemDTOs);
    }

    private UserDTO fetchUser(Long userId) {
        try {
            return userMgmtClient.getUserById(userId);
        } catch (Exception e) {
            throw new HltCustomerException(ErrorCode.USER_NOT_FOUND, "Unable to fetch user details");
        }
    }

    private AddressDTO fetchAddressById(Long addressId) {
        try {
            return userMgmtClient.getAddressById(addressId);
        } catch (Exception e) {
            return getFallbackAddress();
        }
    }

    private AddressDTO fetchAddressByBusinessId(Long businessId) {
        try {
            return userMgmtClient.getAddressByBusinessID(businessId);
        } catch (Exception e) {
            return getFallbackAddress();
        }
    }

    private AddressDTO getFallbackAddress() {
        AddressDTO fallback = new AddressDTO();
        fallback.setAddressLine1("Address not available");
        fallback.setCity("N/A");
        fallback.setState("N/A");
        fallback.setCountry("N/A");
        fallback.setPostalCode("N/A");
        fallback.setIsDefault(false);
        return fallback;
    }

    private Long extractUserIdFromPartnerId(String partnerId) {
        try {
            // Expects format: DP<yyMMdd>-U<userId><2-char>
            String suffix = partnerId.split("-U")[1]; // e.g., "10X9"
            String userIdPart = suffix.substring(0, suffix.length() - 2); // remove last 2 random chars
            return Long.parseLong(userIdPart);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Partner ID format: " + partnerId);
        }
    }

    private B2BUnitDTO fetchBusiness(Long businessId) {
        try {
            return userMgmtClient.getBusinessById(businessId);
        } catch (Exception e) {
            throw new HltCustomerException(ErrorCode.USER_NOT_FOUND, "Unable to fetch business contact deatails please update ");
        }
    }
}
