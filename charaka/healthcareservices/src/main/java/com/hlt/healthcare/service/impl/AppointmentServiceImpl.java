package com.hlt.healthcare.service.impl;

import com.hlt.commonservice.dto.UserDTO;
import com.hlt.healthcare.client.UserMgmtClient;
import com.hlt.healthcare.dto.AppointmentResponseDTO;
import com.hlt.healthcare.model.AppointmentModel;
import com.hlt.healthcare.model.EnquiryModel;
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
    private final UserMgmtClient userMgmtClient;

    @Override
    @Transactional
    public AppointmentResponseDTO create(AppointmentResponseDTO request) {
        EnquiryModel enquiry = enquiryRepository.findById(request.getEnquiryId())
                .orElseThrow(() -> new RuntimeException("Enquiry not found with id: " + request.getEnquiryId()));
        enquiry.setConvertedToAppointment(true);

        AppointmentModel model = new AppointmentModel();
        model.setBusinessId(request.getBusinessId());
        model.setDoctorId(request.getDoctorId());
        model.setEnquiry(enquiry);
        model.setAppointmentDateTime(request.getAppointmentDateTime());
        model.setAppointmentNotes(request.getAppointmentNotes());
        model.setStatus(request.getStatus());

        appointmentRepository.save(model);

        return populate(model);
    }

    @Override
    public Page<AppointmentResponseDTO> getByBusiness(Long businessId, Pageable pageable) {
        return appointmentRepository.findByBusinessId(businessId, pageable).map(this::populate);
    }

    @Override
    public Page<AppointmentResponseDTO> getByDoctor(Long doctorId, Pageable pageable) {
        return appointmentRepository.findByDoctorId(doctorId, pageable).map(this::populate);
    }

    @Override
    public AppointmentResponseDTO getById(Long appointmentId) {
        AppointmentModel model = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + appointmentId));
        return populate(model);
    }

    private AppointmentResponseDTO populate(AppointmentModel model) {
        UserDTO doctor = null;
        try {
            doctor = userMgmtClient.getUserById(model.getDoctorId());
        } catch (Exception ignored) {
        }

//        return AppointmentMapper.toDTO(model, doctor); //TODO
        return null;
    }
}
