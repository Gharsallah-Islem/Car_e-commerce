"""
Visual Search API Routes
FastAPI endpoints for AI-powered car part recognition
"""

from fastapi import APIRouter, HTTPException
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from typing import List, Optional
import logging
import base64
import io
import os
from pathlib import Path
from PIL import Image

from ..services.pretrained_inference import PretrainedInferenceService
from ..services.image_processor import ImageProcessor

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/v1/visual-search", tags=["visual-search"])

# Compute base path for models (relative to ai-module directory)
# This file is at: ai-module/src/api/visual_search_routes.py
# Models are at: ai-module/models/
BASE_DIR = Path(__file__).resolve().parent.parent.parent  # ai-module/
MODELS_DIR = BASE_DIR / "models"

# Initialize services (lazy loading to handle missing model gracefully)
inference_service: Optional[PretrainedInferenceService] = None
image_processor = ImageProcessor()


def get_inference_service() -> PretrainedInferenceService:
    """Lazy load the inference service"""
    global inference_service
    if inference_service is None:
        pretrained_model = MODELS_DIR / "pretrained_model.h5"
        pretrained_labels = MODELS_DIR / "pretrained_labels.json"
        baseline_model = MODELS_DIR / "baseline_model.h5"
        baseline_labels = MODELS_DIR / "class_labels.json"
        
        logger.info(f"Looking for models in: {MODELS_DIR}")
        logger.info(f"Pretrained model exists: {pretrained_model.exists()}")
        logger.info(f"Baseline model exists: {baseline_model.exists()}")
        
        try:
            if pretrained_model.exists() and pretrained_labels.exists():
                # Try pretrained model first (50 classes)
                inference_service = PretrainedInferenceService(
                    model_path=str(pretrained_model),
                    labels_path=str(pretrained_labels)
                )
                logger.info("✓ Loaded pretrained model (50 classes)")
            elif baseline_model.exists() and baseline_labels.exists():
                # Fallback to baseline model
                inference_service = PretrainedInferenceService(
                    model_path=str(baseline_model),
                    labels_path=str(baseline_labels)
                )
                logger.info("✓ Loaded baseline model")
            else:
                raise FileNotFoundError(f"No model found in {MODELS_DIR}")
        except Exception as e:
            logger.error(f"Model loading error: {e}")
            raise HTTPException(
                status_code=503, 
                detail=f"AI model not available: {str(e)}"
            )
    return inference_service


class PredictRequest(BaseModel):
    """Request model for image prediction"""
    image: str  # Base64 encoded image


class PredictionResult(BaseModel):
    """Single prediction result"""
    class_name: str
    confidence: float
    confidence_percent: str


class PredictResponse(BaseModel):
    """Response model for predictions"""
    success: bool
    predictions: List[PredictionResult]
    top_prediction: str
    confidence: float
    message: str


@router.post("/predict", response_model=PredictResponse)
async def predict_car_part(request: PredictRequest):
    """
    Predict car part from uploaded image
    
    Args:
        request: PredictRequest with base64 encoded image
        
    Returns:
        PredictResponse with top predictions and confidence scores
    """
    try:
        logger.info("Received prediction request")
        
        # Validate input
        if not request.image:
            raise HTTPException(status_code=400, detail="Image data is required")
        
        # Decode base64 image
        try:
            image_data = image_processor.decode_base64_image(request.image)
        except Exception as e:
            logger.error(f"Failed to decode image: {e}")
            raise HTTPException(status_code=400, detail=f"Invalid image data: {str(e)}")
        
        # Validate image
        is_valid, error_msg = image_processor.validate_image(image_data)
        if not is_valid:
            raise HTTPException(status_code=400, detail=error_msg)
        
        # Check image quality (log warning but don't reject)
        quality, blur_score = image_processor.check_image_quality(image_data)
        if quality == 'poor':
            logger.warning(f"Low quality image uploaded (blur score: {blur_score:.1f})")
        
        # Convert bytes to PIL Image for prediction
        pil_image = Image.open(io.BytesIO(image_data))
        
        # Get predictions
        service = get_inference_service()
        predictions = service.predict(pil_image, top_k=5)
        
        # Format response
        formatted_predictions = [
            PredictionResult(
                class_name=pred['class'],
                confidence=pred['confidence'],
                confidence_percent=pred['confidence_percent']
            )
            for pred in predictions
        ]
        
        top_pred = predictions[0] if predictions else None
        
        response = PredictResponse(
            success=True,
            predictions=formatted_predictions,
            top_prediction=top_pred['class'] if top_pred else "Unknown",
            confidence=top_pred['confidence'] if top_pred else 0.0,
            message="Image analyzed successfully"
        )
        
        logger.info(f"Prediction complete: {top_pred['class']} ({top_pred['confidence']:.2%})")
        
        return response
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Prediction error: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Prediction failed: {str(e)}")


@router.get("/health")
async def visual_search_health():
    """
    Health check for visual search service
    
    Returns:
        Service status and model information
    """
    try:
        service = get_inference_service()
        num_classes = len(service.class_labels) if service.class_labels else 0
        
        return {
            "status": "healthy",
            "model_loaded": True,
            "num_classes": num_classes,
            "available_classes": list(service.class_labels.values()) if service.class_labels else []
        }
    except Exception as e:
        return {
            "status": "unhealthy",
            "model_loaded": False,
            "error": str(e)
        }


@router.get("/classes")
async def get_available_classes():
    """
    Get list of car parts the model can recognize
    
    Returns:
        List of class names
    """
    try:
        service = get_inference_service()
        classes = list(service.class_labels.values()) if service.class_labels else []
        
        return {
            "success": True,
            "count": len(classes),
            "classes": classes
        }
    except Exception as e:
        logger.error(f"Error getting classes: {e}")
        raise HTTPException(status_code=500, detail=str(e))
