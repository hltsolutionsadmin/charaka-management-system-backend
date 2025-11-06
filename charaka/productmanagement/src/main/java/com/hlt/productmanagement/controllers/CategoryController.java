package com.hlt.productmanagement.controllers;



import com.hlt.productmanagement.dto.CategoryDTO;
import com.hlt.productmanagement.dto.response.ApiResponse;
import com.hlt.productmanagement.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/categories")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CategoryDTO> createOrUpdateCategory(@ModelAttribute CategoryDTO dto) {
        log.info("Received request to add/update category: {}", dto.getName());
        log.info("Media files count: {}", dto.getMediaFiles() != null ? dto.getMediaFiles().size() : 0);
        CategoryDTO response = categoryService.addOrUpdateCategory(dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        log.info("Request received to delete category with ID: {}", id);
        categoryService.deleteCategory(id);

        ApiResponse<Void> response = new ApiResponse<>(
                "Category deleted successfully",
                null,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        log.info("Fetching all categories");
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

}
