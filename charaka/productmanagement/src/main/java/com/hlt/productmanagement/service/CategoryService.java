package com.hlt.productmanagement.service;



import com.hlt.productmanagement.dto.CategoryDTO;

import java.util.List;

public interface CategoryService {

    CategoryDTO addOrUpdateCategory(CategoryDTO dto);

    void deleteCategory(Long id);

    List<CategoryDTO> getAllCategories();

}