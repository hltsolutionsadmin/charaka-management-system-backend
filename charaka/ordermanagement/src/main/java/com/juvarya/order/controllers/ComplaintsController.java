package com.juvarya.order.controllers;

import com.hlt.commonservice.dto.StandardResponse;
import com.hlt.commonservice.user.UserDetailsImpl;
import com.juvarya.order.dto.enums.ComplaintStatus;
import com.juvarya.order.dto.ComplaintDTO;
import com.juvarya.order.dto.request.ComplaintCreateRequest;
import com.juvarya.order.service.ComplaintService;
import com.hlt.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders/complaints")
@RequiredArgsConstructor
public class ComplaintsController {

    private final ComplaintService complaintService;

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping
    public StandardResponse<ComplaintDTO> createComplaint(@RequestBody ComplaintCreateRequest request) {
        UserDetailsImpl loggedInUser = SecurityUtils.getCurrentUserDetails();
        request.setCreatedBy(loggedInUser.getId());
        ComplaintDTO dto = complaintService.createComplaint(request);
        return StandardResponse.single("Complaint raised successfully", dto);
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/{id}")
    public StandardResponse<ComplaintDTO> getComplaintById(@PathVariable Long id) {
        ComplaintDTO dto = complaintService.getComplaintById(id);
        return StandardResponse.single("Complaint details", dto);
    }

    @GetMapping("/filter")
    public StandardResponse<Page<ComplaintDTO>> filterComplaints(
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) Long businessId,
            @RequestParam(required = false) Long createdBy,
            @RequestParam(required = false) List<ComplaintStatus> statuses,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ComplaintDTO> result = complaintService.filterComplaints(orderId, businessId, createdBy, statuses, pageable);
        return StandardResponse.page("Filtered complaints list", result);
    }

}
