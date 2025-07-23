package com.assembliestore.api.module.product.application.controller;

import com.assembliestore.api.module.product.domain.port.StockPort;
import com.assembliestore.api.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products/stock")
@CrossOrigin(originPatterns = "*")
public class StockController {

    @Autowired
    private StockPort stockPort;

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<Integer>> getProductStock(@PathVariable String productId) {
        try {
            int stock = stockPort.getCurrentStock(productId);
            
            if (stock == -1) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(ApiResponse.success("Stock retrieved successfully", stock));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error retrieving stock: " + e.getMessage()));
        }
    }

    @PostMapping("/check")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getMultipleProductsStock(@RequestBody List<String> productIds) {
        try {
            Map<String, Integer> stockMap = stockPort.getCurrentStock(productIds);
            return ResponseEntity.ok(ApiResponse.success("Stock retrieved successfully", stockMap));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error retrieving stock: " + e.getMessage()));
        }
    }

    @PostMapping("/availability")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkStockAvailability(@RequestBody Map<String, Integer> productQuantities) {
        try {
            Map<String, Boolean> availability = stockPort.checkStockAvailability(productQuantities);
            return ResponseEntity.ok(ApiResponse.success("Stock availability checked successfully", availability));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error checking stock availability: " + e.getMessage()));
        }
    }
}
