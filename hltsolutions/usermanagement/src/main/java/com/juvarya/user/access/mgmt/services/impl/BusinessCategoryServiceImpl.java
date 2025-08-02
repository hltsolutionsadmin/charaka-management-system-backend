package com.juvarya.user.access.mgmt.services.impl;

import com.juvarya.auth.exception.handling.JuvaryaCustomerException;
import com.juvarya.auth.exception.handling.ErrorCode;
import com.juvarya.user.access.mgmt.dto.request.BusinessCategoryRequest;
import com.juvarya.user.access.mgmt.model.BusinessCategoryModel;
import com.juvarya.user.access.mgmt.repository.BusinessCategoryRepository;
import com.juvarya.user.access.mgmt.services.BusinessCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BusinessCategoryServiceImpl implements BusinessCategoryService {

    @Autowired
    private BusinessCategoryRepository repository;

    @Override
    public BusinessCategoryModel createCategory(BusinessCategoryRequest request) {
        if (repository.existsByName(request.getName())) {
            throw new JuvaryaCustomerException( ErrorCode.ALREADY_EXISTS);
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
                .orElseThrow(() -> new JuvaryaCustomerException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    @Override
    public List<BusinessCategoryModel> searchByName(String keyword) {
        return repository.findByNameContainingIgnoreCase(keyword);
    }

    @Override
    public BusinessCategoryModel getByName(String name) {
        return repository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new JuvaryaCustomerException(ErrorCode.CATEGORY_NOT_FOUND));
    }


}