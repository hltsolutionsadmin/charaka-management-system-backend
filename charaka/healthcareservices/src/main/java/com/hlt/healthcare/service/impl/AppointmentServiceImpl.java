package com.hlt.healthcare.service.impl;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.healthcare.dto.AppointmentDTO;
import com.hlt.healthcare.dto.enums.AppointmentStatus;
import com.hlt.healthcare.model.AppointmentModel;
import com.hlt.healthcare.model.EnquiryModel;
import com.hlt.healthcare.populator.AppointmentPopulator;
import com.hlt.healthcare.repository.AppointmentRepository;
import com.hlt.healthcare.repository.EnquiryRepository;
import com.hlt.healthcare.service.AppointmentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final EnquiryRepository enquiryRepository;
    private final AppointmentPopulator appointmentPopulator;

    @Override
    @Transactional
    public AppointmentDTO create(AppointmentDTO request) {
        EnquiryModel enquiry = enquiryRepository.findById(request.getEnquiryId())
                .orElseThrow(() -> new HltCustomerException(ErrorCode.ENQUIRY_NOT_FOUND));

        enquiry.setConvertedToAppointment(true);

        AppointmentModel model = new AppointmentModel();
        model.setBusinessId(request.getHospitalId());
        model.setDoctorId(request.getDoctorId());
        model.setEnquiry(enquiry);
        model.setAppointmentDateTime(request.getAppointmentDateTime());
        model.setAppointmentNotes(request.getAppointmentNotes());
        model.setStatus(AppointmentStatus.REQUEST);
        appointmentRepository.save(model);

        return toDTO(model);
    }

    @Override
    public Page<AppointmentDTO> getByBusiness(Long businessId, Pageable pageable) {
        return appointmentRepository.findByBusinessId(businessId, pageable)
                .map(this::toDTO);
    }

    @Override
    public Page<AppointmentDTO> getByDoctor(Long doctorId, Pageable pageable) {
        return appointmentRepository.findByDoctorId(doctorId, pageable)
                .map(this::toDTO);
    }

    @Override
    public AppointmentDTO getById(Long appointmentId) {
        AppointmentModel model = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.APPOINTMENT_NOT_FOUND));
        return toDTO(model);
    }

    private AppointmentDTO toDTO(AppointmentModel model) {
        AppointmentDTO dto = new AppointmentDTO();
        appointmentPopulator.populate(model, dto);
        return dto;
    }
}
