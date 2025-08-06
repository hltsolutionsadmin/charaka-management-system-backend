package com.hlt.usermanagement.services;

import com.hlt.usermanagement.dto.UserBusinessRoleMappingDTO;
import com.hlt.usermanagement.dto.UserDTO;

import java.util.List;

public interface UserBusinessRoleMappingService {


    UserBusinessRoleMappingDTO onboardHospitalAdmin(UserBusinessRoleMappingDTO dto);

    void onboardTelecaller(UserBusinessRoleMappingDTO dto);

    void onboardDoctor(UserBusinessRoleMappingDTO dto);

    void onboardReceptionist(UserBusinessRoleMappingDTO dto);

    void assignTelecallerToHospital(Long telecallerId, Long hospitalId);

    List<UserDTO> getAssignableTelecallersForHospital(Long hospitalId);

    List<UserDTO> getDoctorsByHospital(Long hospitalId);

    List<UserDTO> getReceptionistsByHospital(Long hospitalId);


}
