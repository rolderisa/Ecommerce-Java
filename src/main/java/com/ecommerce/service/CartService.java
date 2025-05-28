package com.ecommerce.service;

import com.ecommerce.dto.cart.CartItemRequest;
import com.ecommerce.dto.cart.CartResponse;

public interface CartService {
    CartResponse getCart(Long userId);
    CartResponse addItem(Long userId, CartItemRequest request);
    CartResponse updateItemQuantity(Long userId, Long itemId, Integer quantity);
    CartResponse removeItem(Long userId, Long itemId);
    void clearCart(Long userId);
} 