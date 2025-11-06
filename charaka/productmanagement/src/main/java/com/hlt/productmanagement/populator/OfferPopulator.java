package com.hlt.productmanagement.populator;

import com.hlt.productmanagement.dto.OfferDTO;
import com.hlt.productmanagement.model.OfferModel;
import com.hlt.utils.Populator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class OfferPopulator implements Populator<OfferModel, OfferDTO> {

    @Override
    public void populate(OfferModel source, OfferDTO target) {
        if (source == null || target == null) {
            return;
        }

        target.setId(source.getId());
        target.setName(source.getName());
        target.setOfferType(source.getOfferType());
        target.setValue(source.getValue());
        target.setMinOrderValue(source.getMinOrderValue());
        target.setTargetType(source.getTargetType());
        target.setCouponCode(source.getCouponCode());
        target.setStartDate(source.getStartDate());
        target.setEndDate(source.getEndDate());
        target.setBusinessId(source.getBusinessId());
        target.setActive(source.getActive());
        target.setDescription(source.getDescription());

        target.setProductIds(
                source.getProductIds() != null ? new ArrayList<>(source.getProductIds()) : new ArrayList<>()
        );
        target.setCategoryIds(
                source.getCategoryIds() != null ? new ArrayList<>(source.getCategoryIds()) : new ArrayList<>()
        );
    }

    public OfferDTO toDto(OfferModel model) {
        if (model == null) return null;
        OfferDTO dto = new OfferDTO();
        populate(model, dto);
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

        model.setProductIds(
                dto.getProductIds() != null ? new ArrayList<>(dto.getProductIds()) : new ArrayList<>()
        );
        model.setCategoryIds(
                dto.getCategoryIds() != null ? new ArrayList<>(dto.getCategoryIds()) : new ArrayList<>()
        );

        return model;
    }
}
