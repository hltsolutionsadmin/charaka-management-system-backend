package com.juvarya.user.access.mgmt.services.impl;



import com.juvarya.user.access.mgmt.model.CustomerLastLoginModel;
import com.juvarya.user.access.mgmt.model.UserModel;
import com.juvarya.user.access.mgmt.repository.CustomerLastLoginRepository;
import com.juvarya.user.access.mgmt.services.CustomerLastLoginService;
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
