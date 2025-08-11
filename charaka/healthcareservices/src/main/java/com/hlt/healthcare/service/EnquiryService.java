package com.hlt.healthcare.service;

import com.hlt.healthcare.dto.EnquiryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EnquiryService {

    /**
     * Creates a new enquiry.
     *
     * @param request Enquiry create request
     * @return EnquiryDTO with details of the created enquiry
     */
    EnquiryDTO create(EnquiryDTO request);

    /**
     * Retrieves all enquiries for a specific business.
     *
     * @param businessId Business ID
     * @param pageable   Pagination details
     * @return Page of EnquiryResponseDTO
     */
    Page<EnquiryDTO> getByBusiness(Long businessId, Pageable pageable);

    /**
     * Retrieves all enquiries handled by a specific telecaller.
     *
     * @param telecallerId Telecaller ID
     * @param pageable     Pagination details
     * @return Page of EnquiryResponseDTO
     */
    Page<EnquiryDTO> getByTelecaller(Long telecallerId, Pageable pageable);

    /**
     * Retrieves a specific enquiry by ID.
     *
     * @param enquiryId Enquiry ID
     * @return EnquiryResponseDTO
     */
    EnquiryDTO getById(Long enquiryId);

    /**
     * Retrieves enquiries by prospect contact (with pagination).
     *
     * @param contact  Prospect contact
     * @param pageable Pagination details
     * @return Page of EnquiryResponseDTO
     */
    Page<EnquiryDTO> getByProspectContact(String contact, Pageable pageable);


}
