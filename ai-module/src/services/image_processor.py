"""
Image Processor Service
Handles image preprocessing, validation, and augmentation
"""

import cv2
import numpy as np
from PIL import Image
import io
import base64
from typing import Tuple, Optional
import logging

logger = logging.getLogger(__name__)

class ImageProcessor:
    """Image preprocessing and validation service"""
    
    def __init__(self, target_size: Tuple[int, int] = (224, 224)):
        """
        Initialize image processor
        
        Args:
            target_size: Target image size for model input (width, height)
        """
        self.target_size = target_size
        self.min_size = 100  # Minimum image dimension
        self.max_size = 4096  # Maximum image dimension
        self.max_file_size_mb = 10  # Maximum file size in MB
        
    def validate_image(self, image_data: bytes) -> Tuple[bool, str]:
        """
        Validate image data
        
        Args:
            image_data: Raw image bytes
            
        Returns:
            Tuple of (is_valid, error_message)
        """
        try:
            # Check file size
            size_mb = len(image_data) / (1024 * 1024)
            if size_mb > self.max_file_size_mb:
                return False, f"Image too large ({size_mb:.1f}MB). Max: {self.max_file_size_mb}MB"
            
            # Try to open image
            image = Image.open(io.BytesIO(image_data))
            
            # Check format
            if image.format not in ['JPEG', 'PNG', 'JPG', 'WEBP']:
                return False, f"Unsupported format: {image.format}. Use JPEG, PNG, or WEBP"
            
            # Check dimensions
            width, height = image.size
            if width < self.min_size or height < self.min_size:
                return False, f"Image too small ({width}x{height}). Min: {self.min_size}x{self.min_size}"
            
            if width > self.max_size or height > self.max_size:
                return False, f"Image too large ({width}x{height}). Max: {self.max_size}x{self.max_size}"
            
            # Check if image is corrupted
            image.verify()
            
            return True, "OK"
            
        except Exception as e:
            logger.error(f"Image validation error: {e}")
            return False, f"Invalid image: {str(e)}"
    
    def preprocess_for_inference(self, image_data: bytes) -> np.ndarray:
        """
        Preprocess image for model inference
        
        Args:
            image_data: Raw image bytes
            
        Returns:
            Preprocessed image array ready for model input
        """
        try:
            # Load image
            image = Image.open(io.BytesIO(image_data))
            
            # Convert to RGB if needed
            if image.mode != 'RGB':
                image = image.convert('RGB')
            
            # Resize to target size
            image = image.resize(self.target_size, Image.Resampling.LANCZOS)
            
            # Convert to numpy array
            img_array = np.array(image, dtype=np.float32)
            
            # Normalize to [0, 1]
            img_array = img_array / 255.0
            
            # Apply ImageNet normalization (for EfficientNet)
            mean = np.array([0.485, 0.456, 0.406])
            std = np.array([0.229, 0.224, 0.225])
            img_array = (img_array - mean) / std
            
            # Add batch dimension
            img_array = np.expand_dims(img_array, axis=0)
            
            return img_array
            
        except Exception as e:
            logger.error(f"Image preprocessing error: {e}")
            raise ValueError(f"Failed to preprocess image: {str(e)}")
    
    def preprocess_for_training(self, image_path: str, augment: bool = False) -> np.ndarray:
        """
        Preprocess image for model training
        
        Args:
            image_path: Path to image file
            augment: Whether to apply data augmentation
            
        Returns:
            Preprocessed image array
        """
        try:
            # Load image
            image = Image.open(image_path)
            
            # Convert to RGB
            if image.mode != 'RGB':
                image = image.convert('RGB')
            
            # Resize
            image = image.resize(self.target_size, Image.Resampling.LANCZOS)
            
            # Convert to numpy
            img_array = np.array(image, dtype=np.float32)
            
            # Apply augmentation if requested
            if augment:
                img_array = self._augment_image(img_array)
            
            # Normalize
            img_array = img_array / 255.0
            mean = np.array([0.485, 0.456, 0.406])
            std = np.array([0.229, 0.224, 0.225])
            img_array = (img_array - mean) / std
            
            return img_array
            
        except Exception as e:
            logger.error(f"Training preprocessing error: {e}")
            raise ValueError(f"Failed to preprocess image: {str(e)}")
    
    def _augment_image(self, img_array: np.ndarray) -> np.ndarray:
        """
        Apply data augmentation
        
        Args:
            img_array: Image array
            
        Returns:
            Augmented image array
        """
        # Random horizontal flip
        if np.random.random() > 0.5:
            img_array = np.fliplr(img_array)
        
        # Random rotation (-15 to +15 degrees)
        angle = np.random.uniform(-15, 15)
        h, w = img_array.shape[:2]
        center = (w // 2, h // 2)
        matrix = cv2.getRotationMatrix2D(center, angle, 1.0)
        img_array = cv2.warpAffine(img_array, matrix, (w, h))
        
        # Random brightness adjustment
        brightness_factor = np.random.uniform(0.8, 1.2)
        img_array = np.clip(img_array * brightness_factor, 0, 255)
        
        # Random contrast adjustment
        contrast_factor = np.random.uniform(0.8, 1.2)
        mean = img_array.mean()
        img_array = np.clip((img_array - mean) * contrast_factor + mean, 0, 255)
        
        return img_array
    
    def decode_base64_image(self, base64_string: str) -> bytes:
        """
        Decode base64 encoded image
        
        Args:
            base64_string: Base64 encoded image string
            
        Returns:
            Raw image bytes
        """
        try:
            # Remove data URL prefix if present
            if ',' in base64_string:
                base64_string = base64_string.split(',')[1]
            
            # Decode base64
            image_data = base64.b64decode(base64_string)
            return image_data
            
        except Exception as e:
            logger.error(f"Base64 decode error: {e}")
            raise ValueError(f"Failed to decode base64 image: {str(e)}")
    
    def check_image_quality(self, image_data: bytes) -> Tuple[str, float]:
        """
        Check image quality (blur detection)
        
        Args:
            image_data: Raw image bytes
            
        Returns:
            Tuple of (quality_level, blur_score)
            quality_level: 'good', 'fair', 'poor'
            blur_score: Higher is better (>100 is good)
        """
        try:
            # Load image
            image = Image.open(io.BytesIO(image_data))
            
            # Convert to grayscale
            gray = cv2.cvtColor(np.array(image), cv2.COLOR_RGB2GRAY)
            
            # Calculate Laplacian variance (blur detection)
            blur_score = cv2.Laplacian(gray, cv2.CV_64F).var()
            
            # Determine quality level
            if blur_score > 100:
                quality = 'good'
            elif blur_score > 50:
                quality = 'fair'
            else:
                quality = 'poor'
            
            return quality, blur_score
            
        except Exception as e:
            logger.error(f"Quality check error: {e}")
            return 'unknown', 0.0
    
    def remove_background(self, image_data: bytes) -> bytes:
        """
        Remove background from image (optional enhancement)
        
        Args:
            image_data: Raw image bytes
            
        Returns:
            Image with background removed
        """
        # TODO: Implement background removal using rembg or similar
        # For now, return original image
        return image_data
    
    def create_thumbnail(self, image_data: bytes, size: Tuple[int, int] = (128, 128)) -> bytes:
        """
        Create thumbnail from image
        
        Args:
            image_data: Raw image bytes
            size: Thumbnail size (width, height)
            
        Returns:
            Thumbnail image bytes
        """
        try:
            image = Image.open(io.BytesIO(image_data))
            image.thumbnail(size, Image.Resampling.LANCZOS)
            
            # Convert to bytes
            buffer = io.BytesIO()
            image.save(buffer, format='JPEG', quality=85)
            return buffer.getvalue()
            
        except Exception as e:
            logger.error(f"Thumbnail creation error: {e}")
            raise ValueError(f"Failed to create thumbnail: {str(e)}")
