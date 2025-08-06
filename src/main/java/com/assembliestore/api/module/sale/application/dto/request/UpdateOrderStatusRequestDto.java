package com.assembliestore.api.module.sale.application.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class UpdateOrderStatusRequestDto {
    
    @NotNull(message = "Status is required")
    private String status;
}
