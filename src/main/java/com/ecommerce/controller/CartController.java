package com.ecommerce.controller;

import com.ecommerce.dto.cart.CartItemRequest;
import com.ecommerce.dto.cart.CartResponse;
import com.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cart", description = "Shopping cart management APIs")
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Get user's cart")
    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @Operation(summary = "Add item to cart")
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CartItemRequest request) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(cartService.addItem(userId, request));
    }

    @Operation(summary = "Update cart item quantity")
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(cartService.updateItemQuantity(userId, itemId, quantity));
    }

    @Operation(summary = "Remove item from cart")
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(cartService.removeItem(userId, itemId));
    }

    @Operation(summary = "Clear cart")
    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        cartService.clearCart(userId);
        return ResponseEntity.ok().build();
    }

    private Long getUserId(UserDetails userDetails) {
        // TODO: Implement proper user ID extraction from UserDetails
        // This is a placeholder implementation
        return 1L;
    }
} 