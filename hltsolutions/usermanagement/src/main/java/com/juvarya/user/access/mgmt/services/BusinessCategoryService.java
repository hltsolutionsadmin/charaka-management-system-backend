package com.juvarya.user.access.mgmt.services;

import com.juvarya.user.access.mgmt.dto.request.BusinessCategoryRequest;
import com.juvarya.user.access.mgmt.model.BusinessCategoryModel;

import java.util.List;

public interface BusinessCategoryService {
    BusinessCategoryModel createCategory(BusinessCategoryRequest request);
    List<BusinessCategoryModel> listAll();
    BusinessCategoryModel getById(Long id);
    void deleteById(Long id);
    List<BusinessCategoryModel> searchByName(String keyword);

    public BusinessCategoryModel getByName(String name);

}
