package com.hlt.healthcare.service.impl;

import com.hlt.healthcare.dto.PatientResponseDTO;
import com.hlt.healthcare.dto.request.PatientCreateRequest;
import com.hlt.healthcare.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
//
@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {
    @Override
    public PatientResponseDTO create(PatientCreateRequest request) {
        return null;
    }

    @Override
    public PatientResponseDTO getById(Long id) {
        return null;
    }

    @Override
    public PatientResponseDTO getByPhone(String phone) {
        return null;
    }

    @Override
    public Page<PatientResponseDTO> getByBusiness(Long businessId, Pageable pageable) {
        return null;
    }
//
//    private final PatientRepository patientRepository;
//
//    @Override
//    public PatientResponseDTO create(PatientCreateRequest request) {
//        PatientModel model = new PatientModel();
//        model.setBusinessId(request.getBusinessId());
//        model.setFullName(request.getFullName());
//        model.setContactNumber(request.getPrimaryPhone());
//        model.setEmail(request.getEmail());
//        model.setGender(request.getGender());
//        model.setDob(request.getDateOfBirth());
//        model.setAge(request.getAge());
//        model.setBloodGroup(request.getBloodGroup());
//
//        patientRepository.save(model);
//        return PatientMapper.toDTO(model);
//    }
//
//    @Override
//    public PatientResponseDTO getById(Long id) {
//        PatientModel model = patientRepository.findById(id)
//                .orElseThrow(() -> new PatientNotFoundException(id));
//        return PatientMapper.toDTO(model);
//    }
//
//    @Override
//    public PatientResponseDTO getByPhone(String phone) {
//        PatientModel model = patientRepository.findByPrimaryPhone(phone)
//                .orElseThrow(() -> new PatientNotFoundException("Phone: " + phone));
//        return PatientMapper.toDTO(model);
//    }
//
//    @Override
//    public Page<PatientResponseDTO> getByBusiness(Long businessId, Pageable pageable) {
//        return patientRepository.findByBusinessId(businessId, pageable)
//                .map(PatientMapper::toDTO);
    }

