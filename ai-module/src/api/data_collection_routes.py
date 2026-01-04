"""
Data Collection API Routes
FastAPI endpoints for collecting training data from admin interface
"""

from fastapi import APIRouter, UploadFile, File, Form, HTTPException
from fastapi.responses import JSONResponse
from typing import List, Optional
import logging
import os
from pathlib import Path
import shutil
import uuid

from ..services.image_processor import ImageProcessor
from ..database.dataset_manager import DatasetManager

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/v1/data-collection", tags=["data-collection"])

# Initialize services
image_processor = ImageProcessor()
dataset_manager = DatasetManager()

@router.post("/upload")
async def upload_product_images(
    product_id: str = Form(...),
    product_name: str = Form(...),
    category: str = Form(...),
    images: List[UploadFile] = File(...)
):
    """
    Upload product images for training dataset
    
    Args:
        product_id: Unique product ID (UUID)
        product_name: Product name for metadata
        category: Product category (e.g., 'brakes', 'engine_parts')
        images: List of image files
        
    Returns:
        Upload status and statistics
    """
    try:
        logger.info(f"Uploading images for product {product_id} ({product_name})")
        
        # Validate inputs
        if not product_id or not category:
            raise HTTPException(status_code=400, detail="Product ID and category are required")
        
        if not images:
            raise HTTPException(status_code=400, detail="At least one image is required")
        
        # Create temporary directory for uploads
        temp_dir = Path("./temp") / str(uuid.uuid4())
        temp_dir.mkdir(parents=True, exist_ok=True)
        
        uploaded_files = []
        validation_errors = []
        
        # Process each image
        for idx, image in enumerate(images):
            try:
                # Read image data
                image_data = await image.read()
                
                # Validate image
                is_valid, error_msg = image_processor.validate_image(image_data)
                if not is_valid:
                    validation_errors.append(f"{image.filename}: {error_msg}")
                    continue
                
                # Check image quality
                quality, blur_score = image_processor.check_image_quality(image_data)
                if quality == 'poor':
                    validation_errors.append(
                        f"{image.filename}: Poor quality (blurry). Blur score: {blur_score:.1f}"
                    )
                    # Still allow upload but warn user
                
                # Save to temp directory
                file_ext = Path(image.filename).suffix
                temp_path = temp_dir / f"{product_id}_{idx:03d}{file_ext}"
                
                with open(temp_path, 'wb') as f:
                    f.write(image_data)
                
                uploaded_files.append(str(temp_path))
                
            except Exception as e:
                logger.error(f"Error processing image {image.filename}: {e}")
                validation_errors.append(f"{image.filename}: {str(e)}")
        
        # Add images to dataset
        if uploaded_files:
            added_count = dataset_manager.add_product_images(
                product_id=product_id,
                category=category,
                image_paths=uploaded_files,
                product_name=product_name
            )
        else:
            added_count = 0
        
        # Clean up temp directory
        shutil.rmtree(temp_dir, ignore_errors=True)
        
        # Get updated stats
        stats = dataset_manager.get_dataset_stats()
        
        return {
            "success": True,
            "uploaded_count": added_count,
            "total_images": len(images),
            "validation_errors": validation_errors,
            "dataset_stats": stats
        }
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Upload error: {e}")
        raise HTTPException(status_code=500, detail=f"Upload failed: {str(e)}")

@router.get("/stats")
async def get_dataset_stats():
    """
    Get current dataset statistics
    
    Returns:
        Dataset statistics including image counts per category
    """
    try:
        stats = dataset_manager.get_dataset_stats()
        return stats
    except Exception as e:
        logger.error(f"Error getting stats: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/categories")
async def get_categories():
    """
    Get list of available categories
    
    Returns:
        List of category names
    """
    try:
        stats = dataset_manager.get_dataset_stats()
        return {
            "categories": list(stats["categories"].keys())
        }
    except Exception as e:
        logger.error(f"Error getting categories: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/category")
async def create_category(category_name: str = Form(...)):
    """
    Create a new category
    
    Args:
        category_name: Name of the category to create
        
    Returns:
        Success status
    """
    try:
        dataset_manager.create_category(category_name)
        return {
            "success": True,
            "category": category_name
        }
    except Exception as e:
        logger.error(f"Error creating category: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/validate")
async def validate_dataset(min_images: int = 50):
    """
    Validate dataset is ready for training
    
    Args:
        min_images: Minimum images required per category
        
    Returns:
        Validation status and any issues
    """
    try:
        is_valid, issues = dataset_manager.validate_dataset(min_images)
        return {
            "is_valid": is_valid,
            "issues": issues,
            "min_images_required": min_images
        }
    except Exception as e:
        logger.error(f"Validation error: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/report")
async def get_dataset_report():
    """
    Get detailed dataset report
    
    Returns:
        Detailed report as text
    """
    try:
        report = dataset_manager.export_dataset_report()
        return {
            "report": report
        }
    except Exception as e:
        logger.error(f"Report generation error: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@router.delete("/product/{product_id}")
async def delete_product_images(product_id: str, category: str):
    """
    Delete all images for a product
    
    Args:
        product_id: Product ID to delete
        category: Category the product belongs to
        
    Returns:
        Deletion status
    """
    try:
        # TODO: Implement deletion logic
        # For now, return not implemented
        raise HTTPException(status_code=501, detail="Deletion not yet implemented")
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Deletion error: {e}")
        raise HTTPException(status_code=500, detail=str(e))
