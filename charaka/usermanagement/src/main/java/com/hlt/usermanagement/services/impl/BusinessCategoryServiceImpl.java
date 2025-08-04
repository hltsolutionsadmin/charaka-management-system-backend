package com.hlt.usermanagement.services.impl;

import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.usermanagement.dto.request.BusinessCategoryRequest;
import com.hlt.usermanagement.model.BusinessCategoryModel;
import com.hlt.usermanagement.repository.BusinessCategoryRepository;
import com.hlt.usermanagement.services.BusinessCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BusinessCategoryServiceImpl implements BusinessCategoryService {

    @Autowired
    private BusinessCategoryRepository repository;

    @Override
    public BusinessCategoryModel createCategory(BusinessCategoryRequest request) {
        if (repository.existsByName(request.getName())) {
            throw new HltCustomerException( ErrorCode.ALREADY_EXISTS);
        }

        BusinessCategoryModel category = new BusinessCategoryModel();
        category.setName(request.getName());
        return repository.save(category);
    }

    @Override
    public List<BusinessCategoryModel> listAll() {
        return repository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public BusinessCategoryModel getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    @Override
    public List<BusinessCategoryModel> searchByName(String keyword) {
        return repository.findByNameContainingIgnoreCase(keyword);
    }

    @Override
    public BusinessCategoryModel getByName(String name) {
        return repository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.CATEGORY_NOT_FOUND));
    }


}