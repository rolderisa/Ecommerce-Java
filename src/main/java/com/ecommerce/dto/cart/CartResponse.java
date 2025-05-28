package com.ecommerce.dto.cart;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class CartResponse {
    private Long id;
    private Set<CartItemResponse> items;
    private BigDecimal subtotal;
    private LocalDateTime updatedAt;
} 