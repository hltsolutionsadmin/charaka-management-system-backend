
package com.hlt.usermanagement.services;


import com.hlt.commonservice.dto.BasicOnboardUserDTO;
import com.hlt.commonservice.dto.UserDTO;
import com.hlt.commonservice.enums.ERole;

import com.hlt.usermanagement.dto.UserUpdateDTO;
import com.hlt.usermanagement.dto.request.ChangePasswordRequest;
import com.hlt.usermanagement.dto.request.ForgotPasswordRequest;
import com.hlt.usermanagement.model.UserModel;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author juvi
 */
public interface UserService {

    UserModel saveUser(UserModel userModel);

    Long onBoardUserWithCredentials(BasicOnboardUserDTO dto);

    void updateUser(final UserUpdateDTO details, final Long userId);

    Long onBoardUser(final String fullName, final String mobileNumber, final Set<ERole> userRoles, Long b2bUnitId);

    void addUserRole(final Long userId, final ERole userRole);

    void removeUserRole(final String mobileNumber, final ERole userRole);

    UserModel findById(Long id);

    UserDTO getUserById(Long userId);

    List<UserModel> findByIds(List<Long> ids);

    UserModel findByEmail(String email);

    Optional<UserModel> findByPrimaryContact(String primaryContact);

    Optional<UserDTO> findDtoByPrimaryContact(String primaryContact);

    Boolean existsByEmail(final String email, final Long userId);

    List<UserDTO> getUsersByRole(String roleName);

    void clearFcmToken(Long userId);

    long getUserCountByBusinessId(Long businessId);

    Optional<UserModel> findByUsername(@NotBlank String username);

    void forgotPassword(ForgotPasswordRequest request);

     void changePassword(ChangePasswordRequest request) ;

      UserDTO convertToUserDto(UserModel user);
}
