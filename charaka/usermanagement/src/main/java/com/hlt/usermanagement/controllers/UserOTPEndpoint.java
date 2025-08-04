package com.hlt.usermanagement.controllers;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.customerservices.CustomerIntegrationService;
import com.hlt.customerservices.impl.CustomerIntegrationServiceImpl;
import com.hlt.usermanagement.dto.UserOTPDTO;
import com.hlt.usermanagement.model.UserModel;
import com.hlt.usermanagement.model.UserOTPModel;
import com.hlt.usermanagement.services.UserOTPService;
import com.hlt.usermanagement.services.UserService;
import com.hlt.utils.JTBaseEndpoint;
import jakarta.validation.Valid;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping(value = "/auth/jtuserotp")
@Slf4j
public class UserOTPEndpoint extends JTBaseEndpoint {

    @Autowired
    private UserOTPService userOTPService;

    @Autowired
    private UserService userService;


    @Value("${otp.trigger}")
    private boolean triggerOtp;


    @PostMapping("/trigger/sign-in")
    public ResponseEntity<?> signIn( @Valid @RequestBody UserOTPDTO userOtpDto, @RequestParam(defaultValue = "true") boolean triggerOtp ) throws IOException {

        log.info("Entering sign-in with Primary Contact: {}, OTP Type: {}", userOtpDto.getPrimaryContact(), userOtpDto.getOtpType());

        Optional<UserModel> user = userService.findByPrimaryContact(userOtpDto.getPrimaryContact());
        if (user.isEmpty()) {
            throw new HltCustomerException(ErrorCode.USER_NOT_FOUND);
        }

        userOTPService.deleteByPrimaryContactAndOtpType(userOtpDto.getPrimaryContact(), userOtpDto.getOtpType());

        UserOTPModel userOtp = generateAndSaveOtp(userOtpDto);
        UserOTPDTO userOTPDTO = new UserOTPDTO(userOtp.getOtp(), userOtp.getCreationTime());

        CustomerIntegrationService jtCustomerIntegration = new CustomerIntegrationServiceImpl();

        if (triggerOtp) {
            log.debug("Triggering OTP for Primary Contact: {}", userOtp.getPrimaryContact());
            jtCustomerIntegration.triggerSMS(userOtp.getPrimaryContact(), userOtp.getOtp());
            userOTPDTO.setOtp(null); 
        }

        log.info("Returning response for Primary Contact: {}", userOtpDto.getPrimaryContact());

        return new ResponseEntity<>(userOTPDTO, HttpStatus.OK);
    }

    @PostMapping("/trigger/otp")
    public ResponseEntity<?> newUserSignUp( @Valid @RequestBody UserOTPDTO userOtpDto, @RequestParam(defaultValue = "true") boolean triggerOtp) throws IOException {

        log.info("Entering newUserSignUp with Primary Contact: {}, OTP Type: {}", userOtpDto.getPrimaryContact(), userOtpDto.getOtpType());
        userOTPService.deleteByPrimaryContactAndOtpType(userOtpDto.getPrimaryContact(), userOtpDto.getOtpType());

        UserOTPModel userOtp = generateAndSaveOtp(userOtpDto);
        UserOTPDTO userOTPDTO = new UserOTPDTO(userOtp.getOtp(), userOtp.getCreationTime());

        CustomerIntegrationService jtCustomerIntegration = new CustomerIntegrationServiceImpl();
        log.info("Returning response for triggerOtp value: {}", triggerOtp);

        if (triggerOtp) {
            log.debug("Triggering OTP for Primary Contact: {}", userOtp.getPrimaryContact());
            jtCustomerIntegration.triggerSMS(userOtp.getPrimaryContact(), userOtp.getOtp());
            userOTPDTO.setOtp(null);
        }

        log.info("Returning response for Primary Contact: {}", userOtpDto.getPrimaryContact());
        return new ResponseEntity<>(userOTPDTO, HttpStatus.OK);
    }


    private UserOTPModel generateAndSaveOtp(UserOTPDTO userOTPDTO) {
        log.debug("Populating JTUserOTPModel for Primary Contact: {}", userOTPDTO.getPrimaryContact());

        UserOTPModel jtUserOTP = new UserOTPModel();
        jtUserOTP.setChannel(userOTPDTO.getChannel());
        jtUserOTP.setCreationTime(new Date());
        jtUserOTP.setPrimaryContact(userOTPDTO.getPrimaryContact());
        jtUserOTP.setEmailAddress(userOTPDTO.getEmailAddress());
        jtUserOTP.setOtpType(userOTPDTO.getOtpType());

        Map<String, String> staticOtps = Map.of(
                "9100881724", "123123",
                "9700020630", "291120"
        );

        String primaryContact = userOTPDTO.getPrimaryContact();
        String otp = staticOtps.get(primaryContact);

        if (otp != null) {
            jtUserOTP.setOtp(otp);
            log.debug("Static OTP assigned for Primary Contact: {}", primaryContact);
        } else {
            otp = new DecimalFormat("000000").format(new Random().nextInt(999999));
            jtUserOTP.setOtp(otp);
            log.debug("Generated OTP for Primary Contact: {}", primaryContact);
        }

        return userOTPService.save(jtUserOTP);
    }
}
