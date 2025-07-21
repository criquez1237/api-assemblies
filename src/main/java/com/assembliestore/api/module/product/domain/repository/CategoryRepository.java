package com.assembliestore.api.module.product.domain.repository;

import java.util.Optional;
import com.assembliestore.api.module.product.domain.entity.Category;

public interface CategoryRepository {

    void save(Category category);

    void update(Category category);

    void delete(String categoryId);

    Optional<Category> findById(String categoryId);
    Optional<Category> findByName(String name);

    Iterable<Category> findAll();

    void markToogleActive(String categoryId);

    void markToogleVisible(String categoryId);


    
}
