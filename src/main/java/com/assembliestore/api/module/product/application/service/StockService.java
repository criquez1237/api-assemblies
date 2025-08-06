package com.assembliestore.api.module.product.application.service;

import com.assembliestore.api.module.product.domain.entity.Product;
import com.assembliestore.api.module.product.domain.port.StockPort;
import com.assembliestore.api.module.product.domain.repository.ProductRepository;
import com.assembliestore.api.service.realtime.service.RealtimeNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class StockService implements StockPort {
    
    private final ProductRepository productRepository;
    
    @Autowired
    private RealtimeNotificationService realtimeNotificationService;

    public StockService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public boolean reduceStock(Map<String, Integer> productQuantities) {
        Map<String, Boolean> availability = checkStockAvailability(productQuantities);
        
        for (Map.Entry<String, Boolean> entry : availability.entrySet()) {
            if (!entry.getValue()) {
                return false;
            }
        }
        
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            String productId = entry.getKey();
            Integer quantityToReduce = entry.getValue();
            
            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                int previousStock = product.getStockQuantity();
                int newStock = previousStock - quantityToReduce;
                
                product.setStockQuantity(newStock);
                product.refreshUpdatedAt();
                productRepository.upsert(product);
                
                notifyStockChange(product, previousStock, newStock, "SALE");
                
                if (newStock == 0) {
                    notifyOutOfStock(product);
                }
            }
        }
        
        return true;
    }

    @Override
    public void restoreStock(Map<String, Integer> productQuantities) {
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            String productId = entry.getKey();
            Integer quantityToRestore = entry.getValue();
            
            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                int previousStock = product.getStockQuantity();
                int newStock = previousStock + quantityToRestore;
                
                product.setStockQuantity(newStock);
                product.refreshUpdatedAt();
                productRepository.upsert(product);
                
                notifyStockChange(product, previousStock, newStock, "RESTORE");
            }
        }
    }

    public void restoreStockForOrder(String orderId) {
       
        System.out.println("Restaurando stock para la orden: " + orderId);
    }
    
    @Override
    public Map<String, Boolean> checkStockAvailability(Map<String, Integer> productQuantities) {
        Map<String, Boolean> availability = new HashMap<>();
        
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            String productId = entry.getKey();
            Integer requiredQuantity = entry.getValue();
            
            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                boolean hasEnoughStock = product.getStockQuantity() >= requiredQuantity;
                availability.put(productId, hasEnoughStock);
            } else {
                availability.put(productId, false); 
            }
        }
        
        return availability;
    }

    @Override
    public int getCurrentStock(String productId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        return productOpt.map(Product::getStockQuantity).orElse(-1);
    }

    @Override
    public Map<String, Integer> getCurrentStock(List<String> productIds) {
        Map<String, Integer> stockMap = new HashMap<>();
        
        for (String productId : productIds) {
            int stock = getCurrentStock(productId);
            stockMap.put(productId, stock);
        }
        
        return stockMap;
    }
    
    private void notifyStockChange(Product product, int previousStock, int newStock, String changeType) {
        // Notificación en consola para debug
        System.out.println("Stock updated: " + product.getName() + " from " + previousStock + " to " + newStock + " (" + changeType + ")");
        
        try {
            // Enviar notificación específica a MANAGEMENT y ADMIN
            realtimeNotificationService.sendStockUpdateToManagement(
                product.getId(), 
                product.getName(), 
                previousStock, 
                newStock, 
                changeType
            );
            
        } catch (Exception e) {
            System.err.println("Error sending stock notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void notifyOutOfStock(Product product) {
        // Notificación en consola para debug
        System.out.println("OUT OF STOCK ALERT: " + product.getName() + " is out of stock!");
        
        try {
            // Usar el método específico del servicio de notificaciones para productos agotados
            realtimeNotificationService.sendOutOfStockAlert(product.getId(), product.getName());
            
        } catch (Exception e) {
            System.err.println("Error sending out of stock notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
