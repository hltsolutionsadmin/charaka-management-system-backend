package com.hlt.usermanagement.populator;

import com.hlt.usermanagement.dto.response.ProductAttributeResponse;
import com.hlt.usermanagement.model.ProductAttributeModel;
import com.hlt.utils.Populator;
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
