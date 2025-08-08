package com.hlt.healthcare.controllers;

import com.hlt.commonservice.dto.StandardResponse;
import com.hlt.healthcare.dto.AppointmentResponseDTO;
import com.hlt.healthcare.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public StandardResponse<AppointmentResponseDTO> createAppointment(@RequestBody AppointmentResponseDTO request) {
        AppointmentResponseDTO response = appointmentService.create(request);
        return StandardResponse.single("Appointment created successfully", response);
    }

    @GetMapping("/business/{businessId}")
    public StandardResponse<Page<AppointmentResponseDTO>> getAppointmentsByBusiness(@PathVariable Long businessId,
                                                                                    Pageable pageable) {
        Page<AppointmentResponseDTO> response = appointmentService.getByBusiness(businessId, pageable);
        return StandardResponse.page("Fetched appointments for business successfully", response);
    }

    @GetMapping("/doctor/{doctorId}")
    public StandardResponse<Page<AppointmentResponseDTO>> getAppointmentsByDoctor(@PathVariable Long doctorId,
                                                                                  Pageable pageable) {
        Page<AppointmentResponseDTO> response = appointmentService.getByDoctor(doctorId, pageable);
        return StandardResponse.page("Fetched appointments for doctor successfully", response);
    }

    @GetMapping("/{appointmentId}")
    public StandardResponse<AppointmentResponseDTO> getAppointmentById(@PathVariable Long appointmentId) {
        AppointmentResponseDTO response = appointmentService.getById(appointmentId);
        return StandardResponse.single("Fetched appointment successfully", response);
    }
}
