package com.hlt.healthcare.populator;

import com.hlt.healthcare.dto.PatientDTO;
import com.hlt.healthcare.dto.AppointmentDTO;
import com.hlt.healthcare.model.PatientModel;
import com.hlt.healthcare.populator.AppointmentPopulator;
import com.hlt.utils.Populator;
import org.springframework.stereotype.Component;

@Component
public class PatientPopulator implements Populator<PatientModel, PatientDTO> {

    private final AppointmentPopulator appointmentPopulator;

    public PatientPopulator(AppointmentPopulator appointmentPopulator) {
        this.appointmentPopulator = appointmentPopulator;
    }

    @Override
    public void populate(PatientModel source, PatientDTO target) {
        target.setId(source.getId());
        target.setBusinessId(source.getBusinessId());
        target.setRegisteredBy(source.getRegisteredBy());
        target.setPatientCode(source.getPatientCode());
        target.setStatus(source.getStatus());
        target.setEmergencyContact(source.getEmergencyContact());
        target.setBloodGroup(source.getBloodGroup());
        target.setAllergies(source.getAllergies());
        target.setDob(source.getDob());
        target.setNotes(source.getNotes());

        // Audit fields
        target.setCreationTime(source.getCreatedAt());
        target.setModificationTime(source.getUpdatedAt());
        target.setCreatedBy(source.getCreatedBy());
        target.setUpdatedBy(source.getUpdatedBy());

        // Appointment mapping
        if (source.getAppointment() != null) {
            AppointmentDTO appointmentDTO = new AppointmentDTO();
            appointmentPopulator.populate(source.getAppointment(), appointmentDTO);
            target.setAppointment(appointmentDTO);
        }
    }
}
