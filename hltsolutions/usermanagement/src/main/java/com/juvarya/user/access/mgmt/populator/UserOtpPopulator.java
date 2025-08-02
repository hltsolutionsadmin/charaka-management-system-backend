package com.juvarya.user.access.mgmt.populator;

import com.juvarya.user.access.mgmt.dto.UserOTPDTO;
import com.juvarya.user.access.mgmt.model.UserOTPModel;
import com.juvarya.utils.Populator;
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
