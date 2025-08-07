package com.hlt.healthcare.service;

import com.hlt.healthcare.dto.PatientResponseDTO;
import com.hlt.healthcare.dto.request.PatientCreateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PatientService {

    PatientResponseDTO create(PatientCreateRequest request);

    PatientResponseDTO getById(Long id);

    PatientResponseDTO getByPhone(String phone);

    Page<PatientResponseDTO> getByBusiness(Long businessId, Pageable pageable);
}
