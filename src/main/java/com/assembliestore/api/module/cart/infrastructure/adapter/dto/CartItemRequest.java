package com.assembliestore.api.module.cart.infrastructure.adapter.dto;

import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CartItemRequest {
    @NotBlank
    private String productId;

    @NotBlank
    private String name;

    // Optional item identifier (generated if absent)
    private String id;

    @NotNull
    private BigDecimal unitPrice;

    private String description;
    private List<String> gallery;

    @Min(1)
    private int quantity = 1;

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
}
