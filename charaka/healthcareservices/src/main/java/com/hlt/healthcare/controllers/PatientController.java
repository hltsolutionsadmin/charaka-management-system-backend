package com.hlt.healthcare.controllers;

import com.hlt.commonservice.dto.StandardResponse;
import com.hlt.healthcare.dto.PatientDTO;
import com.hlt.healthcare.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    // Create or update patient
    @PostMapping
    public StandardResponse<PatientDTO> savePatient(@RequestBody PatientDTO patientDTO) {
        PatientDTO savedPatient = patientService.save(patientDTO);
        return StandardResponse.single("Patient saved successfully", savedPatient);
    }

    // Get patient by ID
    @GetMapping("/{id}")
    public StandardResponse<PatientDTO> getPatientById(@PathVariable Long id) {
        PatientDTO patient = patientService.getById(id);
        return StandardResponse.single("Patient fetched successfully", patient);
    }

    // Get all patients paginated
    @GetMapping
    public StandardResponse<Page<PatientDTO>> getAllPatients(Pageable pageable) {
        Page<PatientDTO> patients = patientService.findAll(pageable);
        return StandardResponse.page("Patients fetched successfully", patients);
    }

    // Delete patient by ID
    @DeleteMapping("/{id}")
    public StandardResponse<Void> deletePatient(@PathVariable Long id) {
        patientService.deleteById(id);
        return StandardResponse.message("Patient deleted successfully");
    }

    // Find patient by patient code
    @GetMapping("/code/{patientCode}")
    public StandardResponse<PatientDTO> getPatientByCode(@PathVariable String patientCode) {
        PatientDTO patient = patientService.findByPatientCode(patientCode);
        return StandardResponse.single("Patient fetched successfully", patient);
    }

    // Find patient by appointment ID
    @GetMapping("/appointment/{appointmentId}")
    public StandardResponse<PatientDTO> getPatientByAppointment(@PathVariable Long appointmentId) {
        PatientDTO patient = patientService.findByAppointmentId(appointmentId);
        return StandardResponse.single("Patient fetched successfully", patient);
    }

    // Find patients by business ID (paginated)
    @GetMapping("/business/{businessId}")
    public StandardResponse<Page<PatientDTO>> getPatientsByBusiness(@PathVariable Long businessId, Pageable pageable) {
        Page<PatientDTO> patients = patientService.findByBusinessId(businessId, pageable);
        return StandardResponse.page("Patients fetched successfully", patients);
    }
}
