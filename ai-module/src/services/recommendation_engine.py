"""
AI Recommendation Engine
Provides intelligent product recommendations using multiple strategies:
- Content-based filtering (similar products by category, brand, features)
- Collaborative filtering (users who bought X also bought Y)
- Trending/popular products
- Personalized recommendations based on user behavior
"""

import logging
from typing import List, Dict, Optional
from datetime import datetime, timedelta
import os

logger = logging.getLogger(__name__)


class RecommendationEngine:
    """
    Hybrid recommendation engine combining multiple strategies.
    Connects to the Spring Boot backend for product and activity data.
    """

    def __init__(self, backend_url: str = None):
        """
        Initialize the recommendation engine.
        
        Args:
            backend_url: URL of the Spring Boot backend API
        """
        self.backend_url = backend_url or os.getenv("BACKEND_URL", "http://localhost:8080")
        logger.info(f"RecommendationEngine initialized with backend: {self.backend_url}")

    def get_personalized_recommendations(
        self, 
        user_id: str, 
        user_activities: List[Dict],
        all_products: List[Dict],
        limit: int = 10
    ) -> List[Dict]:
        """
        Get personalized recommendations based on user's behavior.
        Combines signals from browsing history, purchase history, and category preferences.
        
        Args:
            user_id: User's UUID
            user_activities: List of user's recent activities
            all_products: List of all available products
            limit: Maximum number of recommendations
            
        Returns:
            List of product recommendations with scores and reasons
        """
        recommendations = []
        
        # Extract user preferences from activities
        viewed_products = set()
        purchased_products = set()
        preferred_categories = {}
        preferred_brands = {}
        
        for activity in user_activities:
            product_id = activity.get('productId')
            activity_type = activity.get('activityType')
            category_id = activity.get('categoryId')
            brand_id = activity.get('brandId')
            
            if activity_type == 'VIEW' and product_id:
                viewed_products.add(product_id)
            elif activity_type == 'PURCHASE' and product_id:
                purchased_products.add(product_id)
            
            # Track category preferences
            if category_id:
                preferred_categories[category_id] = preferred_categories.get(category_id, 0) + 1
            if brand_id:
                preferred_brands[brand_id] = preferred_brands.get(brand_id, 0) + 1
        
        # Score each product based on user preferences
        for product in all_products:
            product_id = product.get('id')
            
            # Skip already purchased products
            if product_id in purchased_products:
                continue
            
            # Skip out of stock products
            if product.get('stock', 0) <= 0:
                continue
            
            score = 0.0
            reasons = []
            
            # Boost products in preferred categories
            category_id = product.get('categoryId')
            if category_id and category_id in preferred_categories:
                category_score = min(preferred_categories[category_id] * 0.1, 0.4)
                score += category_score
                reasons.append("Based on your category interests")
            
            # Boost products from preferred brands
            brand_id = product.get('brandId')
            if brand_id and brand_id in preferred_brands:
                brand_score = min(preferred_brands[brand_id] * 0.1, 0.3)
                score += brand_score
                reasons.append("From a brand you like")
            
            # Slight boost for products user hasn't viewed yet (discovery)
            if product_id not in viewed_products:
                score += 0.1
                reasons.append("New for you")
            
            # Base score for in-stock products
            score += 0.2
            
            if score > 0.2:  # Only include products with meaningful scores
                recommendations.append({
                    'productId': product_id,
                    'score': min(score, 1.0),
                    'reason': reasons[0] if reasons else "Recommended for you",
                    'recommendationType': 'PERSONALIZED'
                })
        
        # Sort by score and limit
        recommendations.sort(key=lambda x: x['score'], reverse=True)
        return recommendations[:limit]

    def get_similar_products(
        self,
        product_id: str,
        product: Dict,
        all_products: List[Dict],
        limit: int = 6
    ) -> List[Dict]:
        """
        Get products similar to the given product (content-based filtering).
        Matches by category, brand, price range, and features.
        
        Args:
            product_id: Source product UUID
            product: Source product data
            all_products: List of all available products
            limit: Maximum number of similar products
            
        Returns:
            List of similar product recommendations
        """
        similar = []
        
        source_category = product.get('categoryId')
        source_brand = product.get('brandId')
        source_price = product.get('price', 0)
        
        for candidate in all_products:
            candidate_id = candidate.get('id')
            
            # Skip the source product itself
            if candidate_id == product_id:
                continue
            
            # Skip out of stock
            if candidate.get('stock', 0) <= 0:
                continue
            
            score = 0.0
            reason = "Similar product"
            
            # Same category is a strong signal
            if candidate.get('categoryId') == source_category:
                score += 0.5
                reason = "Same category"
            
            # Same brand
            if candidate.get('brandId') == source_brand:
                score += 0.3
                reason = "Same brand"
            
            # Similar price range (within 30%)
            candidate_price = candidate.get('price', 0)
            if source_price > 0 and candidate_price > 0:
                price_diff = abs(candidate_price - source_price) / source_price
                if price_diff < 0.3:
                    score += 0.2
                    if "Same" not in reason:
                        reason = "Similar price range"
            
            if score > 0:
                similar.append({
                    'productId': candidate_id,
                    'score': min(score, 1.0),
                    'reason': reason,
                    'recommendationType': 'SIMILAR'
                })
        
        # Sort by score
        similar.sort(key=lambda x: x['score'], reverse=True)
        return similar[:limit]

    def get_also_bought_products(
        self,
        product_id: str,
        purchase_data: List[Dict],
        all_products: List[Dict],
        limit: int = 6
    ) -> List[Dict]:
        """
        Get products frequently bought together (collaborative filtering).
        
        Args:
            product_id: Source product UUID
            purchase_data: List of {productId, count} for products bought together
            all_products: List of all available products for enrichment
            limit: Maximum number of recommendations
            
        Returns:
            List of "also bought" recommendations
        """
        recommendations = []
        
        # Create a lookup for product availability
        available_products = {p['id']: p for p in all_products if p.get('stock', 0) > 0}
        
        # Get max count for normalization
        max_count = max((d.get('count', 1) for d in purchase_data), default=1)
        
        for data in purchase_data:
            candidate_id = data.get('productId')
            count = data.get('count', 1)
            
            # Skip the source product
            if candidate_id == product_id:
                continue
            
            # Skip unavailable products
            if candidate_id not in available_products:
                continue
            
            # Normalize score based on purchase frequency
            score = min(count / max(max_count, 1), 1.0)
            
            recommendations.append({
                'productId': candidate_id,
                'score': score,
                'reason': "Customers also bought",
                'recommendationType': 'ALSO_BOUGHT'
            })
        
        recommendations.sort(key=lambda x: x['score'], reverse=True)
        return recommendations[:limit]

    def get_trending_products(
        self,
        trending_data: List[Dict],
        all_products: List[Dict],
        limit: int = 10
    ) -> List[Dict]:
        """
        Get trending/popular products based on recent activity.
        
        Args:
            trending_data: List of {productId, viewCount} sorted by popularity
            all_products: List of all available products
            limit: Maximum number of trending products
            
        Returns:
            List of trending product recommendations
        """
        recommendations = []
        
        available_products = {p['id']: p for p in all_products if p.get('stock', 0) > 0}
        
        # Get max views for normalization
        max_views = max((d.get('viewCount', 1) for d in trending_data), default=1)
        
        for data in trending_data:
            product_id = data.get('productId')
            view_count = data.get('viewCount', 1)
            
            if product_id not in available_products:
                continue
            
            score = min(view_count / max(max_views, 1), 1.0)
            
            recommendations.append({
                'productId': product_id,
                'score': score,
                'reason': "Trending now",
                'recommendationType': 'TRENDING'
            })
        
        return recommendations[:limit]

    def combine_recommendations(
        self,
        *recommendation_lists: List[Dict],
        limit: int = 10
    ) -> List[Dict]:
        """
        Combine multiple recommendation lists, removing duplicates and re-ranking.
        
        Args:
            recommendation_lists: Variable number of recommendation lists
            limit: Maximum final recommendations
            
        Returns:
            Combined and deduplicated recommendations
        """
        seen = set()
        combined = []
        
        for rec_list in recommendation_lists:
            for rec in rec_list:
                product_id = rec.get('productId')
                if product_id not in seen:
                    seen.add(product_id)
                    combined.append(rec)
        
        # Sort by score
        combined.sort(key=lambda x: x.get('score', 0), reverse=True)
        return combined[:limit]


# Singleton instance
_engine_instance: Optional[RecommendationEngine] = None


def get_recommendation_engine() -> RecommendationEngine:
    """Get or create the recommendation engine singleton."""
    global _engine_instance
    if _engine_instance is None:
        _engine_instance = RecommendationEngine()
    return _engine_instance
