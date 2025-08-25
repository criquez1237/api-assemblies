package com.assembliestore.api.module.cart.application.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
// removed unused imports

import org.springframework.stereotype.Service;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

import org.springframework.beans.factory.annotation.Autowired;

import com.assembliestore.api.module.cart.infrastructure.adapter.dto.CartItemRequest;
import com.assembliestore.api.module.cart.infrastructure.adapter.dto.CartItemResponse;
import com.assembliestore.api.module.cart.infrastructure.adapter.dto.CartResponse;

@Service
public class CartService {

    @Autowired
    private Firestore firestore;

    /**
     * Get cart with optional pagination and filters.
     * page: 1-based page number (default 1)
     * limit: items per page (default 10)
     * name: optional product name substring filter (case-insensitive)
     * unitPrice: optional exact unit price filter
     */
    public CartResponse getCart(String userId, Integer page, Integer limit, String nameFilter, java.math.BigDecimal unitPriceFilter) {
        int p = (page == null || page < 1) ? 1 : page;
        int l = (limit == null || limit < 1) ? 10 : limit;

        CartResponse loaded = loadCartFromFirestore(userId);
        if (loaded == null) loaded = new CartResponse(new ArrayList<>(), BigDecimal.ZERO);

        List<CartItemResponse> allItems = loaded.getItems() != null ? new ArrayList<>(loaded.getItems()) : new ArrayList<>();

        // apply filters
        List<CartItemResponse> filtered = new ArrayList<>();
        for (CartItemResponse it : allItems) {
            boolean keep = true;
            if (nameFilter != null && !nameFilter.isBlank()) {
                String nm = it.getName() == null ? "" : it.getName();
                if (!nm.toLowerCase().contains(nameFilter.toLowerCase())) keep = false;
            }
            if (keep && unitPriceFilter != null) {
                if (it.getUnitPrice() == null || it.getUnitPrice().compareTo(unitPriceFilter) != 0) keep = false;
            }
            if (keep) filtered.add(it);
        }

        // pagination
    int totalItems = filtered.size();
    int totalPages = (int) Math.ceil((double) totalItems / l);
        if (totalPages == 0) totalPages = 1;
        int currentPage = Math.min(p, totalPages);
        int fromIndex = (currentPage - 1) * l;
        int toIndex = Math.min(fromIndex + l, totalItems);
        List<CartItemResponse> pageItems = new ArrayList<>();
        if (fromIndex < toIndex) pageItems = filtered.subList(fromIndex, toIndex);

        CartResponse resp = new CartResponse(pageItems, loaded.getTotal());
    // totalItems moved into PaginationDto
        int qty = 0; if (pageItems != null) for (CartItemResponse it : pageItems) qty += it.getQuantity();
        resp.setTotalQuantity(qty);

        com.assembliestore.api.module.cart.infrastructure.adapter.dto.PaginationDto pagination = 
            new com.assembliestore.api.module.cart.infrastructure.adapter.dto.PaginationDto(
                filtered.size(), totalPages, currentPage, currentPage < totalPages, currentPage > 1, l
            );
        resp.setPagination(pagination);

        return resp;
    }

    private CartResponse loadCartFromFirestore(String userId) {
        try {
            DocumentSnapshot doc = firestore.collection("carts").document(userId).get().get();
            if (doc.exists()) {
                Object itemsObj = doc.get("items");
                List<CartItemResponse> items = new ArrayList<>();
                if (itemsObj instanceof List) {
                    List<?> rawItems = (List<?>) itemsObj;
                    for (Object o : rawItems) {
                        if (o instanceof java.util.Map) {
                            java.util.Map<?,?> m = (java.util.Map<?,?>) o;
                            String productId = (String) m.get("productId");
                            String name = (String) m.get("name");
                            Object unitPriceObj = m.get("unitPrice");
                            java.math.BigDecimal unitPrice = unitPriceObj == null ? java.math.BigDecimal.ZERO : java.math.BigDecimal.valueOf(((Number)unitPriceObj).doubleValue());
                            String description = (String) m.get("description");
                            List<String> gallery = new ArrayList<>();
                            Object galleryObj = m.get("gallery");
                            if (galleryObj instanceof List) {
                                for (Object g : (List<?>) galleryObj) {
                                    if (g != null) gallery.add(g.toString());
                                }
                            }
                            int quantity = m.get("quantity") == null ? 0 : ((Number) m.get("quantity")).intValue();
                            Object subtotalObj = m.get("subtotal");
                            java.math.BigDecimal subtotal = subtotalObj == null ? java.math.BigDecimal.ZERO : java.math.BigDecimal.valueOf(((Number)subtotalObj).doubleValue());

                            CartItemResponse item = new CartItemResponse(productId, name, unitPrice, description, gallery, quantity, subtotal);
                            items.add(item);
                        }
                    }
                }

                Object totalObj = doc.get("total");
                java.math.BigDecimal total = totalObj == null ? java.math.BigDecimal.ZERO : java.math.BigDecimal.valueOf(((Number)totalObj).doubleValue());
                CartResponse cart = new CartResponse(items, total);
                return cart;
            }
        } catch (Exception e) {
            // ignore Firestore errors for now and fallback to null
        }
        return null;
    }

    public CartResponse addItem(String userId, CartItemRequest req) {
        CartResponse cart = loadCartFromFirestore(userId);
        if (cart == null) cart = new CartResponse(new ArrayList<>(), BigDecimal.ZERO);

        synchronized (this) {
            List<CartItemResponse> items = cart.getItems();
            if (items == null) {
                items = new ArrayList<>();
                cart.setItems(items);
            }

            CartItemResponse found = null;
            for (CartItemResponse it : items) {
                if (it.getProductId().equals(req.getProductId())) {
                    found = it;
                    break;
                }
            }

        if (found != null) {
                // increase quantity
                int newQty = found.getQuantity() + req.getQuantity();
                found.setQuantity(newQty);
                found.setSubtotal(found.getUnitPrice().multiply(BigDecimal.valueOf(newQty)));
            } else {
        CartItemResponse newItem = new CartItemResponse(
            req.getProductId(),
            req.getName(),
            req.getUnitPrice(),
            req.getDescription(),
            req.getGallery() != null ? req.getGallery() : Collections.emptyList(),
            req.getQuantity(),
            req.getUnitPrice().multiply(BigDecimal.valueOf(req.getQuantity()))
        );
                items.add(newItem);
            }

            recalculateTotal(cart);
            // persist to Firestore
            persistCartToFirestore(userId, cart);
            return cart;
        }
    }

    public CartResponse updateQuantity(String userId, String productId, int quantity) {
        if (quantity < 0) throw new IllegalArgumentException("Quantity must be >= 0");
    CartResponse cart = loadCartFromFirestore(userId);
    if (cart == null) return new CartResponse(new ArrayList<>(), BigDecimal.ZERO);

        synchronized (cart) {
            List<CartItemResponse> items = cart.getItems();
            if (items == null) return cart;
            CartItemResponse found = null;
            for (CartItemResponse it : items) {
                if (it.getProductId().equals(productId)) {
                    found = it;
                    break;
                }
            }

            if (found == null) {
                throw new IllegalArgumentException("Item not found in cart: " + productId);
            }

            if (quantity == 0) {
                items.remove(found);
            } else {
                found.setQuantity(quantity);
                found.setSubtotal(found.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
            }

            recalculateTotal(cart);
            persistCartToFirestore(userId, cart);
            return cart;
        }
    }

    public CartResponse removeItem(String userId, String productId) {
        CartResponse cart = loadCartFromFirestore(userId);
        if (cart == null) return new CartResponse(new ArrayList<>(), BigDecimal.ZERO);

        synchronized (this) {
            cart.getItems().removeIf(i -> i.getProductId() != null && i.getProductId().equals(productId));
            recalculateTotal(cart);
            persistCartToFirestore(userId, cart);
            return cart;
        }
    }

    private void recalculateTotal(CartResponse cart) {
    BigDecimal total = BigDecimal.ZERO;
    int totalQuantity = 0;
        if (cart.getItems() != null) {
            for (CartItemResponse it : cart.getItems()) {
                if (it.getSubtotal() != null) total = total.add(it.getSubtotal());
                totalQuantity += it.getQuantity();
            }
        }
    cart.setTotal(total);
    cart.setTotalQuantity(totalQuantity);
    }

    private void persistCartToFirestore(String userId, CartResponse cart) {
        try {
            java.util.List<java.util.Map<String,Object>> items = new ArrayList<>();
            if (cart.getItems() != null) {
                for (CartItemResponse it : cart.getItems()) {
                    java.util.Map<String,Object> m = new java.util.HashMap<>();
                    m.put("id", it.getId());
                    m.put("productId", it.getProductId());
                    m.put("name", it.getName());
                    m.put("unitPrice", it.getUnitPrice() != null ? it.getUnitPrice().doubleValue() : null);
                    m.put("description", it.getDescription());
                    m.put("gallery", it.getGallery());
                    m.put("quantity", it.getQuantity());
                    m.put("subtotal", it.getSubtotal() != null ? it.getSubtotal().doubleValue() : null);
                    items.add(m);
                }
            }

            if (items.isEmpty()) {
                // delete document when cart empty
                firestore.collection("carts").document(userId).delete().get();
            } else {
                java.util.Map<String,Object> payload = new java.util.HashMap<>();
                payload.put("items", items);
                payload.put("total", cart.getTotal() != null ? cart.getTotal().doubleValue() : 0.0);

                firestore.collection("carts").document(userId).set(payload).get();
            }
        } catch (Exception e) {
            // swallow for now; logging could be added
        }
    }
}
