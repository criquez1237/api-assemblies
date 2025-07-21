package com.assembliestore.api.module.product.infrastructure.adapter.dto;

import com.assembliestore.api.common.type.FileFormat;
import com.assembliestore.api.module.product.domain.entity.SubCategory;

import java.util.List;

public record CategoryRequestDto(

        String id,
        String name,
        String description,
        FileFormat imageUrl,
        List<SubCategory> subCategories,
        boolean actived,
        boolean visible) {
}
