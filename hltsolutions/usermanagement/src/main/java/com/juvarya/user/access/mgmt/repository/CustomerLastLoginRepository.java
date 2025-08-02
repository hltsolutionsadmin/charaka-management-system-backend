package com.juvarya.user.access.mgmt.repository;


import com.juvarya.user.access.mgmt.model.CustomerLastLoginModel;
import com.juvarya.user.access.mgmt.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerLastLoginRepository extends JpaRepository<CustomerLastLoginModel, Long> {

    CustomerLastLoginModel findByCustomer(UserModel userModel);
}
