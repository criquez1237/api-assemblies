package com.assembliestore.api.module.product.domain.port;

import java.util.List;
import java.util.Map;

public interface StockPort {
    /**
     * Reduce el stock de los productos especificados
     * @param productQuantities Map con productId como key y quantity como value
     * @return true si el stock fue reducido exitosamente, false si no hay suficiente stock
     */
    boolean reduceStock(Map<String, Integer> productQuantities);
    
    /**
     * Restaura el stock de los productos especificados (por ejemplo, cuando un pago falla)
     * @param productQuantities Map con productId como key y quantity como value
     */
    void restoreStock(Map<String, Integer> productQuantities);
    
    /**
     * Verifica si hay suficiente stock para los productos especificados
     * @param productQuantities Map con productId como key y quantity como value
     * @return Map con productId como key y boolean como value indicando si hay suficiente stock
     */
    Map<String, Boolean> checkStockAvailability(Map<String, Integer> productQuantities);
    
    /**
     * Obtiene el stock actual de un producto
     * @param productId ID del producto
     * @return Stock actual del producto, -1 si el producto no existe
     */
    int getCurrentStock(String productId);
    
    /**
     * Obtiene el stock actual de múltiples productos
     * @param productIds Lista de IDs de productos
     * @return Map con productId como key y stock actual como value
     */
    Map<String, Integer> getCurrentStock(List<String> productIds);
}
