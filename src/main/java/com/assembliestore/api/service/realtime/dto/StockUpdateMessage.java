package com.assembliestore.api.service.realtime.dto;

public class StockUpdateMessage {
    
    private String productId;
    private String productName;
    private Integer previousStock;
    private Integer currentStock;
    private Integer stockChange;
    private String changeType;    // INCREASE, DECREASE, RESTOCK, OUT_OF_STOCK
    private String reason;        // SALE, PURCHASE, ADJUSTMENT, RETURN
    private Long timestamp;

    public StockUpdateMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    public StockUpdateMessage(String productId, String productName, Integer previousStock, Integer currentStock) {
        this();
        this.productId = productId;
        this.productName = productName;
        this.previousStock = previousStock;
        this.currentStock = currentStock;
        this.stockChange = currentStock - previousStock;
        this.changeType = determineChangeType(previousStock, currentStock);
    }

    private String determineChangeType(Integer previous, Integer current) {
        if (previous == 0 && current > 0) {
            return "RESTOCK";
        } else if (previous > 0 && current == 0) {
            return "OUT_OF_STOCK";
        } else if (current > previous) {
            return "INCREASE";
        } else if (current < previous) {
            return "DECREASE";
        }
        return "NO_CHANGE";
    }

    // Getters y Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getPreviousStock() {
        return previousStock;
    }

    public void setPreviousStock(Integer previousStock) {
        this.previousStock = previousStock;
    }

    public Integer getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
    }

    public Integer getStockChange() {
        return stockChange;
    }

    public void setStockChange(Integer stockChange) {
        this.stockChange = stockChange;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "StockUpdateMessage{" +
                "productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", previousStock=" + previousStock +
                ", currentStock=" + currentStock +
                ", stockChange=" + stockChange +
                ", changeType='" + changeType + '\'' +
                ", reason='" + reason + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
