package com.example.Backend.service.impl;

import com.example.Backend.entity.*;
import com.example.Backend.repository.ProductRepository;
import com.example.Backend.repository.RecommendationRepository;
import com.example.Backend.repository.UserActivityRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.RecommendationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of RecommendationService.
 * Uses a hybrid approach combining user activity data with product similarity.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final RecommendationRepository recommendationRepository;
    private final UserActivityRepository activityRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Product> getPersonalizedRecommendations(UUID userId, int limit) {
        log.info("Getting personalized recommendations for user: {}", userId);

        // Get user's category preferences from activity
        List<Object[]> categoryPrefs = activityRepository.findUserCategoryPreferences(userId);
        List<Object[]> brandPrefs = activityRepository.findUserBrandPreferences(userId);

        // Get recently viewed products to exclude
        List<UUID> recentlyViewed = activityRepository.findRecentlyViewedProductIds(
                userId, PageRequest.of(0, 50));
        Set<UUID> viewedSet = new HashSet<>(recentlyViewed);

        // Build recommendations based on preferences
        List<Product> recommendations = new ArrayList<>();

        // 1. Products from preferred categories
        if (!categoryPrefs.isEmpty()) {
            Long topCategoryId = (Long) categoryPrefs.get(0)[0];
            List<Product> categoryProducts = productRepository
                    .findFeaturedProducts(PageRequest.of(0, limit * 2)).getContent()
                    .stream()
                    .filter(p -> p.getCategory() != null &&
                            topCategoryId.equals(p.getCategory().getId()) &&
                            !viewedSet.contains(p.getId()) &&
                            p.getStock() > 0)
                    .limit(limit / 2)
                    .collect(Collectors.toList());
            recommendations.addAll(categoryProducts);
        }

        // 2. Products from preferred brands
        if (!brandPrefs.isEmpty() && recommendations.size() < limit) {
            Long topBrandId = (Long) brandPrefs.get(0)[0];
            int remaining = limit - recommendations.size();
            Set<UUID> alreadyAdded = recommendations.stream()
                    .map(Product::getId).collect(Collectors.toSet());

            List<Product> brandProducts = productRepository
                    .findFeaturedProducts(PageRequest.of(0, limit * 2)).getContent()
                    .stream()
                    .filter(p -> p.getBrand() != null &&
                            topBrandId.equals(p.getBrand().getId()) &&
                            !viewedSet.contains(p.getId()) &&
                            !alreadyAdded.contains(p.getId()) &&
                            p.getStock() > 0)
                    .limit(remaining)
                    .collect(Collectors.toList());
            recommendations.addAll(brandProducts);
        }

        // 3. Fill with featured products if needed
        if (recommendations.size() < limit) {
            Set<UUID> alreadyAdded = recommendations.stream()
                    .map(Product::getId).collect(Collectors.toSet());
            int remaining = limit - recommendations.size();

            List<Product> featured = productRepository
                    .findFeaturedProducts(PageRequest.of(0, remaining + 10)).getContent()
                    .stream()
                    .filter(p -> !viewedSet.contains(p.getId()) &&
                            !alreadyAdded.contains(p.getId()) &&
                            p.getStock() > 0)
                    .limit(remaining)
                    .collect(Collectors.toList());
            recommendations.addAll(featured);
        }

        log.info("Returning {} personalized recommendations for user: {}",
                recommendations.size(), userId);
        return recommendations;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getSimilarProducts(UUID productId, int limit) {
        log.info("Getting similar products for: {}", productId);

        Product sourceProduct = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        List<Product> similar = new ArrayList<>();

        // 1. Same category products
        if (sourceProduct.getCategory() != null) {
            List<Product> sameCategory = productRepository
                    .findFeaturedProducts(PageRequest.of(0, limit * 3)).getContent()
                    .stream()
                    .filter(p -> !p.getId().equals(productId) &&
                            p.getCategory() != null &&
                            p.getCategory().getId().equals(sourceProduct.getCategory().getId()) &&
                            p.getStock() > 0)
                    .limit(limit / 2)
                    .collect(Collectors.toList());
            similar.addAll(sameCategory);
        }

        // 2. Same brand products
        if (sourceProduct.getBrand() != null && similar.size() < limit) {
            Set<UUID> alreadyAdded = similar.stream()
                    .map(Product::getId).collect(Collectors.toSet());
            int remaining = limit - similar.size();

            List<Product> sameBrand = productRepository
                    .findFeaturedProducts(PageRequest.of(0, limit * 3)).getContent()
                    .stream()
                    .filter(p -> !p.getId().equals(productId) &&
                            !alreadyAdded.contains(p.getId()) &&
                            p.getBrand() != null &&
                            p.getBrand().getId().equals(sourceProduct.getBrand().getId()) &&
                            p.getStock() > 0)
                    .limit(remaining)
                    .collect(Collectors.toList());
            similar.addAll(sameBrand);
        }

        // 3. Similar price range (within 30%)
        if (sourceProduct.getPrice() != null && similar.size() < limit) {
            Set<UUID> alreadyAdded = similar.stream()
                    .map(Product::getId).collect(Collectors.toSet());
            double minPrice = sourceProduct.getPrice().doubleValue() * 0.7;
            double maxPrice = sourceProduct.getPrice().doubleValue() * 1.3;
            int remaining = limit - similar.size();

            List<Product> similarPrice = productRepository
                    .findByPriceBetween(
                            java.math.BigDecimal.valueOf(minPrice),
                            java.math.BigDecimal.valueOf(maxPrice))
                    .stream()
                    .filter(p -> !p.getId().equals(productId) &&
                            !alreadyAdded.contains(p.getId()) &&
                            p.getStock() > 0)
                    .limit(remaining)
                    .collect(Collectors.toList());
            similar.addAll(similarPrice);
        }

        log.info("Returning {} similar products for: {}", similar.size(), productId);
        return similar;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAlsoBoughtProducts(UUID productId, int limit) {
        log.info("Getting also-bought products for: {}", productId);

        // Get products frequently bought together from activity data
        List<Object[]> alsoBoughtData = activityRepository.findAlsoBoughtProductIds(
                productId, PageRequest.of(0, limit));

        List<Product> alsoBought = new ArrayList<>();
        for (Object[] data : alsoBoughtData) {
            UUID alsoProductId = (UUID) data[0];
            productRepository.findById(alsoProductId)
                    .filter(p -> p.getStock() > 0)
                    .ifPresent(alsoBought::add);
        }

        // If not enough data, fall back to similar products
        if (alsoBought.size() < limit) {
            List<Product> similar = getSimilarProducts(productId, limit - alsoBought.size());
            Set<UUID> alreadyAdded = alsoBought.stream()
                    .map(Product::getId).collect(Collectors.toSet());

            similar.stream()
                    .filter(p -> !alreadyAdded.contains(p.getId()))
                    .limit(limit - alsoBought.size())
                    .forEach(alsoBought::add);
        }

        log.info("Returning {} also-bought products for: {}", alsoBought.size(), productId);
        return alsoBought;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getTrendingProducts(int days, int limit) {
        log.info("Getting trending products for last {} days", days);

        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Object[]> trendingData = activityRepository.findTrendingProductIds(
                since, PageRequest.of(0, limit));

        List<Product> trending = new ArrayList<>();
        for (Object[] data : trendingData) {
            UUID trendingProductId = (UUID) data[0];
            productRepository.findById(trendingProductId)
                    .filter(p -> p.getStock() > 0)
                    .ifPresent(trending::add);
        }

        // Fall back to featured products if not enough trending data
        if (trending.size() < limit) {
            Set<UUID> alreadyAdded = trending.stream()
                    .map(Product::getId).collect(Collectors.toSet());
            int remaining = limit - trending.size();

            productRepository.findFeaturedProducts(PageRequest.of(0, remaining + 5))
                    .getContent()
                    .stream()
                    .filter(p -> !alreadyAdded.contains(p.getId()) && p.getStock() > 0)
                    .limit(remaining)
                    .forEach(trending::add);
        }

        log.info("Returning {} trending products", trending.size());
        return trending;
    }

    @Override
    @Transactional
    public Recommendation saveRecommendation(UUID userId, UUID productId,
            RecommendationType type, Double score, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        Recommendation recommendation = new Recommendation(user, product, type, score, reason);
        return recommendationRepository.save(recommendation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Recommendation> getUserRecommendations(UUID userId, int limit) {
        return recommendationRepository.findByUserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(0, limit)).getContent();
    }

    @Override
    @Transactional
    public void markAsViewed(UUID recommendationId) {
        Recommendation recommendation = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Recommendation not found: " + recommendationId));
        recommendation.setIsViewed(true);
        recommendationRepository.save(recommendation);
    }
}
