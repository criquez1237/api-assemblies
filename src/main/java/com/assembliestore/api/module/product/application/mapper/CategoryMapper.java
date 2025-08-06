package com.assembliestore.api.module.product.application.mapper;

import com.assembliestore.api.module.product.application.command.CategorySaveCommand;
import com.assembliestore.api.module.product.application.dto.CategoryFindDto;
import com.assembliestore.api.module.product.domain.entity.Category;

public class CategoryMapper {

    public static CategoryFindDto toDto(final Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryFindDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getImageUrl(),
                category.getSubCategories(),
                category.isActived(),
                category.isDeleted(),
                category.isVisible(),
                category.getCreatedAt(),
                category.getUpdatedAt(),
                category.getDeletedAt()
        );
    }

    public static Category toEntity(final CategorySaveCommand command) {
        if (command == null) {
            return null;
        }
        return Category.builder()
                .id(command.id())
                .name(command.name())
                .description(command.description())
                .imageUrl(command.imageUrl())
                .subCategories(command.subCategories())
                .actived(command.actived())
                .visible(command.visible())
                .build();
    }

}
