package com.assembliestore.api.module.product.application.command;

import com.assembliestore.api.common.type.FileFormat;
import com.assembliestore.api.module.product.domain.entity.SubCategory;

import java.util.List;

public record CategorySaveCommand(
         String id,
         String name,
         String description,
         FileFormat imageUrl,
         List<SubCategory> subCategories,
         boolean actived,
         boolean visible
) {

}
