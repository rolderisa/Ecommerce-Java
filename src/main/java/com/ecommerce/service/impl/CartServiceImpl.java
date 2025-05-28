package com.ecommerce.service.impl;

import com.ecommerce.domain.model.Cart;
import com.ecommerce.domain.model.CartItem;
import com.ecommerce.domain.model.Product;
import com.ecommerce.domain.model.ProductVariant;
import com.ecommerce.domain.repository.CartRepository;
import com.ecommerce.domain.repository.ProductRepository;
import com.ecommerce.domain.repository.ProductVariantRepository;
import com.ecommerce.dto.cart.CartItemRequest;
import com.ecommerce.dto.cart.CartItemResponse;
import com.ecommerce.dto.cart.CartResponse;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> createNewCart(userId));
        return mapToResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(Long userId, CartItemRequest request) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> createNewCart(userId));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.isActive()) {
            throw new IllegalStateException("Product is not available");
        }

        if (product.getStock() < request.getQuantity()) {
            throw new IllegalStateException("Insufficient stock");
        }

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(request.getQuantity());

        if (request.getVariantId() != null) {
            ProductVariant variant = productVariantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product variant not found"));

            if (variant.getStock() < request.getQuantity()) {
                throw new IllegalStateException("Insufficient variant stock");
            }

            cartItem.setVariant(variant);
        }

        cart.addItem(cartItem);
        Cart savedCart = cartRepository.save(cart);
        return mapToResponse(savedCart);
    }

    @Override
    @Transactional
    public CartResponse updateItemQuantity(Long userId, Long itemId, Integer quantity) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (quantity <= 0) {
            cart.removeItem(cartItem);
        } else {
            Product product = cartItem.getProduct();
            if (product.getStock() < quantity) {
                throw new IllegalStateException("Insufficient stock");
            }

            if (cartItem.getVariant() != null && cartItem.getVariant().getStock() < quantity) {
                throw new IllegalStateException("Insufficient variant stock");
            }

            cartItem.setQuantity(quantity);
        }

        Cart savedCart = cartRepository.save(cart);
        return mapToResponse(savedCart);
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long userId, Long itemId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        cart.removeItem(cartItem);
        Cart savedCart = cartRepository.save(cart);
        return mapToResponse(savedCart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        cart.clear();
        cartRepository.save(cart);
    }

    private Cart createNewCart(Long userId) {
        Cart cart = new Cart();
        cart.setUser(new com.ecommerce.domain.model.User());
        cart.getUser().setId(userId);
        return cartRepository.save(cart);
    }

    private CartResponse mapToResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setUpdatedAt(cart.getUpdatedAt());

        response.setItems(cart.getItems().stream()
                .map(this::mapToItemResponse)
                .collect(Collectors.toSet()));

        response.setSubtotal(calculateSubtotal(cart));
        return response;
    }

    private CartItemResponse mapToItemResponse(CartItem item) {
        CartItemResponse response = new CartItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getName());
        response.setProductImage(item.getProduct().getImages().stream()
                .filter(image -> image.isPrimary())
                .findFirst()
                .map(image -> image.getUrl())
                .orElse(null));
        response.setQuantity(item.getQuantity());

        if (item.getVariant() != null) {
            response.setVariantId(item.getVariant().getId());
            response.setVariantName(item.getVariant().getName());
            response.setUnitPrice(item.getVariant().getPrice());
        } else {
            response.setUnitPrice(item.getProduct().getPrice());
        }

        response.setTotalPrice(response.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        return response;
    }

    private BigDecimal calculateSubtotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> {
                    BigDecimal price = item.getVariant() != null ? 
                            item.getVariant().getPrice() : 
                            item.getProduct().getPrice();
                    return price.multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
} 