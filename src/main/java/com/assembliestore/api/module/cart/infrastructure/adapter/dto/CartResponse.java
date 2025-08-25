package com.assembliestore.api.module.cart.infrastructure.adapter.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartResponse {
    private List<CartItemResponse> items;
    private BigDecimal total;
    private int totalQuantity;
    private com.assembliestore.api.module.cart.infrastructure.adapter.dto.PaginationDto pagination;

    public CartResponse() {}

    public CartResponse(List<CartItemResponse> items, BigDecimal total) {
        this.items = items;
        this.total = total;
        int qty = 0;
        if (items != null) for (CartItemResponse it : items) qty += it.getQuantity();
        this.totalQuantity = qty;
    }

    public List<CartItemResponse> getItems() { return items; }
    public void setItems(List<CartItemResponse> items) { this.items = items; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public int getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }

    public com.assembliestore.api.module.cart.infrastructure.adapter.dto.PaginationDto getPagination() {
        return pagination;
    }

    public void setPagination(com.assembliestore.api.module.cart.infrastructure.adapter.dto.PaginationDto pagination) {
        this.pagination = pagination;
    }
}
