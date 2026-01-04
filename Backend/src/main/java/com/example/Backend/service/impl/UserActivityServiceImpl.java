package com.example.Backend.service.impl;

import com.example.Backend.dto.UserActivityDTO;
import com.example.Backend.entity.ActivityType;
import com.example.Backend.entity.Product;
import com.example.Backend.entity.User;
import com.example.Backend.entity.UserActivity;
import com.example.Backend.repository.ProductRepository;
import com.example.Backend.repository.UserActivityRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.UserActivityService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of UserActivityService for tracking user behavior.
 * Used by the recommendation engine to personalize product suggestions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserActivityServiceImpl implements UserActivityService {

    private final UserActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // Minimum time between duplicate view events (5 minutes)
    private static final int DUPLICATE_VIEW_MINUTES = 5;

    @Override
    @Transactional
    public UserActivity trackActivity(UUID userId, UserActivityDTO activityDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        UserActivity activity = new UserActivity();
        activity.setUser(user);
        activity.setActivityType(activityDTO.getActivityType());
        activity.setSessionId(activityDTO.getSessionId());
        activity.setSearchQuery(activityDTO.getSearchQuery());
        activity.setCategoryId(activityDTO.getCategoryId());
        activity.setMetadata(activityDTO.getMetadata());

        if (activityDTO.getProductId() != null) {
            Product product = productRepository.findById(activityDTO.getProductId())
                    .orElse(null);
            activity.setProduct(product);
        }

        log.debug("Tracking activity: {} for user: {}", activityDTO.getActivityType(), userId);
        return activityRepository.save(activity);
    }

    @Override
    @Transactional
    public UserActivity trackProductView(UUID userId, UUID productId, String sessionId) {
        // Prevent duplicate views within short timeframe
        LocalDateTime since = LocalDateTime.now().minusMinutes(DUPLICATE_VIEW_MINUTES);
        if (activityRepository.hasRecentView(userId, productId, since)) {
            log.debug("Skipping duplicate view for product {} by user {}", productId, userId);
            return null;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        UserActivity activity = new UserActivity(user, product, ActivityType.VIEW);
        activity.setSessionId(sessionId);

        log.info("Tracked product view: {} by user: {}", productId, userId);
        return activityRepository.save(activity);
    }

    @Override
    @Transactional
    public UserActivity trackAddToCart(UUID userId, UUID productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        UserActivity activity = new UserActivity(user, product, ActivityType.ADD_TO_CART);

        log.info("Tracked add to cart: {} by user: {}", productId, userId);
        return activityRepository.save(activity);
    }

    @Override
    @Transactional
    public List<UserActivity> trackPurchase(UUID userId, List<UUID> productIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        List<UserActivity> activities = new ArrayList<>();

        for (UUID productId : productIds) {
            Product product = productRepository.findById(productId).orElse(null);
            if (product != null) {
                UserActivity activity = new UserActivity(user, product, ActivityType.PURCHASE);
                activities.add(activityRepository.save(activity));
            }
        }

        log.info("Tracked purchase of {} products by user: {}", productIds.size(), userId);
        return activities;
    }

    @Override
    @Transactional
    public UserActivity trackSearch(UUID userId, String searchQuery) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        UserActivity activity = new UserActivity(user, searchQuery, ActivityType.SEARCH);

        log.debug("Tracked search '{}' by user: {}", searchQuery, userId);
        return activityRepository.save(activity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> getRecentlyViewedProducts(UUID userId, int limit) {
        return activityRepository.findRecentlyViewedProductIds(userId, PageRequest.of(0, limit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getUserCategoryPreferences(UUID userId) {
        return activityRepository.findUserCategoryPreferences(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getTrendingProducts(int days, int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return activityRepository.findTrendingProductIds(since, PageRequest.of(0, limit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getAlsoBoughtProducts(UUID productId, int limit) {
        return activityRepository.findAlsoBoughtProductIds(productId, PageRequest.of(0, limit));
    }

    @Override
    @Transactional(readOnly = true)
    public Long getActivityCount(UUID userId, ActivityType activityType) {
        return activityRepository.countByUserIdAndActivityType(userId, activityType);
    }
}
