package com.hlt.healthcare.service.impl;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.commonservice.dto.UserDTO;
import com.hlt.healthcare.client.UserMgmtClient;
import com.hlt.healthcare.dto.EnquiryDTO;
import com.hlt.healthcare.model.EnquiryModel;
import com.hlt.healthcare.populator.EnquiryPopulator;
import com.hlt.healthcare.repository.EnquiryRepository;
import com.hlt.healthcare.service.EnquiryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnquiryServiceImpl implements EnquiryService {

    private final EnquiryRepository enquiryRepository;
    private final UserMgmtClient userMgmtClient;
    private final EnquiryPopulator enquiryPopulator;

    @Override
    @Transactional
    public EnquiryDTO create(EnquiryDTO request) {
        EnquiryModel model = new EnquiryModel();

        model.setBusinessId(request.getBusinessId());
        model.setTelecallerId(request.getTelecallerId());
        model.setProspectName(request.getProspectName());
        model.setProspectContact(request.getProspectContact());
        model.setProspectEmail(request.getProspectEmail());
        model.setEnquiryReason(request.getEnquiryReason());
        model.setInteractionNotes(request.getInteractionNotes());
        model.setNextFollowUpDate(request.getNextFollowUpDate());
        model.setFollowUpDone(Boolean.TRUE.equals(request.getFollowUpDone()));
        model.setConvertedToAppointment(Boolean.TRUE.equals(request.getConvertedToAppointment()));
        model.setConvertedToPatient(Boolean.TRUE.equals(request.getConvertedToPatient()));
        if (request.getProspectContact() != null) {
            model.setProspectContactHash(DigestUtils.sha256Hex(request.getProspectContact()));
        }
        enquiryRepository.save(model);
        return toDTO(model);
    }

    @Override
    public Page<EnquiryDTO> getByBusiness(Long businessId, Pageable pageable) {
        return enquiryRepository.findByBusinessId(businessId, pageable)
                .map(this::toDTO);
    }

    @Override
    public Page<EnquiryDTO> getByTelecaller(Long telecallerId, Pageable pageable) {
        return enquiryRepository.findByTelecallerId(telecallerId, pageable)
                .map(this::toDTO);
    }

    @Override
    public EnquiryDTO getById(Long enquiryId) {
        EnquiryModel model = enquiryRepository.findById(enquiryId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.ENQUIRY_NOT_FOUND));
        return toDTO(model);
    }

    @Override
    public Page<EnquiryDTO> getByProspectContact(String contact, Pageable pageable) {
        return enquiryRepository.findByProspectContact(contact, pageable)
                .map(this::toDTO);
    }


    @Override
    public Page<EnquiryDTO> getCustomerHistoryByContactHash(String hash, int page, int size) {
        List<EnquiryModel> allEnquiries = enquiryRepository.findCustomerHistoryByContactHash(hash);

        if (allEnquiries.isEmpty()) {
            throw new HltCustomerException(ErrorCode.ENQUIRY_NOT_FOUND,
                    "No enquiries found for the given contact hash");
        }

        // Sort newest first (optional)
        allEnquiries.sort((e1, e2) -> e2.getCreatedAt().compareTo(e1.getCreatedAt()));

        // Pagination calculation
        int start = Math.min(page * size, allEnquiries.size());
        int end = Math.min(start + size, allEnquiries.size());

        List<EnquiryDTO> pagedList = allEnquiries.subList(start, end).stream()
                .map(this::toDTO)
                .toList();

        return new PageImpl<>(pagedList, PageRequest.of(page, size), allEnquiries.size());
    }

    private EnquiryDTO toDTO(EnquiryModel model) {
        EnquiryDTO dto = new EnquiryDTO();
        enquiryPopulator.populate(model, dto);
        return dto;
    }
}
