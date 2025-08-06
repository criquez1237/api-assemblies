package com.assembliestore.api.module.product.application.service;

import com.assembliestore.api.module.product.application.port.ProductPort;
import com.assembliestore.api.module.product.domain.entity.Product;
import com.assembliestore.api.module.product.domain.entity.Gallery;
import com.assembliestore.api.module.product.domain.entity.Specification;
import com.assembliestore.api.module.product.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService implements ProductPort {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void upsertProduct(Product product) {
        if (product == null) {
            throw new RuntimeException("El producto no puede ser nulo");
        }
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new RuntimeException("El nombre del producto es requerido");
        }
        productRepository.upsert(product);
    }

    @Override
    public void upsertProducts(List<Product> products) {
        if (products == null || products.isEmpty()) {
            throw new RuntimeException("La lista de productos no puede estar vacía");
        }
        
        // Validar cada producto
        for (Product product : products) {
            if (product == null) {
                throw new RuntimeException("Ningún producto en la lista puede ser nulo");
            }
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                throw new RuntimeException("El nombre es requerido para todos los productos");
            }
        }
        
        productRepository.upsertBatch(products);
    }

    @Override
    public Optional<Product> findProductById(String productId, String role) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new RuntimeException("El ID del producto es requerido");
        }
        
        Optional<Product> product = productRepository.findById(productId);
        
        if (product.isEmpty()) {
            return Optional.empty();
        }
        
        // Aplicar filtros según el rol
        Product foundProduct = product.get();
        
        switch (role.toUpperCase()) {
            case "CLIENT":
                // Cliente: solo productos activos, visibles y no borrados
                if (foundProduct.isDeleted() || !foundProduct.isActived() || !foundProduct.isVisible()) {
                    return Optional.empty();
                }
                return Optional.of(filterForClient(foundProduct));
                
            case "MANAGEMENT":
                // Management: todo menos los borrados
                if (foundProduct.isDeleted()) {
                    return Optional.empty();
                }
                return Optional.of(filterForManagement(foundProduct));
                
            case "ADMIN":
                // Admin: todo
                return product;
                
            default:
                throw new RuntimeException("Rol no válido: " + role);
        }
    }

    @Override
    public List<Product> findAllProducts(String role) {
        switch (role.toUpperCase()) {
            case "CLIENT":
                return productRepository.findAllForClient();
                
            case "MANAGEMENT":
                return productRepository.findAllForManagement();
                
            case "ADMIN":
                return productRepository.findAll();
                
            default:
                throw new RuntimeException("Rol no válido: " + role);
        }
    }

    @Override
    public void addGalleryImage(String productId, Gallery image) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        product.addGalleryImage(image);
        productRepository.upsert(product);
    }

    @Override
    public void removeGalleryImage(String productId, String imageId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        product.removeGalleryImage(imageId);
        productRepository.upsert(product);
    }

    @Override
    public void setGalleryImageState(String productId, String imageId, Boolean visible, Boolean actived, Boolean deleted) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        if (visible != null) product.setGalleryImageVisible(imageId, visible);
        if (actived != null) product.setGalleryImageActived(imageId, actived);
        if (deleted != null) product.setGalleryImageDeleted(imageId, deleted);
        productRepository.upsert(product);
    }

    @Override
    public void addSpecification(String productId, Specification spec) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        product.addSpecification(spec);
        productRepository.upsert(product);
    }

    @Override
    public void removeSpecification(String productId, String specId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        product.removeSpecification(specId);
        productRepository.upsert(product);
    }

    @Override
    public void setSpecificationState(String productId, String specId, Boolean visible, Boolean actived, Boolean deleted) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        if (visible != null) product.setSpecificationVisible(specId, visible);
        if (actived != null) product.setSpecificationActived(specId, actived);
        if (deleted != null) product.setSpecificationDeleted(specId, deleted);
        productRepository.upsert(product);
    }

    private Product filterForClient(Product product) {
        // Para clientes: solo datos básicos
        return Product.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .specifications(product.getSpecifications())
            .brandName(product.getBrandName())
            .price(product.getPrice())
            .gallery(product.getGallery())
            .build();
    }

    private Product filterForManagement(Product product) {
        // Para management: todo menos deletedAt
        return Product.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .specifications(product.getSpecifications())
            .brandName(product.getBrandName())
            .price(product.getPrice())
            .gallery(product.getGallery())
            .subCategoryId(product.getSubCategoryId())
            .stockQuantity(product.getStockQuantity())
            .actived(product.isActived())
            .visible(product.isVisible())
            .deleted(product.isDeleted())
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .build();
    }
}
