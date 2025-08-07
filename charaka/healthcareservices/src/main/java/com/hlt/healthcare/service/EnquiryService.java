package com.hlt.healthcare.service;

import com.hlt.healthcare.dto.EnquiryResponseDTO;
import com.hlt.healthcare.dto.request.EnquiryCreateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EnquiryService {

    /**
     * Creates a new enquiry.
     *
     * @param request Enquiry create request
     * @return EnquiryResponseDTO with details of the created enquiry
     */
    EnquiryResponseDTO create(EnquiryCreateRequest request);

    /**
     * Retrieves all enquiries for a specific business.
     *
     * @param businessId Business ID
     * @param pageable   Pagination details
     * @return Page of EnquiryResponseDTO
     */
    Page<EnquiryResponseDTO> getByBusiness(Long businessId, Pageable pageable);

    /**
     * Retrieves all enquiries handled by a specific telecaller.
     *
     * @param telecallerId Telecaller ID
     * @param pageable     Pagination details
     * @return Page of EnquiryResponseDTO
     */
    Page<EnquiryResponseDTO> getByTelecaller(Long telecallerId, Pageable pageable);

    /**
     * Retrieves a specific enquiry by ID.
     *
     * @param enquiryId Enquiry ID
     * @return EnquiryResponseDTO
     */
    EnquiryResponseDTO getById(Long enquiryId);
}
