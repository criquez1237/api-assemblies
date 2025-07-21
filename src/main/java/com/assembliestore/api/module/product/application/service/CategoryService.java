package com.assembliestore.api.module.product.application.service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;

import com.assembliestore.api.module.product.application.command.CategorySaveCommand;
import com.assembliestore.api.module.product.application.dto.CategoryFindDto;
import com.assembliestore.api.module.product.application.mapper.CategoryMapper;
import com.assembliestore.api.module.product.application.port.CategoryPort;
import com.assembliestore.api.module.product.domain.entity.Category;
import com.assembliestore.api.module.product.domain.repository.CategoryRepository;

@Service
public class CategoryService implements CategoryPort {

    private final CategoryRepository _categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this._categoryRepository = categoryRepository;
    }

    @Override
    public void saveCategory(CategorySaveCommand category) {

        Optional<CategoryFindDto> existingCategory = findCategoryByName(category.name());
        if (existingCategory.isPresent()) {
            throw new IllegalArgumentException("Categoria ya existente con el nombre: " + category.name());
        }

        Category categoryEntity = CategoryMapper.toEntity(category);

        categoryEntity.setId(UUID.randomUUID().toString());
        categoryEntity.setCreatedAt(new Date());
        categoryEntity.setUpdatedAt(new Date());

        _categoryRepository.save(categoryEntity);

    }

    @Override
    public void updateCategory(CategorySaveCommand category) {

        Optional<Category> existingCategory = _categoryRepository.findById(category.id());
        if (existingCategory.isEmpty()) {
            throw new IllegalArgumentException("Categoria no encontrada con el id: " + category.id());
        }


        Optional<Category> existingCategoryByName = _categoryRepository.findByName(category.name());
        if (existingCategoryByName.isPresent() && !existingCategoryByName.get().getId().equals(category.id())) {
            throw new IllegalArgumentException("Ya existe una categoria con el nombre: " + category.name());
        }

        Category categoryEntity = existingCategoryByName.get();
        categoryEntity.refreshUpdatedAt();


        _categoryRepository.update(categoryEntity);
    }

    @Override
    public void deleteCategory(String categoryId) {

        Optional<Category> existingCategory = _categoryRepository.findById(categoryId);
        if (existingCategory.isEmpty() || existingCategory.get().isDeleted()) {
            throw new IllegalArgumentException("Categoria no encontrada con el id: " + categoryId);
        }

        Category categoryEntity = existingCategory.get();
        categoryEntity.setVisible(false);
        categoryEntity.setActived(false);
        categoryEntity.markAsDeleted();

        _categoryRepository.update(categoryEntity);
    }

    @Override
    public Optional<CategoryFindDto> findCategoryById(String categoryId) {

        Optional<Category> existingCategory = _categoryRepository.findById(categoryId);
        if (existingCategory.isEmpty() || existingCategory.get().isDeleted()) {
            throw new IllegalArgumentException("Categoria no encontrada con el id: " + categoryId);
        }

        return Optional.of(CategoryMapper.toDto(existingCategory.get()));
    }

    @Override
    public Iterable<CategoryFindDto> findAllCategories() {

        Iterable<Category> categories = _categoryRepository.findAll();

        return StreamSupport.stream(categories.spliterator(), false)
                .map(CategoryMapper::toDto)
                .toList();
    }

    @Override
    public void toggleActiveCategory(String categoryId) {

        Optional<Category> existingCategory = _categoryRepository.findById(categoryId);
        if (existingCategory.isEmpty() || existingCategory.get().isDeleted()) {
            throw new IllegalArgumentException("Categoria no encontrada con el id: " + categoryId);
        }

        Category categoryEntity = existingCategory.get();
        categoryEntity.markToogleActive();
        if(categoryEntity.isActived()) {
            categoryEntity.setVisible(true);
        } else {
            categoryEntity.setVisible(false);
        }

        _categoryRepository.update(categoryEntity);
    }

    @Override
    public void toggleVisibleCategory(String categoryId) {
        Optional<Category> existingCategory = _categoryRepository.findById(categoryId);
        if (existingCategory.isEmpty() || existingCategory.get().isDeleted() || !existingCategory.get().isActived()) {
            throw new IllegalArgumentException("Categoria no encontrada con el id: " + categoryId);
        }

        Category categoryEntity = existingCategory.get();
        categoryEntity.markToogleVisible();

        _categoryRepository.update(categoryEntity);
    }

    @Override
    public Optional<CategoryFindDto> findCategoryByName(String categoryName) {

        Optional<Category> existingCategory = _categoryRepository.findByName(categoryName);
        if (existingCategory.isEmpty() || existingCategory.get().isDeleted()) {
            return Optional.empty();
        }

        return Optional.of(CategoryMapper.toDto(existingCategory.get()));
    }

}
