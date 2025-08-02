package com.hlt.usermanagement.repository;


import com.hlt.usermanagement.model.CustomerLastLoginModel;
import com.hlt.usermanagement.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerLastLoginRepository extends JpaRepository<CustomerLastLoginModel, Long> {

    CustomerLastLoginModel findByCustomer(UserModel userModel);
}
