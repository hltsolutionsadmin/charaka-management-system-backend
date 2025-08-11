package com.hlt.healthcare.controllers;

import com.hlt.commonservice.dto.StandardResponse;
import com.hlt.healthcare.dto.AppointmentDTO;
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
    public StandardResponse<AppointmentDTO> createAppointment(@RequestBody AppointmentDTO request) {
        AppointmentDTO response = appointmentService.create(request);
        return StandardResponse.single("Appointment created successfully", response);
    }

    @GetMapping("/business/{businessId}")
    public StandardResponse<Page<AppointmentDTO>> getAppointmentsByBusiness(@PathVariable Long businessId,
                                                                                    Pageable pageable) {
        Page<AppointmentDTO> response = appointmentService.getByBusiness(businessId, pageable);
        return StandardResponse.page("Fetched appointments for business successfully", response);
    }

    @GetMapping("/doctor/{doctorId}")
    public StandardResponse<Page<AppointmentDTO>> getAppointmentsByDoctor(@PathVariable Long doctorId,
                                                                                  Pageable pageable) {
        Page<AppointmentDTO> response = appointmentService.getByDoctor(doctorId, pageable);
        return StandardResponse.page("Fetched appointments for doctor successfully", response);
    }

    @GetMapping("/{appointmentId}")
    public StandardResponse<AppointmentDTO> getAppointmentById(@PathVariable Long appointmentId) {
        AppointmentDTO response = appointmentService.getById(appointmentId);
        return StandardResponse.single("Fetched appointment successfully", response);
    }
}
