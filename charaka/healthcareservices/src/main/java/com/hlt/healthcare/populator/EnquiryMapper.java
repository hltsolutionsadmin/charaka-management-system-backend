package com.hlt.healthcare.populator;

import com.hlt.healthcare.dto.EnquiryResponseDTO;

import com.hlt.commonservice.dto.UserDTO;
import com.hlt.healthcare.model.EnquiryModel;
import com.hlt.healthcare.model.PatientModel;

public class EnquiryMapper {

    public static EnquiryResponseDTO toDTO(EnquiryModel model, UserDTO telecaller, PatientModel patient) {
        EnquiryResponseDTO dto = new EnquiryResponseDTO();

        dto.setId(model.getId());
        dto.setBusinessId(model.getBusinessId());

        dto.setTelecallerId(model.getTelecallerId());
        dto.setTelecallerName(telecaller != null ? telecaller.getFullName() : null);

        dto.setPatientId(patient != null ? patient.getId() : null);
        dto.setPatientName(patient != null ? patient.getFullName() : null);

        dto.setProspectName(model.getProspectName());
        dto.setProspectContact(model.getProspectContact());
        dto.setProspectEmail(model.getProspectEmail());

        dto.setEnquiryReason(model.getEnquiryReason());
        dto.setInteractionNotes(model.getInteractionNotes());

        dto.setNextFollowUpDate(model.getNextFollowUpDate());
        dto.setFollowUpDone(model.getFollowUpDone());
        dto.setConvertedToAppointment(model.getConvertedToAppointment());
        dto.setConvertedToPatient(model.getConvertedToPatient());

        return dto;
    }
}
