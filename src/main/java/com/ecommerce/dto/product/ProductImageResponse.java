package com.ecommerce.dto.product;

import lombok.Data;

@Data
public class ProductImageResponse {
    private Long id;
    private String url;
    private String altText;
    private boolean isPrimary;
} 