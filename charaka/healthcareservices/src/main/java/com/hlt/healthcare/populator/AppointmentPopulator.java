package com.hlt.healthcare.populator;

import com.hlt.healthcare.dto.AppointmentDTO;
import com.hlt.healthcare.model.AppointmentModel;
import com.hlt.utils.Populator;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AppointmentPopulator implements Populator<AppointmentModel, AppointmentDTO> {

    @Override
    public void populate(AppointmentModel source, AppointmentDTO target) {
        target.setId(source.getId());
        target.setHospitalId(source.getBusinessId());

        if (source.getEnquiry() != null) {
            target.setEnquiryId(source.getEnquiry().getId());
        }

        target.setDoctorId(source.getDoctorId());
        target.setAppointmentDateTime(source.getAppointmentDateTime());
        target.setAppointmentNotes(source.getAppointmentNotes());
        target.setStatus(source.getStatus());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        target.setCreatedBy(source.getCreatedBy());
        target.setUpdatedBy(source.getUpdatedBy());


    }
}
