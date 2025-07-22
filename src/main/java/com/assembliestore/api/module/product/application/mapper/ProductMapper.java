package com.assembliestore.api.module.product.application.mapper;

import com.assembliestore.api.module.product.application.dto.request.ProductRequestDto;
import com.assembliestore.api.module.product.application.dto.response.ProductResponseDto;
import com.assembliestore.api.module.product.domain.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequestDto dto) {
        if (dto == null) return null;
        Double price = dto.getPrice() != null ? dto.getPrice().setScale(2, java.math.RoundingMode.HALF_UP).doubleValue() : 0.0;
        return Product.builder()
            .id(dto.getId())
            .name(dto.getName())
            .description(dto.getDescription())
            .specifications(dto.getSpecifications())
            .brandName(dto.getBrandName())
            .price(price)
            .gallery(dto.getGallery())
            .subCategoryId(dto.getSubCategoryId())
            .stockQuantity(dto.getStockQuantity())
            .actived(dto.getActived() != null ? dto.getActived() : true)
            .visible(dto.getVisible() != null ? dto.getVisible() : true)
            .deleted(false)
            .build();
    }

    public ProductResponseDto toDto(Product entity) {
        if (entity == null) return null;
        Double price = entity.getPrice() != null ? Math.round(entity.getPrice() * 100.0) / 100.0 : 0.0;
        return ProductResponseDto.builder()
            .id(entity.getId())
            .name(entity.getName())
            .description(entity.getDescription())
            .specifications(entity.getSpecifications())
            .brandName(entity.getBrandName())
            .price(price)
            .gallery(entity.getGallery())
            .subCategoryId(entity.getSubCategoryId())
            .stockQuantity(entity.getStockQuantity())
            .actived(entity.isActived())
            .visible(entity.isVisible())
            .deleted(entity.isDeleted())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .deletedAt(entity.getDeletedAt())
            .build();
    }
}
