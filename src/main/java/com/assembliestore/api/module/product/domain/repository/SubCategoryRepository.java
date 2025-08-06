package com.assembliestore.api.module.product.domain.repository;

import com.assembliestore.api.module.product.domain.entity.SubCategory;
import java.util.List;
import java.util.Optional;

public interface SubCategoryRepository {
    void save(SubCategory subCategory);
    void update(SubCategory subCategory);
    void delete(String subCategoryId);
    Optional<SubCategory> findById(String subCategoryId);
    List<SubCategory> findAll();
    void toggleActiveSubCategory(String subCategoryId);
    void toggleVisibleSubCategory(String subCategoryId);
}
