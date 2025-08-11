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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    private EnquiryDTO toDTO(EnquiryModel model) {
        EnquiryDTO dto = new EnquiryDTO();
        enquiryPopulator.populate(model, dto);
        return dto;
    }
}
