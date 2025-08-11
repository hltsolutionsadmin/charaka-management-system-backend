package com.hlt.healthcare.service;

import com.hlt.healthcare.dto.AppointmentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppointmentService {

    AppointmentDTO create(AppointmentDTO request);

    AppointmentDTO getById(Long appointmentId);

    Page<AppointmentDTO> getByBusiness(Long businessId, Pageable pageable);

    Page<AppointmentDTO> getByDoctor(Long doctorId, Pageable pageable);
}
