package com.hlt.healthcare.service;

import com.hlt.healthcare.dto.AppointmentResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppointmentService {

    AppointmentResponseDTO create(AppointmentResponseDTO request);

    AppointmentResponseDTO getById(Long appointmentId);

    Page<AppointmentResponseDTO> getByBusiness(Long businessId, Pageable pageable);

    Page<AppointmentResponseDTO> getByDoctor(Long doctorId, Pageable pageable);
}
