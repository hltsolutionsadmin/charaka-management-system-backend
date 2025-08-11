package com.hlt.healthcare.populator;

import com.hlt.healthcare.dto.AppointmentDTO;
import com.hlt.healthcare.dto.EnquiryDTO;
import com.hlt.healthcare.model.EnquiryModel;
import com.hlt.utils.Populator;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class EnquiryPopulator implements Populator<EnquiryModel, EnquiryDTO> {

    private final AppointmentPopulator appointmentPopulator;

    public EnquiryPopulator(AppointmentPopulator appointmentPopulator) {
        this.appointmentPopulator = appointmentPopulator;
    }

    @Override
    public void populate(EnquiryModel source, EnquiryDTO target) {
        // Basic IDs
        target.setId(source.getId());
        target.setBusinessId(source.getBusinessId());
        target.setTelecallerId(source.getTelecallerId());


        // Prospect details
        target.setProspectName(source.getProspectName());
        target.setProspectContact(source.getProspectContact());
        target.setProspectEmail(source.getProspectEmail());

        // Enquiry details
        target.setEnquiryReason(source.getEnquiryReason());
        target.setInteractionNotes(source.getInteractionNotes());

        // Follow-up details
        target.setNextFollowUpDate(source.getNextFollowUpDate());
        target.setFollowUpDone(source.getFollowUpDone());

        // Conversion flags
        target.setConvertedToAppointment(source.getConvertedToAppointment());
        target.setConvertedToPatient(source.getConvertedToPatient());

        // Timestamps
        target.setCreationTime(source.getCreatedAt());
        target.setModificationTime(source.getUpdatedAt());

        // Appointments mapping
        if (source.getAppointments() != null && !source.getAppointments().isEmpty()) {
            target.setAppointments(
                    source.getAppointments()
                            .stream()
                            .map(a -> {
                                AppointmentDTO dto = new AppointmentDTO();
                                appointmentPopulator.populate(a, dto);
                                return dto;
                            })
                            .collect(Collectors.toList())
            );
        }
    }
}
