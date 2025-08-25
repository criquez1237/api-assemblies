package com.assembliestore.api.module.cart.infrastructure.adapter.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartItemResponse {
    private String productId;
    private String id;
    private String name;
    private BigDecimal unitPrice;
    private String description;
    private List<String> gallery;
    private int quantity;
    private BigDecimal subtotal;

    public CartItemResponse() {}

    public CartItemResponse(String productId, String name, BigDecimal unitPrice, String description, List<String> gallery, int quantity, BigDecimal subtotal) {
        this.productId = productId;
        this.name = name;
        this.unitPrice = unitPrice;
        this.description = description;
        this.gallery = gallery;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    public CartItemResponse(String id, String productId, String name, BigDecimal unitPrice, String description, List<String> gallery, int quantity, BigDecimal subtotal) {
        this.id = id;
        this.productId = productId;
        this.name = name;
        this.unitPrice = unitPrice;
        this.description = description;
        this.gallery = gallery;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getGallery() { return gallery; }
    public void setGallery(List<String> gallery) { this.gallery = gallery; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
