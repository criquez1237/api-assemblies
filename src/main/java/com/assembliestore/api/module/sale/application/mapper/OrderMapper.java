package com.assembliestore.api.module.sale.application.mapper;

import com.assembliestore.api.module.sale.application.dto.request.CreateOrderRequestDto;
import com.assembliestore.api.module.sale.application.dto.request.OrderProductRequestDto;
import com.assembliestore.api.module.sale.application.dto.request.ShippingAddressRequestDto;
import com.assembliestore.api.module.sale.application.dto.response.OrderProductResponseDto;
import com.assembliestore.api.module.sale.application.dto.response.OrderResponseDto;
import com.assembliestore.api.module.sale.application.dto.response.ShippingAddressResponseDto;
import com.assembliestore.api.module.sale.domain.entity.Order;
import com.assembliestore.api.module.sale.domain.entity.OrderProduct;
import com.assembliestore.api.module.sale.domain.entity.PaymentMethod;
import com.assembliestore.api.module.sale.domain.entity.ShippingAddress;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public Order toEntity(CreateOrderRequestDto dto, String userId) {
        if (dto == null) return null;

        List<OrderProduct> products = dto.getProducts() != null ?
                dto.getProducts().stream()
                        .map(this::toOrderProductEntity)
                        .toList() : null;

        Order order = Order.builder()
                .userId(userId)
                .products(products)
                .shippingAddress(toShippingAddressEntity(dto.getShippingAddress()))
                .paymentMethod(PaymentMethod.fromValue(dto.getPaymentMethod()))
                .build();

        // Calcular total automáticamente
        order.calculateTotal();
        
        return order;
    }

    @Deprecated
    public Order toEntity(CreateOrderRequestDto dto) {
        throw new UnsupportedOperationException("Use toEntity(CreateOrderRequestDto dto, String userId) instead");
    }

    public OrderResponseDto toResponseDto(Order order) {
        if (order == null) return null;

        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setTotal(order.getTotal());
        dto.setStatus(order.getStatus().getValue());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatusUpdateDate(order.getStatusUpdateDate());
        dto.setShippingAddress(toShippingAddressResponseDto(order.getShippingAddress()));
        dto.setPaymentMethod(order.getPaymentMethod().getValue());
        if (order.getProducts() != null) {
            List<OrderProductResponseDto> products = order.getProducts().stream()
                    .map(this::toOrderProductResponseDto)
                    .toList();
            dto.setProducts(products);
        }

        return dto;
    }

    public List<OrderResponseDto> toResponseDtoList(List<Order> orders) {
        return orders.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    private OrderProduct toOrderProductEntity(OrderProductRequestDto dto) {
        if (dto == null) return null;

        return OrderProduct.builder()
                .productId(dto.getProductId())
                .name(dto.getName())
                .unitPrice(dto.getUnitPrice())
                .quantity(dto.getQuantity())
                .build();
    }

    private ShippingAddress toShippingAddressEntity(ShippingAddressRequestDto dto) {
        if (dto == null) return null;

        return ShippingAddress.builder()
                .street(dto.getStreet())
                .city(dto.getCity())
                .country(dto.getCountry())
                .postalCode(dto.getPostalCode())
                .build();
    }

    private OrderProductResponseDto toOrderProductResponseDto(OrderProduct orderProduct) {
        if (orderProduct == null) return null;

        OrderProductResponseDto dto = new OrderProductResponseDto();
        dto.setProductId(orderProduct.getProductId());
        dto.setName(orderProduct.getName());
        dto.setUnitPrice(orderProduct.getUnitPrice());
        dto.setQuantity(orderProduct.getQuantity());
        dto.setSubtotal(orderProduct.getSubtotal());

        return dto;
    }

    private ShippingAddressResponseDto toShippingAddressResponseDto(ShippingAddress address) {
        if (address == null) return null;

        ShippingAddressResponseDto dto = new ShippingAddressResponseDto();
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setCountry(address.getCountry());
        dto.setPostalCode(address.getPostalCode());

        return dto;
    }
}
