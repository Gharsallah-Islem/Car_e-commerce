"""
Recommendation API Routes
FastAPI endpoints for AI-powered product recommendations
"""

from fastapi import APIRouter, HTTPException, Query
from pydantic import BaseModel
from typing import List, Optional, Dict, Any
import logging
import httpx
import os

from ..services.recommendation_engine import get_recommendation_engine

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/v1/recommendations", tags=["recommendations"])

# Backend API URL for fetching product data
BACKEND_URL = os.getenv("BACKEND_URL", "http://localhost:8080")


class RecommendationResult(BaseModel):
    """Single recommendation result"""
    productId: str
    score: float
    reason: str
    recommendationType: str


class RecommendationResponse(BaseModel):
    """Response model for recommendations"""
    success: bool
    recommendations: List[RecommendationResult]
    count: int
    message: str


class PersonalizedRequest(BaseModel):
    """Request for personalized recommendations"""
    userId: str
    userActivities: List[Dict[str, Any]] = []
    products: List[Dict[str, Any]] = []
    limit: int = 10


class SimilarProductsRequest(BaseModel):
    """Request for similar products"""
    productId: str
    product: Dict[str, Any]
    allProducts: List[Dict[str, Any]] = []
    limit: int = 6


class AlsoBoughtRequest(BaseModel):
    """Request for also-bought products"""
    productId: str
    purchaseData: List[Dict[str, Any]] = []
    allProducts: List[Dict[str, Any]] = []
    limit: int = 6


class TrendingRequest(BaseModel):
    """Request for trending products"""
    trendingData: List[Dict[str, Any]] = []
    allProducts: List[Dict[str, Any]] = []
    limit: int = 10


@router.post("/personalized", response_model=RecommendationResponse)
async def get_personalized_recommendations(request: PersonalizedRequest):
    """
    Get personalized recommendations for a user.
    
    The Spring Boot backend should call this with:
    - userId: The user's UUID
    - userActivities: User's recent view/purchase activities
    - products: List of available products
    - limit: Max recommendations to return
    """
    try:
        engine = get_recommendation_engine()
        
        recommendations = engine.get_personalized_recommendations(
            user_id=request.userId,
            user_activities=request.userActivities,
            all_products=request.products,
            limit=request.limit
        )
        
        results = [
            RecommendationResult(**rec) for rec in recommendations
        ]
        
        return RecommendationResponse(
            success=True,
            recommendations=results,
            count=len(results),
            message=f"Found {len(results)} personalized recommendations"
        )
        
    except Exception as e:
        logger.error(f"Personalized recommendation error: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/similar", response_model=RecommendationResponse)
async def get_similar_products(request: SimilarProductsRequest):
    """
    Get products similar to a given product (content-based filtering).
    
    Args:
        productId: Source product UUID
        product: Source product data
        allProducts: List of all products to compare against
        limit: Max similar products
    """
    try:
        engine = get_recommendation_engine()
        
        recommendations = engine.get_similar_products(
            product_id=request.productId,
            product=request.product,
            all_products=request.allProducts,
            limit=request.limit
        )
        
        results = [
            RecommendationResult(**rec) for rec in recommendations
        ]
        
        return RecommendationResponse(
            success=True,
            recommendations=results,
            count=len(results),
            message=f"Found {len(results)} similar products"
        )
        
    except Exception as e:
        logger.error(f"Similar products error: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/also-bought", response_model=RecommendationResponse)
async def get_also_bought(request: AlsoBoughtRequest):
    """
    Get products frequently bought together (collaborative filtering).
    
    Args:
        productId: Source product UUID
        purchaseData: List of {productId, count} from collaborative analysis
        allProducts: List of products for validation
        limit: Max recommendations
    """
    try:
        engine = get_recommendation_engine()
        
        recommendations = engine.get_also_bought_products(
            product_id=request.productId,
            purchase_data=request.purchaseData,
            all_products=request.allProducts,
            limit=request.limit
        )
        
        results = [
            RecommendationResult(**rec) for rec in recommendations
        ]
        
        return RecommendationResponse(
            success=True,
            recommendations=results,
            count=len(results),
            message=f"Found {len(results)} also-bought products"
        )
        
    except Exception as e:
        logger.error(f"Also-bought error: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/trending", response_model=RecommendationResponse)
async def get_trending_products(request: TrendingRequest):
    """
    Get trending/popular products based on recent activity.
    
    Args:
        trendingData: List of {productId, viewCount} sorted by popularity
        allProducts: List of products for validation
        limit: Max trending products
    """
    try:
        engine = get_recommendation_engine()
        
        recommendations = engine.get_trending_products(
            trending_data=request.trendingData,
            all_products=request.allProducts,
            limit=request.limit
        )
        
        results = [
            RecommendationResult(**rec) for rec in recommendations
        ]
        
        return RecommendationResponse(
            success=True,
            recommendations=results,
            count=len(results),
            message=f"Found {len(results)} trending products"
        )
        
    except Exception as e:
        logger.error(f"Trending products error: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/health")
async def recommendation_health():
    """Health check for recommendation service"""
    try:
        engine = get_recommendation_engine()
        return {
            "status": "healthy",
            "service": "recommendation-engine",
            "backend_url": BACKEND_URL
        }
    except Exception as e:
        return {
            "status": "unhealthy",
            "error": str(e)
        }
