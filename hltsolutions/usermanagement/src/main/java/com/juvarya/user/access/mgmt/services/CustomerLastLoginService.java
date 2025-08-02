package com.juvarya.user.access.mgmt.services;


import com.juvarya.user.access.mgmt.model.CustomerLastLoginModel;
import com.juvarya.user.access.mgmt.model.UserModel;

public interface CustomerLastLoginService {
    CustomerLastLoginModel save(CustomerLastLoginModel customerLastLoginModel);

    CustomerLastLoginModel findByJtCustomer(UserModel userModel);
}
