package com.assembliestore.api.module.product.domain.repository;

import com.assembliestore.api.module.product.domain.entity.Product;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    void upsert(Product product);
    void upsertBatch(List<Product> products);
    Optional<Product> findById(String productId);
    List<Product> findAll();
    List<Product> findAllForClient();
    List<Product> findAllForManagement();
}
