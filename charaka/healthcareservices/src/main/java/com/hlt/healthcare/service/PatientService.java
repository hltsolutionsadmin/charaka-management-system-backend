package com.hlt.healthcare.service;

import com.hlt.healthcare.dto.PatientDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PatientService {

    // Create or update patient
    PatientDTO save(PatientDTO patientDTO);

    // Get patient by ID
    PatientDTO getById(Long id);

    // Get all patients paginated
    Page<PatientDTO> findAll(Pageable pageable);

    // Delete patient by ID
    void deleteById(Long id);

    // Find patient by patient code
    PatientDTO findByPatientCode(String patientCode);

    // Find patients by appointment ID
    PatientDTO findByAppointmentId(Long appointmentId);

    // Find patients by business ID (with pagination)
    Page<PatientDTO> findByBusinessId(Long businessId, Pageable pageable);
}
