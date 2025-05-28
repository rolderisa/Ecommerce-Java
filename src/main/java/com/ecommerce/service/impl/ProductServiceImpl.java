package com.ecommerce.service.impl;

import com.ecommerce.domain.model.Category;
import com.ecommerce.domain.model.Product;
import com.ecommerce.domain.model.ProductImage;
import com.ecommerce.domain.model.ProductVariant;
import com.ecommerce.domain.model.Tag;
import com.ecommerce.domain.repository.CategoryRepository;
import com.ecommerce.domain.repository.ProductRepository;
import com.ecommerce.domain.repository.ProductImageRepository;
import com.ecommerce.domain.repository.ProductVariantRepository;
import com.ecommerce.domain.repository.TagRepository;

import com.ecommerce.dto.product.ProductRequest;
import com.ecommerce.dto.product.ProductResponse;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setActive(true); // Default to active

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        product.setCategory(category);

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            product.setTags(tags);
        }

        Product savedProduct = productRepository.save(product);

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            Set<ProductImage> images = request.getImages().stream()
                    .map(imageRequest -> {
                        ProductImage image = new ProductImage();
                        image.setUrl(imageRequest.getUrl());
                        image.setAltText(imageRequest.getAltText());
                        image.setPrimary(imageRequest.isPrimary());
                        image.setProduct(savedProduct);
                        return image;
                    })
                    .collect(Collectors.toSet());
            savedProduct.setImages(new HashSet<>(productImageRepository.saveAll(images)));
        }

        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            Set<ProductVariant> variants = request.getVariants().stream()
                    .map(variantRequest -> {
                        ProductVariant variant = new ProductVariant();
                        variant.setSku(variantRequest.getSku());
                        variant.setName(variantRequest.getName());
                        variant.setDescription(variantRequest.getDescription());
                        variant.setPrice(variantRequest.getPrice());
                        variant.setStock(variantRequest.getStock());
                        variant.setSize(variantRequest.getSize());
                        variant.setColor(variantRequest.getColor());
                        variant.setMaterial(variantRequest.getMaterial());
                        variant.setProduct(savedProduct);
                        return variant;
                    })
                    .collect(Collectors.toSet());
            savedProduct.setVariants(new HashSet<>(productVariantRepository.saveAll(variants)));
        }

        return convertToDto(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + request.getCategoryId()));
        product.setCategory(category);

        if (request.getTagIds() != null) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            product.setTags(tags);
        } else {
            product.setTags(new HashSet<>()); // Clear tags if tagIds is null
        }

        // Handle images update: clear existing and add new ones
        if (product.getImages() != null) {
            productImageRepository.deleteAll(product.getImages());
            product.setImages(new HashSet<>());
        }
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            Set<ProductImage> images = request.getImages().stream()
                    .map(imageRequest -> {
                        ProductImage image = new ProductImage();
                        image.setUrl(imageRequest.getUrl());
                        image.setAltText(imageRequest.getAltText());
                        image.setPrimary(imageRequest.isPrimary());
                        image.setProduct(product);
                        return image;
                    })
                    .collect(Collectors.toSet());
            product.setImages(new HashSet<>(productImageRepository.saveAll(images)));
        }

        // Handle variants update: clear existing and add new ones
        if (product.getVariants() != null) {
            productVariantRepository.deleteAll(product.getVariants());
            product.setVariants(new HashSet<>());
        }
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            Set<ProductVariant> variants = request.getVariants().stream()
                    .map(variantRequest -> {
                        ProductVariant variant = new ProductVariant();
                        variant.setSku(variantRequest.getSku());
                        variant.setName(variantRequest.getName());
                        variant.setDescription(variantRequest.getDescription());
                        variant.setPrice(variantRequest.getPrice());
                        variant.setStock(variantRequest.getStock());
                        variant.setSize(variantRequest.getSize());
                        variant.setColor(variantRequest.getColor());
                        variant.setMaterial(variantRequest.getMaterial());
                        variant.setProduct(product);
                        return variant;
                    })
                    .collect(Collectors.toSet());
            product.setVariants(new HashSet<>(productVariantRepository.saveAll(variants)));
        }

        Product updatedProduct = productRepository.save(product);
        return convertToDto(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
        productRepository.delete(product);
    }

    @Override
    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
        return convertToDto(product);
    }

    @Override
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        List<ProductResponse> content = products.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, products.getTotalElements());
    }

    @Override
    public Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        Category category = categoryRepository.findById(categoryId)
                 .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + categoryId));
        Page<Product> products = productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
         List<ProductResponse> content = products.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, products.getTotalElements());
    }

    @Override
    public Page<ProductResponse> searchProducts(String query, Pageable pageable) {
         Page<Product> products = productRepository.search(query, pageable);
         List<ProductResponse> content = products.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, products.getTotalElements());
    }

    @Override
    @Transactional
    public void updateStock(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
        product.setStock(quantity);
        productRepository.save(product);
    }

    // Helper method to convert Product entity to ProductResponse DTO
    private ProductResponse convertToDto(Product product) {
        ProductResponse dto = new ProductResponse();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setActive(product.isActive());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        if (product.getTags() != null) {
            dto.setTags(product.getTags().stream()
                    .map(Tag::getName)
                    .collect(Collectors.toSet()));
        }

        if (product.getImages() != null) {
            dto.setImages(product.getImages().stream()
                    .map(image -> {
                        com.ecommerce.dto.product.ProductImageResponse imageDto = new com.ecommerce.dto.product.ProductImageResponse();
                        imageDto.setId(image.getId());
                        imageDto.setUrl(image.getUrl());
                        imageDto.setAltText(image.getAltText());
                        imageDto.setPrimary(image.isPrimary());
                        return imageDto;
                    })
                    .collect(Collectors.toSet()));
        }

        if (product.getVariants() != null) {
            dto.setVariants(product.getVariants().stream()
                    .map(variant -> {
                        com.ecommerce.dto.product.ProductVariantResponse variantDto = new com.ecommerce.dto.product.ProductVariantResponse();
                        variantDto.setId(variant.getId());
                        variantDto.setSku(variant.getSku());
                        variantDto.setName(variant.getName());
                        variantDto.setDescription(variant.getDescription());
                        variantDto.setPrice(variant.getPrice());
                        variantDto.setStock(variant.getStock());
                        variantDto.setSize(variant.getSize());
                        variantDto.setColor(variant.getColor());
                        variantDto.setMaterial(variant.getMaterial());
                        return variantDto;
                    })
                    .collect(Collectors.toSet()));
        }

        return dto;
    }
} 