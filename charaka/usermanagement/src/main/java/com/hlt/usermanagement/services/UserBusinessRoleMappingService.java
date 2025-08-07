package com.hlt.usermanagement.services;

import com.hlt.usermanagement.dto.UserBusinessRoleMappingDTO;
import com.hlt.usermanagement.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserBusinessRoleMappingService {


    UserBusinessRoleMappingDTO onboardHospitalAdmin(UserBusinessRoleMappingDTO dto);

    UserBusinessRoleMappingDTO onboardTelecaller(UserBusinessRoleMappingDTO dto);

    UserBusinessRoleMappingDTO onboardDoctor(UserBusinessRoleMappingDTO dto);

    UserBusinessRoleMappingDTO onboardReceptionist(UserBusinessRoleMappingDTO dto);

    UserBusinessRoleMappingDTO assignTelecallerToHospital(Long telecallerId, Long hospitalId);

    Page<UserDTO> getDoctorsByHospital(Long hospitalId, Pageable pageable);

    Page<UserDTO> getReceptionistsByHospital(Long hospitalId, Pageable pageable);

    Page<UserDTO> getAssignableTelecallersForHospital(Long hospitalId, int page, int size);

//     String forgetPassword(String email);

}
