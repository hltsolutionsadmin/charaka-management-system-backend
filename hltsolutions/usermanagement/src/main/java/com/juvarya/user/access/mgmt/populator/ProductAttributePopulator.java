package com.juvarya.user.access.mgmt.populator;

import com.juvarya.user.access.mgmt.dto.response.ProductAttributeResponse;
import com.juvarya.user.access.mgmt.model.ProductAttributeModel;
import com.juvarya.utils.Populator;
import org.springframework.stereotype.Component;

@Component
public class ProductAttributePopulator implements Populator<ProductAttributeModel, ProductAttributeResponse> {

    @Override
    public void populate(ProductAttributeModel source, ProductAttributeResponse target) {
        if (source == null || target == null) {
            return;
        }
        target.setId(source.getId());
        target.setAttributeName(source.getAttributeName());
        target.setAttributeValue(source.getAttributeValue());
    }
}
