package com.juvarya.order.service.impl;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.juvarya.order.dao.ComplaintsRepository;
import com.juvarya.order.dto.ComplaintDTO;
import com.juvarya.order.dto.enums.ComplaintStatus;
import com.juvarya.order.dto.request.ComplaintCreateRequest;
import com.juvarya.order.entity.ComplaintsModel;
import com.juvarya.order.populator.ComplaintPopulator;
import com.juvarya.order.service.ComplaintService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComplaintServiceImpl implements ComplaintService {

    @Autowired
    private ComplaintsRepository complaintsRepository;

    @Autowired
    private ComplaintPopulator complaintPopulator;

    @Override
    public ComplaintDTO createComplaint(ComplaintCreateRequest request) {
        validateCreateRequest(request);

        ComplaintsModel model = new ComplaintsModel();
        model.setTitle(request.getTitle());
        model.setDescription(request.getDescription());
        model.setCreatedDt(LocalDateTime.now());
        model.setStatus(ComplaintStatus.OPEN);
        model.setComplaintType(request.getComplaintType());
        model.setOrderId(request.getOrderId());
        model.setCreatedBy(request.getCreatedBy());
        model.setB2bId(request.getBusinessId());

        ComplaintsModel saved = complaintsRepository.save(model);
        return populateDTO(saved);
    }

    @Override
    public ComplaintDTO getComplaintById(Long id) {
        if (id == null) {
            throw new HltCustomerException(ErrorCode.INVALID_REQUEST, "Complaint ID must not be null.");
        }

        ComplaintsModel model = complaintsRepository.findById(id)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.COMPLAINT_NOT_FOUND, "Complaint not found with ID: " + id));

        return populateDTO(model);
    }

    @Override
    public Page<ComplaintDTO> filterComplaints(String orderId, Long b2bId, Long createdBy, List<ComplaintStatus> statuses, Pageable pageable) {
        Specification<ComplaintsModel> spec = Specification.where(null);

        if (orderId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("orderId"), orderId));
        }

        if (b2bId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("b2bId"), b2bId));
        }

        if (createdBy != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("createdBy"), createdBy));
        }

        if (statuses != null && !statuses.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("status").in(statuses));
        }

        return complaintsRepository.findAll(spec, pageable).map(this::populateDTO);
    }


    private ComplaintDTO populateDTO(ComplaintsModel model) {
        ComplaintDTO dto = new ComplaintDTO();
        complaintPopulator.populate(model, dto);
        return dto;
    }

    private void validateCreateRequest(ComplaintCreateRequest request) {
        if (request == null || request.getTitle() == null || request.getDescription() == null) {
            throw new HltCustomerException(ErrorCode.INVALID_REQUEST, "Title and description are required.");
        }
    }
}
