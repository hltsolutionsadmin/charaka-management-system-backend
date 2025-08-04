package com.hlt.usermanagement.controllers;

import com.hlt.usermanagement.dto.BusinessCategoryDTO;
import com.hlt.usermanagement.dto.request.BusinessCategoryRequest;
import com.hlt.usermanagement.model.BusinessCategoryModel;
import com.hlt.usermanagement.services.BusinessCategoryService;
import com.hlt.utils.JuavaryaConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/businessCategory")
@RequiredArgsConstructor
@Slf4j
public class BusinessCategoryController {

    private final BusinessCategoryService businessCategoryService;

    @PostMapping("/add")
    @PreAuthorize(JuavaryaConstants.ROLE_SUPER_ADMIN)
    public ResponseEntity<?> addCategory(@Valid @RequestBody BusinessCategoryRequest request) {
        BusinessCategoryModel createdCategory = businessCategoryService.createCategory(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Category '" + createdCategory.getName() + "' added successfully.");
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize(JuavaryaConstants.ROLE_SUPER_ADMIN)
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        businessCategoryService.deleteById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Category deleted successfully with ID: " + id);
    }

    @GetMapping("/list")
    public ResponseEntity<List<BusinessCategoryModel>> getAllCategories() {
        List<BusinessCategoryModel> categories = businessCategoryService.listAll();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusinessCategoryDTO> getCategoryById(@PathVariable Long id) {
        BusinessCategoryModel category = businessCategoryService.getById(id);
        BusinessCategoryDTO dto = new BusinessCategoryDTO();
        BeanUtils.copyProperties(category, dto);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("by/{name}")
    public ResponseEntity<BusinessCategoryDTO> getCategoryByName(@PathVariable String name) {
        BusinessCategoryModel category = businessCategoryService.getByName(name.trim().toLowerCase());
        BusinessCategoryDTO dto = new BusinessCategoryDTO();
        BeanUtils.copyProperties(category, dto);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/search")
    public ResponseEntity<List<BusinessCategoryDTO>> searchCategories(@RequestParam String query) {
        List<BusinessCategoryModel> results = businessCategoryService.searchByName(query.trim().toLowerCase());

        List<BusinessCategoryDTO> dtoList = results.stream().map(model -> {
            BusinessCategoryDTO dto = new BusinessCategoryDTO();
            BeanUtils.copyProperties(model, dto);
            return dto;
        }).toList();

        return ResponseEntity.ok(dtoList);
    }

}
