package com.assembliestore.api.module.product.infrastructure.adapter.dto;

import java.util.Date;
import java.util.List;

import com.assembliestore.api.common.type.FileFormat;
import com.assembliestore.api.module.product.domain.entity.SubCategory;

public record CategoryResponseDto(
        String id,
        String name,
        String description,
        FileFormat imageUrl,
        List<SubCategory> subCategories,
        boolean actived,
        boolean visible,
        boolean deleted,
        Date createdAt,
        Date updatedAt,
        Date deletedAt) {

}
