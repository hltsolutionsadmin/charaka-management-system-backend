package com.juvarya.delivery.populator;

import com.juvarya.delivery.dto.DeliveryPartnerDTO;
import com.juvarya.delivery.model.DeliveryPartnerModel;
import com.juvarya.utils.Populator;
import org.springframework.stereotype.Component;

@Component
public class DeliveryPartnerPopulator implements Populator<DeliveryPartnerModel, DeliveryPartnerDTO> {

    @Override
    public void populate(DeliveryPartnerModel source, DeliveryPartnerDTO target) {
        target.setId(source.getId());
        target.setUserId(source.getUserId());
        target.setDeliveryPartnerId(source.getDeliveryPartnerId());
        target.setVehicleNumber(source.getVehicleNumber());
        target.setActive(source.getActive());
        target.setAvailable(source.getAvailable());
        target.setLastAssignedTime(source.getLastAssignedTime());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
    }


}

