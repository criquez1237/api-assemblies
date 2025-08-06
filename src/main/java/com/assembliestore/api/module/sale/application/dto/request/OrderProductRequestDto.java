package com.assembliestore.api.module.sale.application.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderProductRequestDto {
    private String productId;
    private String name;
    private BigDecimal unitPrice;
    private Integer quantity;
}
