package com.assembliestore.api.module.product.application.dto;

import com.assembliestore.api.common.type.FileFormat;
import java.util.Date;

public record SubCategoryFindDto(
    String id,
    String name,
    String description,
    FileFormat imageUrl,
    boolean actived,
    boolean deleted,
    boolean visible,
    Date createdAt,
    Date updatedAt,
    Date deletedAt,
    String categoryId
) {}
