package com.hlt.healthcare.service.impl;

import com.hlt.commonservice.dto.UserDTO;
import com.hlt.healthcare.client.UserMgmtClient;
import com.hlt.healthcare.dto.EnquiryResponseDTO;
import com.hlt.healthcare.dto.request.EnquiryCreateRequest;
import com.hlt.healthcare.model.EnquiryModel;
import com.hlt.healthcare.model.PatientModel;
import com.hlt.healthcare.populator.EnquiryMapper;
import com.hlt.healthcare.repository.EnquiryRepository;
import com.hlt.healthcare.repository.PatientRepository;
import com.hlt.healthcare.service.EnquiryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnquiryServiceImpl implements EnquiryService {

    private final EnquiryRepository enquiryRepository;
    private final PatientRepository patientRepository;
    private final UserMgmtClient userMgmtClient;

    @Override
    @Transactional
    public EnquiryResponseDTO create(EnquiryCreateRequest request) {
        EnquiryModel model = new EnquiryModel();

        model.setBusinessId(request.getBusinessId());
        model.setTelecallerId(request.getTelecallerId());

        if (request.getPatientId() != null) {
            PatientModel patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + request.getPatientId()));
            model.setPatient(patient);
        }

        model.setProspectName(request.getProspectName());
        model.setProspectContact(request.getProspectContact());
        model.setProspectEmail(request.getProspectEmail());
        model.setEnquiryReason(request.getEnquiryReason());
        model.setInteractionNotes(request.getInteractionNotes());
        model.setNextFollowUpDate(request.getNextFollowUpDate());
        model.setFollowUpDone(request.getFollowUpDone() != null ? request.getFollowUpDone() : false);
        model.setConvertedToAppointment(request.getConvertedToAppointment() != null ? request.getConvertedToAppointment() : false);
        model.setConvertedToPatient(request.getConvertedToPatient() != null ? request.getConvertedToPatient() : false);

        enquiryRepository.save(model);

        return populate(model);
    }

    @Override
    public Page<EnquiryResponseDTO> getByBusiness(Long businessId, Pageable pageable) {
        return enquiryRepository.findByBusinessId(businessId, pageable).map(this::populate);
    }

    @Override
    public Page<EnquiryResponseDTO> getByTelecaller(Long telecallerId, Pageable pageable) {
        return enquiryRepository.findByTelecallerId(telecallerId, pageable).map(this::populate);
    }

    @Override
    public EnquiryResponseDTO getById(Long enquiryId) {
        EnquiryModel model = enquiryRepository.findById(enquiryId)
                .orElseThrow(() -> new RuntimeException("Enquiry not found with ID: " + enquiryId));
        return populate(model);
    }

    private EnquiryResponseDTO populate(EnquiryModel model) {
        UserDTO telecaller = null;
        try {
            telecaller = userMgmtClient.getUserById(model.getTelecallerId());
        } catch (Exception ignored) {}

        PatientModel patient = model.getPatient();

        return EnquiryMapper.toDTO(model, telecaller, patient);
    }
}

