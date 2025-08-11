package com.hlt.healthcare.controllers;

import com.hlt.commonservice.dto.StandardResponse;
import com.hlt.healthcare.dto.EnquiryResponseDTO;
import com.hlt.healthcare.dto.request.EnquiryCreateRequest;
import com.hlt.healthcare.service.EnquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enquiries")
@RequiredArgsConstructor
public class EnquiryController {

    private final EnquiryService enquiryService;

    @PostMapping
    public StandardResponse<EnquiryResponseDTO> createEnquiry(@RequestBody EnquiryCreateRequest request) {
        EnquiryResponseDTO response = enquiryService.create(request);
        return StandardResponse.single("Enquiry created successfully", response);
    }

    @GetMapping("/business/{businessId}")
    public StandardResponse<Page<EnquiryResponseDTO>> getEnquiriesByBusiness(@PathVariable Long businessId,
                                                                             Pageable pageable) {
        Page<EnquiryResponseDTO> response = enquiryService.getByBusiness(businessId, pageable);
        return StandardResponse.page("Fetched enquiries for business successfully", response);
    }

    @GetMapping("/telecaller/{telecallerId}")
    public StandardResponse<Page<EnquiryResponseDTO>> getEnquiriesByTelecaller(@PathVariable Long telecallerId,
                                                                               Pageable pageable) {
        Page<EnquiryResponseDTO> response = enquiryService.getByTelecaller(telecallerId, pageable);
        return StandardResponse.page("Fetched enquiries for telecaller successfully", response);
    }

    @GetMapping("/{enquiryId}")
    public StandardResponse<EnquiryResponseDTO> getEnquiryById(@PathVariable Long enquiryId) {
        EnquiryResponseDTO response = enquiryService.getById(enquiryId);
        return StandardResponse.single("Fetched enquiry successfully", response);
    }

    @GetMapping("/prospect-contact/{contact}")
    public StandardResponse<Page<EnquiryResponseDTO>> getEnquiriesByProspectContact(
            @PathVariable String contact, Pageable pageable) {
        Page<EnquiryResponseDTO> response = enquiryService.getByProspectContact(contact, pageable);
        return StandardResponse.page("Fetched enquiries for prospect contact successfully", response);
    }

}
