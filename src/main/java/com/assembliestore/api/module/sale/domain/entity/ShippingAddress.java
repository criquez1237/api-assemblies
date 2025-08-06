package com.assembliestore.api.module.sale.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddress {
    
    private String street;
    private String city;
    private String country;
    private String postalCode;
}
