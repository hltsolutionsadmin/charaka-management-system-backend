package com.hlt.healthcare.populator;
import com.hlt.healthcare.dto.AppointmentResponseDTO;

import com.hlt.commonservice.dto.UserDTO;
import com.hlt.healthcare.model.AppointmentModel;
import com.hlt.healthcare.model.PatientModel;

public class AppointmentMapper {

    public static AppointmentResponseDTO toDTO(AppointmentModel model, UserDTO doctor, PatientModel patient) {
        AppointmentResponseDTO dto = new AppointmentResponseDTO();

        dto.setId(model.getId());
        dto.setBusinessId(model.getBusinessId());

        dto.setEnquiryId(model.getEnquiry() != null ? model.getEnquiry().getId() : null);

        dto.setDoctorId(model.getDoctorId());
        dto.setDoctorName(doctor != null ? doctor.getFullName() : null);

        dto.setAppointmentDateTime(model.getAppointmentDateTime());
        dto.setAppointmentNotes(model.getAppointmentNotes());
        dto.setStatus(model.getStatus());

        dto.setPatientId(patient != null ? patient.getId() : null);
        dto.setPatientName(patient != null ? patient.getFullName() : null);

        return dto;
    }
}
