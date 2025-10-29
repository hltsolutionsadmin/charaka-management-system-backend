package com.juvarya.product.populator;

import com.juvarya.product.dto.OfferDTO;
import com.juvarya.product.model.OfferModel;
import org.springframework.stereotype.Component;

@Component
public class OfferPopulator {

    public OfferDTO toDto(OfferModel model) {
        if (model == null) return null;

        OfferDTO dto = new OfferDTO();
        dto.setId(model.getId());
        dto.setName(model.getName());
        dto.setOfferType(model.getOfferType());
        dto.setValue(model.getValue());
        dto.setMinOrderValue(model.getMinOrderValue());
        dto.setTargetType(model.getTargetType());
        dto.setCouponCode(model.getCouponCode());
        dto.setStartDate(model.getStartDate());
        dto.setEndDate(model.getEndDate());
        dto.setBusinessId(model.getBusinessId());
        dto.setActive(model.getActive());
        dto.setDescription(model.getDescription());
        dto.setProductIds(model.getProductIds());
        dto.setCategoryIds(model.getCategoryIds());
        return dto;
    }

    public OfferModel toModel(OfferDTO dto) {
        if (dto == null) return null;

        OfferModel model = new OfferModel();
        model.setId(dto.getId());
        model.setName(dto.getName());
        model.setOfferType(dto.getOfferType());
        model.setValue(dto.getValue());
        model.setMinOrderValue(dto.getMinOrderValue());
        model.setTargetType(dto.getTargetType());
        model.setCouponCode(dto.getCouponCode());
        model.setStartDate(dto.getStartDate());
        model.setEndDate(dto.getEndDate());
        model.setBusinessId(dto.getBusinessId());
        model.setActive(dto.getActive());
        model.setDescription(dto.getDescription());
        model.setProductIds(dto.getProductIds());
        model.setCategoryIds(dto.getCategoryIds());
        return model;
    }
}
