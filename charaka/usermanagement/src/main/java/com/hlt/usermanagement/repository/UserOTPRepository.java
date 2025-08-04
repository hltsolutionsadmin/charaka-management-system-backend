package com.hlt.usermanagement.repository;


import com.hlt.usermanagement.model.UserOTPModel;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserOTPRepository extends JpaRepository<UserOTPModel, Long> {
    List<UserOTPModel> findByEmailAddressAndOtpType(String emailAddress, String otpType, Sort sort);

    void deleteByPrimaryContactAndOtpType(String primaryContact, String otpType);

    UserOTPModel findByOtpTypeAndPrimaryContact(String otpType, String primaryContact);
}
