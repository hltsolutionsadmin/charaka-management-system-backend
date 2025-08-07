package com.hlt.healthcare.populator;
import com.hlt.healthcare.dto.PatientResponseDTO;
import com.hlt.commonservice.dto.UserDTO;
import com.hlt.healthcare.model.PatientModel;

public class PatientMapper {

    public static PatientResponseDTO toDTO(PatientModel model, UserDTO registeredBy) {
        PatientResponseDTO dto = new PatientResponseDTO();

        dto.setId(model.getId());
        dto.setBusinessId(model.getBusinessId());

        dto.setEnquiryId(model.getEnquiry() != null ? model.getEnquiry().getId() : null);
        dto.setAppointmentId(model.getAppointment() != null ? model.getAppointment().getId() : null);

        dto.setFullName(model.getFullName());
        dto.setContactNumber(model.getContactNumber());
        dto.setEmail(model.getEmail());
        dto.setGender(model.getGender());
        dto.setDob(model.getDob());
        dto.setNotes(model.getNotes());

        dto.setRegisteredBy(model.getRegisteredBy());
        dto.setRegisteredByName(registeredBy != null ? registeredBy.getFullName() : null);

        return dto;
    }
}
