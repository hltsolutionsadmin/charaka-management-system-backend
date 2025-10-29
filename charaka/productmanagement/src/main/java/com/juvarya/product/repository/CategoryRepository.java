package com.juvarya.product.repository;



import com.juvarya.product.model.CategoryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryModel, Long> {
    Optional<CategoryModel> findByNameIgnoreCase(String name);
}