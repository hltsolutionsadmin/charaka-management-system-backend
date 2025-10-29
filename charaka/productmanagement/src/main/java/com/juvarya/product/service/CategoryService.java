package com.juvarya.product.service;


import com.juvarya.product.dto.CategoryDTO;

import java.util.List;

public interface CategoryService {
    CategoryDTO addOrUpdateCategory(CategoryDTO dto);

    void deleteCategory(Long id);
    List<CategoryDTO> getAllCategories();

}