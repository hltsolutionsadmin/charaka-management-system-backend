package com.hlt.healthcare.service.impl;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.healthcare.dto.PatientDTO;
import com.hlt.healthcare.model.AppointmentModel;
import com.hlt.healthcare.model.PatientModel;
import com.hlt.healthcare.populator.PatientPopulator;
import com.hlt.healthcare.repository.AppointmentRepository;
import com.hlt.healthcare.repository.PatientRepository;
import com.hlt.healthcare.service.PatientService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final PatientPopulator patientPopulator;
    private final AppointmentRepository appointmentRepository;

    @Override
    public PatientDTO save(PatientDTO patientDTO) {
        PatientModel model = new PatientModel();

        if (patientDTO.getId() != null) {
            model = patientRepository.findById(patientDTO.getId())
                    .orElse(new PatientModel());
        }

        populateModelFromDTO(patientDTO, model);

        PatientModel saved = patientRepository.save(model);
        return toDTO(saved);
    }

    @Override
    public PatientDTO getById(Long id) {
        PatientModel model = patientRepository.findById(id)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.PATIENT_NOT_FOUND));
        return toDTO(model);
    }

    @Override
    public Page<PatientDTO> findAll(Pageable pageable) {
        return patientRepository.findAll(pageable).map(this::toDTO);
    }

    @Override
    public void deleteById(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new HltCustomerException(ErrorCode.PATIENT_NOT_FOUND);
        }
        patientRepository.deleteById(id);
    }

    @Override
    public PatientDTO findByPatientCode(String patientCode) {
        PatientModel model = patientRepository.findByPatientCode(patientCode)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.PATIENT_NOT_FOUND));
        return toDTO(model);
    }

    @Override
    public PatientDTO findByAppointmentId(Long appointmentId) {
        PatientModel model = patientRepository.findByAppointment_Id(appointmentId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.PATIENT_NOT_FOUND));
        return toDTO(model);
    }

    @Override
    public Page<PatientDTO> findByBusinessId(Long businessId, Pageable pageable) {
        return patientRepository.findByBusinessId(businessId, pageable).map(this::toDTO);
    }

    private void populateModelFromDTO(PatientDTO dto, PatientModel model) {
        model.setBusinessId(dto.getBusinessId());
        model.setRegisteredBy(dto.getRegisteredBy());
        model.setPatientCode(dto.getPatientCode());
        model.setStatus(dto.getStatus());
        model.setEmergencyContact(dto.getEmergencyContact());
        model.setBloodGroup(dto.getBloodGroup());
        model.setAllergies(dto.getAllergies());

        if (dto.getAppointment() != null && dto.getAppointment().getId() != null) {
            AppointmentModel appointment = appointmentRepository.findById(dto.getAppointment().getId())
                    .orElseThrow(() -> new HltCustomerException(ErrorCode.APPOINTMENT_NOT_FOUND));
            model.setAppointment(appointment);
        } else {
            model.setAppointment(null);
        }

        model.setDob(dto.getDob());
        model.setNotes(dto.getNotes());
    }

    private PatientDTO toDTO(PatientModel model) {
        PatientDTO dto = new PatientDTO();
        patientPopulator.populate(model, dto);
        return dto;
    }
}
