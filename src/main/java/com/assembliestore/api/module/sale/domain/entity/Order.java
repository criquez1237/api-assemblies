package com.assembliestore.api.module.sale.domain.entity;

import com.google.cloud.firestore.annotation.ServerTimestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    private String id;
    private String userId;
    private List<OrderProduct> products;
    private BigDecimal total;
    private OrderStatus status;
    @ServerTimestamp
    private Date orderDate;
    @ServerTimestamp
    private Date statusUpdateDate;
    private ShippingAddress shippingAddress;
    private PaymentMethod paymentMethod;
    @Builder.Default
    private boolean deleted = false;
    private Date deletedAt;

    public void refreshStatusUpdateDate() {
        this.statusUpdateDate = new Date();
    }

    public void updateStatus(OrderStatus newStatus) {
        this.status = newStatus;
        refreshStatusUpdateDate();
    }

    public void calculateTotal() {
        if (products != null && !products.isEmpty()) {
            this.total = products.stream()
                    .map(OrderProduct::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            this.total = BigDecimal.ZERO;
        }
    }
}
