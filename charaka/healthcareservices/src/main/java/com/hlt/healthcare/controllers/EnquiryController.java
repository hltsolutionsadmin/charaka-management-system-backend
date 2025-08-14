package com.hlt.healthcare.controllers;

import com.hlt.commonservice.dto.StandardResponse;
import com.hlt.commonservice.user.UserDetailsImpl;
import com.hlt.healthcare.dto.EnquiryDTO;
import com.hlt.healthcare.service.EnquiryService;
import com.hlt.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enquiries")
@RequiredArgsConstructor
public class EnquiryController {

    private final EnquiryService enquiryService;

    @PostMapping
    public StandardResponse<EnquiryDTO> createEnquiry(@RequestBody EnquiryDTO request) {
        UserDetailsImpl currentUserDetails = SecurityUtils.getCurrentUserDetails();
        request.setTelecallerId(currentUserDetails.getId());
        request.setCreatedByName(currentUserDetails.getUsername());
        EnquiryDTO response = enquiryService.create(request);
        return StandardResponse.single("Enquiry created successfully", response);
    }

    @GetMapping("/business/{businessId}")
    public StandardResponse<Page<EnquiryDTO>> getEnquiriesByBusiness(@PathVariable Long businessId,
                                                                     Pageable pageable) {
        Page<EnquiryDTO> response = enquiryService.getByBusiness(businessId, pageable);
        return StandardResponse.page("Fetched enquiries for business successfully", response);
    }

    @GetMapping("/telecaller")
    public StandardResponse<Page<EnquiryDTO>> getEnquiriesByTelecaller(Pageable pageable) {
        UserDetailsImpl currentUserDetails = SecurityUtils.getCurrentUserDetails();

        Page<EnquiryDTO> response = enquiryService.getByTelecaller(currentUserDetails.getId(), pageable);
        return StandardResponse.page("Fetched enquiries for telecaller successfully", response);
    }

    @GetMapping("/{enquiryId}")
    public StandardResponse<EnquiryDTO> getEnquiryById(@PathVariable Long enquiryId) {
        EnquiryDTO response = enquiryService.getById(enquiryId);
        return StandardResponse.single("Fetched enquiry successfully", response);
    }

    @GetMapping("/prospect-contact/{contact}")
    public StandardResponse<Page<EnquiryDTO>> getEnquiriesByProspectContact(@PathVariable String contact,
                                                                            Pageable pageable) {
        Page<EnquiryDTO> response = enquiryService.getByProspectContact(contact, pageable);
        return StandardResponse.page("Fetched enquiries for prospect contact successfully", response);
    }

    @GetMapping("/customer-history/{contactNumber}")
    public StandardResponse<Page<EnquiryDTO>> getCustomerHistory(
            @PathVariable String contactNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<EnquiryDTO> history = enquiryService.getCustomerHistoryByContactHash(contactNumber, page, size);
        return StandardResponse.page("Customer history fetched successfully", history);
    }

}
