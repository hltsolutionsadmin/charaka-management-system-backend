package com.juvarya.order.service;

import com.juvarya.order.dto.ComplaintDTO;
import com.juvarya.order.dto.enums.ComplaintStatus;
import com.juvarya.order.dto.request.ComplaintCreateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ComplaintService {

    ComplaintDTO createComplaint(ComplaintCreateRequest request);

    ComplaintDTO getComplaintById(Long id);

    Page<ComplaintDTO> filterComplaints(String orderId, Long b2bId, Long createdBy, List<ComplaintStatus> statuses, Pageable pageable);

}
