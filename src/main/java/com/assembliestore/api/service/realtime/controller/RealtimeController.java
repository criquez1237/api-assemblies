package com.assembliestore.api.service.realtime.controller;

import com.assembliestore.api.service.realtime.dto.NotificationMessage;
import com.assembliestore.api.service.realtime.dto.StockUpdateMessage;
import com.assembliestore.api.service.realtime.service.RealtimeNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/realtime")
@Tag(name = "Realtime", description = "WebSocket and real-time notifications management")
public class RealtimeController {

    @Autowired
    private RealtimeNotificationService notificationService;

    @GetMapping("/stats")
    @Operation(summary = "Get WebSocket connection statistics", description = "Returns current WebSocket connection statistics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    @PreAuthorize("hasRole('MANAGEMENT') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getConnectionStats() {
        Map<String, Object> stats = notificationService.getConnectionStats();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/test-notification")
    @Operation(summary = "Send test notification", description = "Send a test notification through WebSocket")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PreAuthorize("hasRole('MANAGEMENT') or hasRole('ADMIN')")
    public ResponseEntity<String> sendTestNotification(@RequestBody NotificationMessage notification) {
        try {
            notificationService.sendNotification(notification);
            return ResponseEntity.ok("Notification sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error sending notification: " + e.getMessage());
        }
    }

    @PostMapping("/test-stock-update")
    @Operation(summary = "Send test stock update", description = "Send a test stock update through WebSocket")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock update sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PreAuthorize("hasRole('MANAGEMENT')")
    public ResponseEntity<String> sendTestStockUpdate(@RequestBody StockUpdateMessage stockUpdate) {
        try {
            notificationService.sendStockUpdate(stockUpdate);
            return ResponseEntity.ok("Stock update sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error sending stock update: " + e.getMessage());
        }
    }

    @PostMapping("/order-status/{orderId}")
    @Operation(summary = "Send order status update", description = "Send order status update notification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order status update sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PreAuthorize("hasRole('MANAGEMENT') or hasRole('ADMIN')")
    public ResponseEntity<String> sendOrderStatusUpdate(
            @PathVariable String orderId,
            @RequestParam String oldStatus,
            @RequestParam String newStatus,
            @RequestParam String userId) {
        try {
            notificationService.sendOrderStatusUpdate(orderId, oldStatus, newStatus, userId);
            return ResponseEntity.ok("Order status update sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error sending order status update: " + e.getMessage());
        }
    }

    @PostMapping("/out-of-stock/{productId}")
    @Operation(summary = "Send out of stock alert", description = "Send out of stock alert for a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Out of stock alert sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PreAuthorize("hasRole('MANAGEMENT')")
    public ResponseEntity<String> sendOutOfStockAlert(
            @PathVariable String productId,
            @RequestParam String productName) {
        try {
            notificationService.sendOutOfStockAlert(productId, productName);
            return ResponseEntity.ok("Out of stock alert sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error sending out of stock alert: " + e.getMessage());
        }
    }

    @PostMapping("/welcome/{userId}")
    @Operation(summary = "Send welcome notification", description = "Send welcome notification to new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Welcome notification sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PreAuthorize("hasRole('MANAGEMENT') or hasRole('ADMIN')")
    public ResponseEntity<String> sendWelcomeNotification(
            @PathVariable String userId,
            @RequestParam String userName) {
        try {
            notificationService.sendWelcomeNotification(userId, userName);
            return ResponseEntity.ok("Welcome notification sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error sending welcome notification: " + e.getMessage());
        }
    }

    @PostMapping("/personal/{sessionId}")
    @Operation(summary = "Send personal message", description = "Send personal message to specific WebSocket session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Personal message sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    @PreAuthorize("hasRole('MANAGEMENT') or hasRole('ADMIN')")
    public ResponseEntity<String> sendPersonalMessage(
            @PathVariable String sessionId,
            @RequestParam String title,
            @RequestParam String message,
            @RequestBody(required = false) Object data) {
        try {
            boolean sent = notificationService.sendPersonalMessage(sessionId, title, message, data);
            if (sent) {
                return ResponseEntity.ok("Personal message sent successfully");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error sending personal message: " + e.getMessage());
        }
    }
}
