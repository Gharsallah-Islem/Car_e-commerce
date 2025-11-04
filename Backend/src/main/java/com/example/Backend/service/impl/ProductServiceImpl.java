package com.example.Backend.service.impl;

import com.example.Backend.dto.ProductDTO;
import com.example.Backend.entity.Product;
import com.example.Backend.repository.ProductRepository;
import com.example.Backend.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Product createProduct(ProductDTO productDTO) {
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setCategory(productDTO.getCategory());
        product.setBrand(productDTO.getBrand());
        product.setStock(productDTO.getStockQuantity());
        product.setImageUrl(productDTO.getImageUrl());

        // Convert vehicle compatibility map to JSON string if needed
        if (productDTO.getVehicleCompatibility() != null) {
            // Store as JSON string in compatibility field
            product.setCompatibility(productDTO.getVehicleCompatibility().toString());
        }

        return productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
    }

    @Override
    public Product updateProduct(UUID id, ProductDTO productDTO) {
        Product product = getProductById(id);

        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setCategory(productDTO.getCategory());
        product.setBrand(productDTO.getBrand());
        product.setStock(productDTO.getStockQuantity());
        product.setImageUrl(productDTO.getImageUrl());

        // Convert vehicle compatibility map to JSON string if needed
        if (productDTO.getVehicleCompatibility() != null) {
            product.setCompatibility(productDTO.getVehicleCompatibility().toString());
        }

        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String searchTerm, String category, String brand,
            String model, BigDecimal minPrice, BigDecimal maxPrice,
            Pageable pageable) {
        return productRepository.searchProducts(searchTerm, category, brand, model,
                minPrice, maxPrice, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getProductsByBrand(String brand) {
        return productRepository.findByBrand(brand);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getCompatibleProducts(String brand, String model, Integer year) {
        return productRepository.findCompatibleProducts(brand, model, year);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getProductsInStock() {
        return productRepository.findInStock();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts(Integer threshold) {
        return productRepository.findLowStock(threshold);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getOutOfStockProducts() {
        return productRepository.findOutOfStock();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getTopSellingProducts(Pageable pageable) {
        return productRepository.findTopSellingProducts(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getFeaturedProducts(Pageable pageable) {
        // Return top selling products as featured products
        return productRepository.findTopSellingProducts(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return productRepository.findDistinctCategories();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllBrands() {
        return productRepository.findDistinctBrands();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getModelsByBrand(String brand) {
        return productRepository.findDistinctModelsByBrand(brand);
    }

    @Override
    public Product updateStock(UUID id, Integer quantity) {
        Product product = getProductById(id);

        if (quantity > 0) {
            product.increaseStock(quantity);
        } else if (quantity < 0) {
            product.decreaseStock(Math.abs(quantity));
        }

        return productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countProductsInStock() {
        return productRepository.countInStock();
    }
}
