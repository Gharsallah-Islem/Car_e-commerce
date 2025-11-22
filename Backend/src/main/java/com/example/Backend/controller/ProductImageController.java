package com.example.Backend.controller;

import com.example.Backend.entity.Product;
import com.example.Backend.entity.ProductImage;
import com.example.Backend.repository.ProductImageRepository;
import com.example.Backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Product Image management
 * Handles multiple images per product
 */
@RestController
@RequestMapping("/api/products/{productId}/images")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class ProductImageController {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    /**
     * Get all images for a product
     * GET /api/products/{productId}/images
     */
    @GetMapping
    public ResponseEntity<List<ProductImage>> getProductImages(@PathVariable UUID productId) {
        List<ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId);
        return ResponseEntity.ok(images);
    }

    /**
     * Add new image to product
     * POST /api/products/{productId}/images
     * Body: { "imageUrl": "https://...", "isPrimary": false }
     */
    @PostMapping
    public ResponseEntity<?> addProductImage(
            @PathVariable UUID productId,
            @RequestBody Map<String, Object> request) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        String imageUrl = (String) request.get("imageUrl");
        Boolean isPrimary = request.get("isPrimary") != null ? (Boolean) request.get("isPrimary") : false;

        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Image URL is required"));
        }

        // Get current max display order
        long imageCount = productImageRepository.countByProductId(productId);

        // If this is the first image or marked as primary, update existing primary
        if (isPrimary) {
            List<ProductImage> existingImages = productImageRepository
                    .findByProductIdOrderByDisplayOrderAsc(productId);
            existingImages.forEach(img -> {
                img.setIsPrimary(false);
                productImageRepository.save(img);
            });
        }

        ProductImage productImage = ProductImage.builder()
                .product(product)
                .imageUrl(imageUrl)
                .isPrimary(isPrimary || imageCount == 0) // First image is always primary
                .displayOrder((int) imageCount)
                .build();

        ProductImage saved = productImageRepository.save(productImage);

        // Update product's main imageUrl if this is primary
        if (saved.getIsPrimary()) {
            product.setImageUrl(imageUrl);
            productRepository.save(product);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Delete product image
     * DELETE /api/products/{productId}/images/{imageId}
     */
    @DeleteMapping("/{imageId}")
    public ResponseEntity<?> deleteProductImage(
            @PathVariable UUID productId,
            @PathVariable UUID imageId) {

        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        if (!image.getProduct().getId().equals(productId)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Image does not belong to this product"));
        }

        boolean wasPrimary = image.getIsPrimary();
        productImageRepository.delete(image);

        // If deleted image was primary, set another image as primary
        if (wasPrimary) {
            List<ProductImage> remainingImages = productImageRepository
                    .findByProductIdOrderByDisplayOrderAsc(productId);
            if (!remainingImages.isEmpty()) {
                ProductImage newPrimary = remainingImages.get(0);
                newPrimary.setIsPrimary(true);
                productImageRepository.save(newPrimary);

                // Update product's main imageUrl
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Product not found"));
                product.setImageUrl(newPrimary.getImageUrl());
                productRepository.save(product);
            } else {
                // No images left, clear product imageUrl
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Product not found"));
                product.setImageUrl(null);
                productRepository.save(product);
            }
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * Reorder product images
     * PATCH /api/products/{productId}/images/reorder
     * Body: [{ "id": "uuid1", "displayOrder": 0 }, { "id": "uuid2", "displayOrder":
     * 1 }]
     */
    @PatchMapping("/reorder")
    public ResponseEntity<?> reorderImages(
            @PathVariable UUID productId,
            @RequestBody List<Map<String, Object>> imageOrders) {

        for (Map<String, Object> order : imageOrders) {
            String imageId = (String) order.get("id");
            Integer displayOrder = (Integer) order.get("displayOrder");

            ProductImage image = productImageRepository.findById(UUID.fromString(imageId))
                    .orElseThrow(() -> new RuntimeException("Image not found"));

            if (!image.getProduct().getId().equals(productId)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Image does not belong to this product"));
            }

            image.setDisplayOrder(displayOrder);
            productImageRepository.save(image);
        }

        List<ProductImage> updatedImages = productImageRepository
                .findByProductIdOrderByDisplayOrderAsc(productId);
        return ResponseEntity.ok(updatedImages);
    }

    /**
     * Set image as primary
     * PATCH /api/products/{productId}/images/{imageId}/set-primary
     */
    @PatchMapping("/{imageId}/set-primary")
    public ResponseEntity<?> setPrimaryImage(
            @PathVariable UUID productId,
            @PathVariable UUID imageId) {

        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        if (!image.getProduct().getId().equals(productId)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Image does not belong to this product"));
        }

        // Remove primary from all images
        List<ProductImage> allImages = productImageRepository
                .findByProductIdOrderByDisplayOrderAsc(productId);
        allImages.forEach(img -> {
            img.setIsPrimary(false);
            productImageRepository.save(img);
        });

        // Set this image as primary
        image.setIsPrimary(true);
        ProductImage saved = productImageRepository.save(image);

        // Update product's main imageUrl
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setImageUrl(image.getImageUrl());
        productRepository.save(product);

        return ResponseEntity.ok(saved);
    }
}
