package com.hlt.healthcare.controllers;

import com.hlt.commonservice.dto.StandardResponse;
import com.hlt.healthcare.dto.PatientResponseDTO;
import com.hlt.healthcare.dto.request.PatientCreateRequest;
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

    @PostMapping
    public StandardResponse<PatientResponseDTO> createPatient(@RequestBody PatientCreateRequest request) {
        PatientResponseDTO response = patientService.create(request);
        return StandardResponse.single("Patient created successfully", response);
    }

    @GetMapping("/{id}")
    public StandardResponse<PatientResponseDTO> getPatientById(@PathVariable Long id) {
        PatientResponseDTO response = patientService.getById(id);
        return StandardResponse.single("Fetched patient successfully", response);
    }

    @GetMapping("/business/{businessId}")
    public StandardResponse<Page<PatientResponseDTO>> getPatientsByBusiness(@PathVariable Long businessId,
                                                                            Pageable pageable) {
        Page<PatientResponseDTO> response = patientService.getByBusiness(businessId, pageable);
        return StandardResponse.page("Fetched patients for business successfully", response);
    }

    @GetMapping("/phone/{phone}")
    public StandardResponse<PatientResponseDTO> getPatientByPhone(@PathVariable String phone) {
        PatientResponseDTO response = patientService.getByPhone(phone);
        return StandardResponse.single("Fetched patient by phone successfully", response);
    }
}
