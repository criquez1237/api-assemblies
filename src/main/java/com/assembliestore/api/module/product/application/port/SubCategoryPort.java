package com.assembliestore.api.module.product.application.port;

import com.assembliestore.api.module.product.domain.entity.SubCategory;
import java.util.List;
import java.util.Optional;

public interface SubCategoryPort {
    void saveSubCategory(SubCategory subCategory);
    void updateSubCategory(SubCategory subCategory);
    void deleteSubCategory(String subCategoryId);
    Optional<SubCategory> findSubCategoryById(String subCategoryId);
    List<SubCategory> findAllSubCategories();
    void toggleActiveSubCategory(String subCategoryId);
    void toggleVisibleSubCategory(String subCategoryId);
}
