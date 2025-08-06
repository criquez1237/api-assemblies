package com.assembliestore.api.module.product.infrastructure.adapter.mapper;

import com.assembliestore.api.module.product.application.command.CategorySaveCommand;
import com.assembliestore.api.module.product.application.dto.CategoryFindDto;
import com.assembliestore.api.module.product.infrastructure.adapter.dto.CategoryRequestDto;
import com.assembliestore.api.module.product.infrastructure.adapter.dto.CategoryResponseDto;

public class CategoryMapperInfras {

    public static CategorySaveCommand toCategorySaveCommand(CategoryRequestDto categoryRequestDto) {
        return new CategorySaveCommand(
            categoryRequestDto.id(),
            categoryRequestDto.name(),
            categoryRequestDto.description(),
            categoryRequestDto.imageUrl(),
            categoryRequestDto.subCategories(), // <-- usa la lista
            categoryRequestDto.actived(),
            categoryRequestDto.visible()
        );
    }

    public static CategoryResponseDto toCategoryResponseDto(CategoryFindDto categoryFindDto) {
        return new CategoryResponseDto(
            categoryFindDto.id(),
            categoryFindDto.name(),
            categoryFindDto.description(),
            categoryFindDto.imageUrl(),
            categoryFindDto.subCategories(), // <-- usa la lista
            categoryFindDto.actived(),
            categoryFindDto.visible(),
            categoryFindDto.deleted(),
            categoryFindDto.createdAt(),
            categoryFindDto.updatedAt(),
            categoryFindDto.deletedAt()
        );
    }

}
