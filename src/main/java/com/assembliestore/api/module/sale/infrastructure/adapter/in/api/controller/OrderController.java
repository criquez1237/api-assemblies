package com.assembliestore.api.module.sale.infrastructure.adapter.in.api.controller;

import com.assembliestore.api.common.response.ApiResponse;
import com.assembliestore.api.module.sale.application.dto.request.CreateOrderRequestDto;
import com.assembliestore.api.module.sale.application.dto.request.UpdateOrderStatusRequestDto;
import com.assembliestore.api.module.sale.application.dto.response.OrderResponseDto;
import com.assembliestore.api.module.sale.application.mapper.OrderMapper;
import com.assembliestore.api.module.sale.application.service.OrderService;
import com.assembliestore.api.module.sale.domain.entity.Order;
import com.assembliestore.api.module.sale.domain.entity.OrderStatus;
import com.assembliestore.api.module.user.application.port.TokenPort;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private TokenPort tokenPort;

    /**
     * Extrae el userId del token JWT
     */
    private String getUserIdFromToken(HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        
        final String jwtToken = authHeader.substring(7);
        var jwtTokenDto = tokenPort.findByToken(jwtToken);
        
        if (jwtTokenDto == null) {
            throw new RuntimeException("Invalid token");
        }
        
        return jwtTokenDto.getUserId();
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN') or hasRole('MANAGEMENT')")
    public ResponseEntity<ApiResponse<Object>> createOrder(
            @Valid @RequestBody CreateOrderRequestDto createOrderDto,
            HttpServletRequest request) {
        try {
            // Extract userId from token instead of request body
            String userId = getUserIdFromToken(request);
            Order order = orderMapper.toEntity(createOrderDto, userId);
            var response = orderService.createOrder(order);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Order created successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error creating order: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN') or hasRole('MANAGEMENT')")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrderById(
            @Parameter(description = "ID de la orden") @PathVariable String id) {
        
        Optional<Order> order = orderService.findOrderById(id);
        if (order.isPresent()) {
            OrderResponseDto response = orderMapper.toResponseDto(order.get());
            return ResponseEntity.ok(ApiResponse.success("Orden encontrada", response));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Orden no encontrada"));
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGEMENT')")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getAllOrders() {
        
        List<Order> orders = orderService.findAllOrders();
        List<OrderResponseDto> response = orderMapper.toResponseDtoList(orders);
        
        return ResponseEntity.ok(ApiResponse.success("Órdenes obtenidas exitosamente", response));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN') or hasRole('MANAGEMENT')")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getOrdersByUserId(
            @Parameter(description = "ID del usuario") @PathVariable String userId) {
        
        List<Order> orders = orderService.findOrdersByUserId(userId);
        List<OrderResponseDto> response = orderMapper.toResponseDtoList(orders);
        
        return ResponseEntity.ok(ApiResponse.success("Órdenes del usuario obtenidas", response));
    }

    @GetMapping("/status/{status}")

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGEMENT')")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getOrdersByStatus(
            @Parameter(description = "Order status") @PathVariable String status) {
        
        try {
            OrderStatus orderStatus = OrderStatus.fromValue(status);
            List<Order> orders = orderService.findOrdersByStatus(orderStatus);
            List<OrderResponseDto> response = orderMapper.toResponseDtoList(orders);
            
            return ResponseEntity.ok(ApiResponse.success("Orders by status retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid status: " + status));
        }
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGEMENT')")
    public ResponseEntity<ApiResponse<OrderResponseDto>> updateOrderStatus(
            @Parameter(description = "ID de la orden") @PathVariable String id,
            @Valid @RequestBody UpdateOrderStatusRequestDto statusDto) {
        
        try {
            OrderStatus newStatus = OrderStatus.fromValue(statusDto.getStatus());
            Order updatedOrder = orderService.updateOrderStatus(id, newStatus);
            OrderResponseDto response = orderMapper.toResponseDto(updatedOrder);
            
            return ResponseEntity.ok(ApiResponse.success("Order status updated", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error updating status: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(
            @Parameter(description = "ID de la orden") @PathVariable String id) {
        
        try {
            orderService.deleteOrder(id);
            return ResponseEntity.ok(ApiResponse.success("Orden eliminada exitosamente", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error al eliminar orden: " + e.getMessage()));
        }
    }
}
