package com.juvarya.product.populator;

import com.juvarya.product.dto.MediaDTO;
import com.juvarya.product.dto.ProductAttributeDTO;
import com.juvarya.product.dto.ProductDTO;
import com.juvarya.product.model.ProductAttributeModel;
import com.juvarya.product.model.ProductModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ProductPopulator {

    public void populate(ProductModel source, ProductDTO target) {
        if (source == null || target == null) {
            log.warn("Product source or target is null. Skipping population.");
            return;
        }

        populateBasicFields(source, target);
        populateMedia(source, target);
        populateAttributesAndAvailability(source.getAttributes(), target);
    }

    private void populateBasicFields(ProductModel source, ProductDTO target) {
        target.setId(source.getId());
        target.setName(source.getName());
        target.setShortCode(source.getShortCode());
        target.setIgnoreTax(source.isIgnoreTax());
        target.setDiscount(source.isDiscount());
        target.setDescription(source.getDescription());
        target.setAvailable(source.isAvailable());
        target.setBusinessId(source.getBusinessId());
        target.setShopifyProductId(source.getShopifyProductId());
        target.setShopifyVariantId(source.getShopifyVariantId());
        target.setCategoryId(source.getCategory() != null ? source.getCategory().getId() : null);
        target.setPrice(source.getPrice());
    }

    private void populateMedia(ProductModel source, ProductDTO target) {
        if (source.getMedia() != null && !source.getMedia().isEmpty()) {
            List<MediaDTO> mediaDTOs = source.getMedia().stream()
                    .map(media -> new MediaDTO(media.getMediaType(), media.getUrl()))
                    .collect(Collectors.toList());
            target.setMedia(mediaDTOs);
        }
    }

    private void populateAttributesAndAvailability(Set<ProductAttributeModel> attributes,
                                                   ProductDTO target) {
        if (attributes == null || attributes.isEmpty()) {
            target.setStatus("Unavailable");
            return;
        }

        List<ProductAttributeDTO> attributeDTOs = attributes.stream()
                .map(attr -> new ProductAttributeDTO(attr.getId(), attr.getAttributeName(), attr.getAttributeValue()))
                .collect(Collectors.toList());

        target.setAttributes(attributeDTOs);

        String startTimeStr = null;
        String endTimeStr = null;

        for (ProductAttributeDTO attr : attributeDTOs) {
            if ("startTime".equalsIgnoreCase(attr.getAttributeName())) {
                startTimeStr = attr.getAttributeValue();
            } else if ("endTime".equalsIgnoreCase(attr.getAttributeName())) {
                endTimeStr = attr.getAttributeValue();
            }
        }

        if (startTimeStr != null && endTimeStr != null) {
            try {
                LocalTime now = LocalTime.now();
                LocalTime start = LocalTime.parse(startTimeStr);
                LocalTime end = LocalTime.parse(endTimeStr);

                boolean isAvailable = start.isBefore(end)
                        ? now.isAfter(start) && now.isBefore(end)
                        : now.isAfter(start) || now.isBefore(end);

                target.setStatus(isAvailable ? "Available" : "Unavailable");
            } catch (Exception e) {
                log.error("Error parsing time attributes: startTime={}, endTime={}", startTimeStr, endTimeStr);
                target.setStatus("Unavailable");
            }
        } else {
            target.setStatus("Unavailable");
        }
    }

}
