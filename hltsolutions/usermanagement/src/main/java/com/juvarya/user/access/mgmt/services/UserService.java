
package com.juvarya.user.access.mgmt.services;



import com.juvarya.commonservice.dto.BasicOnboardUserDTO;
import com.juvarya.commonservice.dto.UserDTO;
import com.juvarya.commonservice.enums.ERole;

import com.juvarya.commonservice.enums.UserVerificationStatus;
import com.juvarya.commonservice.user.UserDetailsImpl;
import com.juvarya.user.access.mgmt.dto.UserUpdateDTO;
import com.juvarya.user.access.mgmt.model.UserModel;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    Long onBoardUser(final String fullName, final String mobileNumber, final Set<ERole> userRoles,Long b2bUnitId);

    void addUserRole(final Long userId, final ERole userRole);

    void removeUserRole(final String mobileNumber, final ERole userRole);

    UserModel findById(Long id);

    UserDTO getUserById(Long userId);

    List<UserModel> findByIds(List<Long> ids);

    UserModel findByEmail(String email);

    Optional<UserModel> findByPrimaryContact(String primaryContact);

    Page<UserModel> getAllUsers(Pageable pageable);

    Boolean existsByEmail(final String email, final Long userId);

    List<UserDTO> getUsersByRole(String roleName);

    void clearFcmToken(Long userId);

    long getUserCountByBusinessId(Long businessId);

    void verifyStudent(Long userId, UserVerificationStatus status);

    Page<UserDTO> getUnverifiedStudents(int page, int size);

    void deleteRolesAndResetSkillratFlag(Long userId);

    void approveDeliveryPartner(Long userId);

    List<UserDTO> getUsersByRoleAndDeliveryPartnerFalse(ERole role);


    Optional<UserModel> findByUsername(@NotBlank String username);
}
