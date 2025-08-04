package com.hlt.usermanagement.populator;

import com.hlt.usermanagement.dto.response.BusinessAttributeResponse;
import com.hlt.usermanagement.model.BusinessAttributeModel;
import com.hlt.utils.Populator;
import org.springframework.stereotype.Component;

@Component
public class BusinessAttributePopulator implements Populator<BusinessAttributeModel, BusinessAttributeResponse> {

    @Override
    public void populate(BusinessAttributeModel source, BusinessAttributeResponse target) {
        if (source == null || target == null) {
            return;
        }
        target.setId(source.getId());
        target.setAttributeName(source.getAttributeName());
        target.setAttributeValue(source.getAttributeValue());
    }
}
