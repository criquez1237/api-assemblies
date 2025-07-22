package com.assembliestore.api.module.product.application.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ProductBatchRequestDto {
    private List<ProductRequestDto> products;
}
