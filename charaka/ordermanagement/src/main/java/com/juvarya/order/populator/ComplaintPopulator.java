package com.juvarya.order.populator;

import com.juvarya.order.dto.ComplaintDTO;
import com.juvarya.order.entity.ComplaintsModel;
import com.hlt.utils.Populator;
import org.springframework.stereotype.Component;

@Component
public class ComplaintPopulator implements Populator<ComplaintsModel, ComplaintDTO> {

    @Override
    public void populate(ComplaintsModel source, ComplaintDTO target) {
        if (source == null || target == null) return;

        target.setId(source.getId());
        target.setTitle(source.getTitle());
        target.setDescription(source.getDescription());
        target.setCreatedDt(source.getCreatedDt());
        target.setCreatedBy(source.getCreatedBy());
        target.setStatus(source.getStatus());
        target.setComplaintType(source.getComplaintType());
        target.setAssignedTo(source.getAssignedTo());
        target.setAssignedOn(source.getAssignedOn());
        target.setOrderId(source.getOrderId());
        target.setBusinessId(source.getB2bId());
    }
}
