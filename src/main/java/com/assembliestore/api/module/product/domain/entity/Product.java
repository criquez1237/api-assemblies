package com.assembliestore.api.module.product.domain.entity;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Product {

    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private String categoryId;
    private String brandId;
    private Double price;
    private Integer stockQuantity;
}
