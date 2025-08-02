package com.hlt.usermanagement.populator;

import com.hlt.usermanagement.dto.UserOTPDTO;
import com.hlt.usermanagement.model.UserOTPModel;
import com.hlt.utils.Populator;
import org.springframework.stereotype.Component;


@Component
public class UserOtpPopulator implements Populator<UserOTPModel, UserOTPDTO> {

    @Override
    public void populate(UserOTPModel source, UserOTPDTO target) {
        target.setId(source.getId());
        target.setCreationTime(source.getCreationTime());
        target.setOtpType(source.getOtpType());
        target.setOtp(source.getOtp());
    }

}
