package com.ecommerce.dto.product;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductImageRequest {
    @NotBlank(message = "Image URL is required")
    private String url;
    
    private String altText;
    
    private boolean isPrimary = false;
} 