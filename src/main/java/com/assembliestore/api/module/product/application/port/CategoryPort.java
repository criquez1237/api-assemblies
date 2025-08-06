package com.assembliestore.api.module.product.application.port;

import java.util.Optional;

import com.assembliestore.api.module.product.application.command.CategorySaveCommand;
import com.assembliestore.api.module.product.application.dto.CategoryFindDto;

public interface CategoryPort {

    void saveCategory(CategorySaveCommand category);

    void updateCategory(CategorySaveCommand category);
    
    void deleteCategory(String categoryId);

    Optional<CategoryFindDto> findCategoryById(String categoryId);

    Optional<CategoryFindDto> findCategoryByName(String categoryName);

    Iterable<CategoryFindDto> findAllCategories();

    void toggleActiveCategory(String categoryId);

    void toggleVisibleCategory(String categoryId);

}
