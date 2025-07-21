package com.assembliestore.api.module.product.application.dto;

import java.util.Date;
import java.util.List;

import com.assembliestore.api.common.type.FileFormat;
import com.assembliestore.api.module.product.domain.entity.SubCategory;

public record CategoryFindDto(

        String id,
        String name,
        String description,
        FileFormat imageUrl,
        List<SubCategory> subCategories,
        boolean actived,
        boolean deleted,
        boolean visible,
        Date createdAt,
        Date updatedAt,
        Date deletedAt
) {

}
