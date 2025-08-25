package com.assembliestore.api.module.cart.infrastructure.adapter.in.api.controller;

import com.assembliestore.api.common.response.ApiErrorResponse;
import com.assembliestore.api.common.response.ApiSuccessResponse;
import com.assembliestore.api.common.response.ErrorDetail;
import com.assembliestore.api.common.response.ResponseUtil;
import com.assembliestore.api.common.response.TechnicalDetails;
import com.assembliestore.api.config.AppEnvConfig;
import com.assembliestore.api.module.cart.application.service.CartService;
import com.assembliestore.api.module.cart.infrastructure.adapter.dto.CartItemRequest;
import com.assembliestore.api.module.cart.infrastructure.adapter.dto.CartResponse;
import com.assembliestore.api.module.user.application.port.TokenPort;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private TokenPort tokenPort;

    @Autowired
    private AppEnvConfig appEnvConfig;

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

    @GetMapping
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN') or hasRole('MANAGEMENT')")
    public ResponseEntity<?> getCart(HttpServletRequest request,
                                     @RequestParam(name = "page", required = false) Integer page,
                                     @RequestParam(name = "limit", required = false) Integer limit,
                                     @RequestParam(name = "name", required = false) String name,
                                     @RequestParam(name = "unitPrice", required = false) java.math.BigDecimal unitPrice) {
        long start = System.currentTimeMillis();
        try {
            String userId = getUserIdFromToken(request);
            CartResponse cart = cartService.getCart(userId, page, limit, name, unitPrice);

            TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(request, System.currentTimeMillis() - start, appEnvConfig);
            ApiSuccessResponse<CartResponse> resp = new ApiSuccessResponse<>("Carrito obtenido", "CART_GET_SUCCESS", cart, tech);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(request, System.currentTimeMillis() - start, appEnvConfig);
            ApiErrorResponse error = new ApiErrorResponse("Error obteniendo carrito", "CART_GET_ERROR",
                    java.util.Arrays.asList(new ErrorDetail("cart", e.getMessage())), tech);
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN') or hasRole('MANAGEMENT')")
    public ResponseEntity<?> addItem(@Valid @RequestBody CartItemRequest requestBody, HttpServletRequest request) {
        long start = System.currentTimeMillis();
        try {
            String userId = getUserIdFromToken(request);
            CartResponse cart = cartService.addItem(userId, requestBody);

            TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(request, System.currentTimeMillis() - start, appEnvConfig);
            ApiSuccessResponse<CartResponse> resp = new ApiSuccessResponse<>("Item agregado al carrito", "CART_ADD_ITEM_SUCCESS", cart, tech);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(request, System.currentTimeMillis() - start, appEnvConfig);
            ApiErrorResponse error = new ApiErrorResponse("Error agregando item", "CART_ADD_ITEM_ERROR",
                    java.util.Arrays.asList(new ErrorDetail("add", e.getMessage())), tech);
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PatchMapping("/items/{itemId}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN') or hasRole('MANAGEMENT')")
    public ResponseEntity<?> updateQuantity(@PathVariable String itemId, @RequestParam int quantity, HttpServletRequest request) {
        long start = System.currentTimeMillis();
        try {
            String userId = getUserIdFromToken(request);
            CartResponse cart = cartService.updateQuantity(userId, itemId, quantity);

            TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(request, System.currentTimeMillis() - start, appEnvConfig);
            ApiSuccessResponse<CartResponse> resp = new ApiSuccessResponse<>("Cantidad actualizada", "CART_UPDATE_QTY_SUCCESS", cart, tech);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(request, System.currentTimeMillis() - start, appEnvConfig);
            ApiErrorResponse error = new ApiErrorResponse("Error actualizando cantidad", "CART_UPDATE_QTY_ERROR",
                    java.util.Arrays.asList(new ErrorDetail("update", e.getMessage())), tech);
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN') or hasRole('MANAGEMENT')")
    public ResponseEntity<?> removeItem(@PathVariable String itemId, HttpServletRequest request) {
        long start = System.currentTimeMillis();
        try {
            String userId = getUserIdFromToken(request);
            CartResponse cart = cartService.removeItem(userId, itemId);

            TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(request, System.currentTimeMillis() - start, appEnvConfig);
            ApiSuccessResponse<CartResponse> resp = new ApiSuccessResponse<>("Item removido", "CART_REMOVE_ITEM_SUCCESS", cart, tech);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(request, System.currentTimeMillis() - start, appEnvConfig);
            ApiErrorResponse error = new ApiErrorResponse("Error removiendo item", "CART_REMOVE_ITEM_ERROR",
                    java.util.Arrays.asList(new ErrorDetail("remove", e.getMessage())), tech);
            return ResponseEntity.badRequest().body(error);
        }
    }
}
