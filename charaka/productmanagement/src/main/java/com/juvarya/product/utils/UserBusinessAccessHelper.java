package com.juvarya.product.utils;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.commonservice.dto.UserDTO;
import com.hlt.commonservice.enums.ERole;
import com.hlt.commonservice.user.UserDetailsImpl;
import com.juvarya.product.client.UserMgmtClient;
import com.juvarya.product.dto.B2BUnitDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@Component
public class UserBusinessAccessHelper {

    @Autowired
    private UserMgmtClient userMgmtClient;

    public void validateAccess(UserDetailsImpl userDetails, Long requestBusinessId) {
        Long userId = userDetails.getId();
        UserDTO user = userMgmtClient.getUserById(userId);

        if (user == null || user.getB2bUnit() == null) {
            throw new HltCustomerException(ErrorCode.UNAUTHORIZED_BUSINESS_ACCESS);
        }

        Long userBusinessId = user.getB2bUnit().getId();

        if (!userBusinessId.equals(requestBusinessId)) {
            throw new HltCustomerException(ErrorCode.UNAUTHORIZED_BUSINESS_ACCESS);
        }

        if (!user.getB2bUnit().isApproved()) {
            throw new HltCustomerException(ErrorCode.BUSINESS_NOT_APPROVED);
        }
    }
}
