package com.hlt.usermanagement.services;

import com.hlt.usermanagement.dto.request.BusinessCategoryRequest;
import com.hlt.usermanagement.model.BusinessCategoryModel;

import java.util.List;

public interface BusinessCategoryService {
    BusinessCategoryModel createCategory(BusinessCategoryRequest request);
    List<BusinessCategoryModel> listAll();
    BusinessCategoryModel getById(Long id);
    void deleteById(Long id);
    List<BusinessCategoryModel> searchByName(String keyword);

    public BusinessCategoryModel getByName(String name);

}
