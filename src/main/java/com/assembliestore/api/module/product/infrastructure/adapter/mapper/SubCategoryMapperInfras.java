package com.assembliestore.api.module.product.infrastructure.adapter.mapper;

import com.assembliestore.api.module.product.domain.entity.SubCategory;
import com.assembliestore.api.module.product.infrastructure.adapter.dto.SubCategoryRequestDto;
import com.assembliestore.api.module.product.infrastructure.adapter.dto.SubCategoryResponseDto;

public class SubCategoryMapperInfras {
    public static SubCategory toEntity(SubCategoryRequestDto dto) {
        return SubCategory.builder()
            .id(dto.id())
            .name(dto.name())
            .description(dto.description())
            .imageUrl(dto.imageUrl())
            .actived(dto.actived())
            .deleted(dto.deleted())
            .visible(dto.visible())
            .createdAt(dto.createdAt())
            .updatedAt(dto.updatedAt())
            .deletedAt(dto.deletedAt())
            .categoryId(dto.categoryId())
            .build();
    }

    public static SubCategoryResponseDto toResponseDto(SubCategory entity) {
        return new SubCategoryResponseDto(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getImageUrl(),
            entity.isActived(),
            entity.isDeleted(),
            entity.isVisible(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getDeletedAt(),
            entity.getCategoryId()
        );
    }
}
