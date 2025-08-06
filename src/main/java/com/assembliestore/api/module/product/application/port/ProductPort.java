package com.assembliestore.api.module.product.application.port;

import com.assembliestore.api.module.product.domain.entity.Product;
import com.assembliestore.api.module.product.domain.entity.Gallery;
import com.assembliestore.api.module.product.domain.entity.Specification;
import java.util.List;
import java.util.Optional;

public interface ProductPort {
    void upsertProduct(Product product);
    void upsertProducts(List<Product> products);
    Optional<Product> findProductById(String productId, String role);
    List<Product> findAllProducts(String role);

    // Gallery
    void addGalleryImage(String productId, Gallery image);
    void removeGalleryImage(String productId, String imageId);
    void setGalleryImageState(String productId, String imageId, Boolean visible, Boolean actived, Boolean deleted);

    // Specification
    void addSpecification(String productId, Specification spec);
    void removeSpecification(String productId, String specId);
    void setSpecificationState(String productId, String specId, Boolean visible, Boolean actived, Boolean deleted);
}
