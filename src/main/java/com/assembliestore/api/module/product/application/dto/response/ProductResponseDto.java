package com.assembliestore.api.module.product.application.dto.response;

import com.assembliestore.api.module.product.domain.entity.Gallery;
import com.assembliestore.api.module.product.domain.entity.Specification;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class ProductResponseDto {
    private String id;
    private String name;
    private String description;
    private List<Specification> specifications;
    private String brandName;
    private Double price;
    private List<Gallery> gallery;
    private String subCategoryId;
    private Integer stockQuantity;
    private Boolean actived;
    private Boolean visible;
    private Boolean deleted;
    private Date createdAt;
    private Date updatedAt;
    private Date deletedAt;
}
