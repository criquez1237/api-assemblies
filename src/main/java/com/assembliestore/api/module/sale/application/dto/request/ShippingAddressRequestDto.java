package com.assembliestore.api.module.sale.application.dto.request;

import lombok.Data;

@Data
public class ShippingAddressRequestDto {
    private String street;
    private String city;
    private String country;
    private String postalCode;
}
