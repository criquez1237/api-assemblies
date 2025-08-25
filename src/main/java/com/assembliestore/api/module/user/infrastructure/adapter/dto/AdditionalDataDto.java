package com.assembliestore.api.module.user.infrastructure.adapter.dto;

public class AdditionalDataDto {
    private Integer totalItemCart;

    public AdditionalDataDto() {}

    public AdditionalDataDto(Integer totalItemCart) {
        this.totalItemCart = totalItemCart;
    }

    public Integer getTotalItemCart() { return totalItemCart; }
    public void setTotalItemCart(Integer totalItemCart) { this.totalItemCart = totalItemCart; }
}
