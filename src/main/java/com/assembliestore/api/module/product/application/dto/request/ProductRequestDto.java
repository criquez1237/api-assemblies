package com.assembliestore.api.module.product.application.dto.request;

import com.assembliestore.api.module.product.domain.entity.Gallery;
import com.assembliestore.api.module.product.domain.entity.Specification;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequestDto {
    private String id;
    private String name;
    private String description;
    private List<Specification> specifications;
    private String brandName;
    private BigDecimal price;
    private List<Gallery> gallery;
    private String subCategoryId;
    private Integer stockQuantity;
    private Boolean actived;
    private Boolean visible;
}
