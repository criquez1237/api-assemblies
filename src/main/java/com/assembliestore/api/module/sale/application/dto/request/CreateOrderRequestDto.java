package com.assembliestore.api.module.sale.application.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Data
public class CreateOrderRequestDto {
    
    @NotNull(message = "Product list is required")
    @NotEmpty(message = "Product list cannot be empty")
    private List<OrderProductRequestDto> products;
    
    @NotNull(message = "Shipping address is required")
    private ShippingAddressRequestDto shippingAddress;
    
    @NotNull(message = "Payment method is required")
    private String paymentMethod;
}
