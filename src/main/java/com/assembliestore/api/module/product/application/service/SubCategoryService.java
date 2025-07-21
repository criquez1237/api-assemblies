package com.assembliestore.api.module.product.application.service;

import com.assembliestore.api.module.product.application.port.SubCategoryPort;
import com.assembliestore.api.module.product.domain.entity.SubCategory;
import com.assembliestore.api.module.product.domain.repository.SubCategoryRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SubCategoryService implements SubCategoryPort {
    private final SubCategoryRepository subCategoryRepository;

    public SubCategoryService(SubCategoryRepository subCategoryRepository) {
        this.subCategoryRepository = subCategoryRepository;
    }

    @Override
    public void saveSubCategory(SubCategory subCategory) {
        subCategoryRepository.save(subCategory);
    }

    @Override
    public void updateSubCategory(SubCategory subCategory) {
        subCategoryRepository.update(subCategory);
    }

    @Override
    public void deleteSubCategory(String subCategoryId) {
        subCategoryRepository.delete(subCategoryId);
    }

    @Override
    public Optional<SubCategory> findSubCategoryById(String subCategoryId) {
        return subCategoryRepository.findById(subCategoryId);
    }

    @Override
    public List<SubCategory> findAllSubCategories() {
        return subCategoryRepository.findAll();
    }

    @Override
    public void toggleActiveSubCategory(String subCategoryId) {
        subCategoryRepository.toggleActiveSubCategory(subCategoryId);
    }

    @Override
    public void toggleVisibleSubCategory(String subCategoryId) {
        subCategoryRepository.toggleVisibleSubCategory(subCategoryId);
    }
}
