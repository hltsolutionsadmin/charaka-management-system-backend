package com.hlt.usermanagement.services;


import com.hlt.usermanagement.model.CustomerLastLoginModel;
import com.hlt.usermanagement.model.UserModel;

public interface CustomerLastLoginService {
    CustomerLastLoginModel save(CustomerLastLoginModel customerLastLoginModel);

    CustomerLastLoginModel findByJtCustomer(UserModel userModel);
}
