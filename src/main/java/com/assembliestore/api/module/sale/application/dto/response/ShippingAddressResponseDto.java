package com.assembliestore.api.module.sale.application.dto.response;

import lombok.Data;

@Data
public class ShippingAddressResponseDto {
    private String street;
    private String city;
    private String country;
    private String postalCode;
}
