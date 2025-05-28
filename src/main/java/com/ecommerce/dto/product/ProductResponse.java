package com.ecommerce.dto.product;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Long categoryId;
    private String categoryName;
    private Set<String> tags;
    private Set<ProductImageResponse> images;
    private Set<ProductVariantResponse> variants;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 