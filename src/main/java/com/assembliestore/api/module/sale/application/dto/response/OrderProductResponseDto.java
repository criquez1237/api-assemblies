package com.assembliestore.api.module.sale.application.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderProductResponseDto {
    private String productId;
    private String name;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
}
