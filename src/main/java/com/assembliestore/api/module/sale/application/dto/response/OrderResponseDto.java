package com.assembliestore.api.module.sale.application.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class OrderResponseDto {
    private String id;
    private String userId;
    private List<OrderProductResponseDto> products;
    private BigDecimal total;
    private String status;
    private Date orderDate;
    private Date statusUpdateDate;
    private ShippingAddressResponseDto shippingAddress;
    private String paymentMethod;
}
