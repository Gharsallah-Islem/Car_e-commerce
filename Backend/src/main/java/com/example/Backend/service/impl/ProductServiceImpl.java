package com.example.Backend.service.impl;

import com.example.Backend.dto.ProductDTO;
import com.example.Backend.entity.Product;
import com.example.Backend.repository.ProductRepository;
import com.example.Backend.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    @Transactional(readOnly = true)
    public Page<Product> filterProducts(Long categoryId, Long brandId, BigDecimal minPrice,
            BigDecimal maxPrice, String search, Boolean inStock, Pageable pageable) {

        // Map category ID to category name
        final String categoryName = categoryId != null ? mapCategoryIdToName(categoryId) : null;

        // Map brand ID to brand name
        final String brandName = brandId != null ? mapBrandIdToName(brandId) : null;

        // Get all products and filter in memory to avoid bytea issue
        List<Product> allProducts = productRepository.findAll();

        // Apply filters
        List<Product> filtered = allProducts.stream()
                .filter(p -> categoryName == null || categoryName.equals(p.getCategory()))
                .filter(p -> brandName == null || brandName.equals(p.getBrand()))
                .filter(p -> minPrice == null || p.getPrice().compareTo(minPrice) >= 0)
                .filter(p -> maxPrice == null || p.getPrice().compareTo(maxPrice) <= 0)
                .filter(p -> search == null || p.getName().toLowerCase().contains(search.toLowerCase()))
                .filter(p -> inStock == null || !inStock || p.isInStock())
                .collect(java.util.stream.Collectors.toList());

        // Apply sorting
        if (pageable.getSort().isSorted()) {
            filtered = applySorting(filtered, pageable.getSort());
        }

        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtered.size());
        List<Product> pageContent = filtered.subList(start, end);

        return new org.springframework.data.domain.PageImpl<>(
                pageContent,
                pageable,
                filtered.size());
    }

    /**
     * Apply sorting to a list of products
     */
    private List<Product> applySorting(List<Product> products, Sort sort) {
        java.util.Comparator<Product> comparator = null;

        for (Sort.Order order : sort) {
            java.util.Comparator<Product> currentComparator = null;

            switch (order.getProperty()) {
                case "name":
                    currentComparator = java.util.Comparator.comparing(Product::getName,
                            String.CASE_INSENSITIVE_ORDER);
                    break;
                case "price":
                    currentComparator = java.util.Comparator.comparing(Product::getPrice);
                    break;
                case "createdAt":
                    currentComparator = java.util.Comparator.comparing(Product::getCreatedAt);
                    break;
                default:
                    currentComparator = java.util.Comparator.comparing(Product::getName);
            }

            if (order.getDirection() == Sort.Direction.DESC) {
                currentComparator = currentComparator.reversed();
            }

            comparator = comparator == null ? currentComparator : comparator.thenComparing(currentComparator);
        }

        if (comparator != null) {
            products.sort(comparator);
        }

        return products;
    }

    /**
     * Map category ID to category name
     */
    private String mapCategoryIdToName(Long categoryId) {
        Map<Long, String> categoryMap = new HashMap<>();
        categoryMap.put(1L, "Engine Parts");
        categoryMap.put(2L, "Brake Systems");
        categoryMap.put(3L, "Suspension");
        categoryMap.put(4L, "Electrical");
        categoryMap.put(5L, "Body Parts");
        categoryMap.put(6L, "Filters");
        categoryMap.put(7L, "Exhaust");
        categoryMap.put(8L, "Transmission");
        categoryMap.put(9L, "Cooling System");
        categoryMap.put(10L, "Interior");

        return categoryMap.get(categoryId);
    }

    /**
     * Map brand ID to brand name
     */
    private String mapBrandIdToName(Long brandId) {
        Map<Long, String> brandMap = new HashMap<>();
        brandMap.put(1L, "Bosch");
        brandMap.put(2L, "Continental");
        brandMap.put(3L, "Brembo");
        brandMap.put(4L, "Bilstein");
        brandMap.put(5L, "Denso");
        brandMap.put(6L, "Mann-Filter");
        brandMap.put(7L, "Akrapovic");
        brandMap.put(8L, "ZF");

        return brandMap.get(brandId);
    }
}
