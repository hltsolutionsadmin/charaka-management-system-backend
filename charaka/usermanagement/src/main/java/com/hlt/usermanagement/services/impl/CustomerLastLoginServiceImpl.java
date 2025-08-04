package com.hlt.usermanagement.services.impl;



import com.hlt.usermanagement.model.CustomerLastLoginModel;
import com.hlt.usermanagement.model.UserModel;
import com.hlt.usermanagement.repository.CustomerLastLoginRepository;
import com.hlt.usermanagement.services.CustomerLastLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerLastLoginServiceImpl implements CustomerLastLoginService {

    @Autowired
    private CustomerLastLoginRepository customerLastLoginRepository;

    @Override
    @Transactional
    public CustomerLastLoginModel save(CustomerLastLoginModel customerLastLoginModel) {
        return customerLastLoginRepository.save(customerLastLoginModel);
    }

    @Override
    public CustomerLastLoginModel findByJtCustomer(UserModel userModel) {
        return customerLastLoginRepository.findByCustomer(userModel);
    }

}
