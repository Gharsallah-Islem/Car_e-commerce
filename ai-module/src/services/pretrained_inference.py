"""
Inference Service using Pre-trained Model

This service loads the pre-trained EfficientNetB2 model and provides
prediction capabilities for car parts classification.
"""

import tensorflow as tf
import numpy as np
from PIL import Image
import json
from pathlib import Path

class PretrainedInferenceService:
    def __init__(self, model_path='models/baseline_model.h5', labels_path='models/class_labels.json'):
        """
        Initialize inference service with pre-trained model
        
        Args:
            model_path: Path to the pre-trained .h5 model file
            labels_path: Path to the class labels JSON file
        """
        self.model_path = model_path
        self.labels_path = labels_path
        self.model = None
        self.class_labels = None
        self.img_size = 224
        
        self._load_model()
        self._load_labels()
    
    def _load_model(self):
        """Load the pre-trained model"""
        if not Path(self.model_path).exists():
            raise FileNotFoundError(
                f"Model not found at {self.model_path}. "
                "Please ensure the model file exists."
            )
        
        print(f"Loading model from {self.model_path}...")
        
        # Custom DepthwiseConv2D to handle version compatibility
        # Newer TensorFlow versions include 'groups' parameter that older versions don't support
        class CompatibleDepthwiseConv2D(tf.keras.layers.DepthwiseConv2D):
            def __init__(self, *args, **kwargs):
                # Remove 'groups' parameter if present (not supported in older TF versions)
                kwargs.pop('groups', None)
                super().__init__(*args, **kwargs)
        
        try:
            # Try loading with compile=False to avoid optimizer issues
            self.model = tf.keras.models.load_model(
                self.model_path, 
                compile=False
            )
            print("✓ Model loaded successfully!")
        except Exception as e:
            print(f"Warning: Load with default failed: {e}")
            print("Trying with custom_objects for version compatibility...")
            try:
                # Try with custom_objects for layer compatibility
                self.model = tf.keras.models.load_model(
                    self.model_path,
                    compile=False,
                    custom_objects={'DepthwiseConv2D': CompatibleDepthwiseConv2D}
                )
                print("✓ Model loaded with version-compatible layer!")
            except Exception as e2:
                print(f"Error loading model: {e2}")
                raise
    
    def _load_labels(self):
        """Load class labels from JSON file"""
        if not Path(self.labels_path).exists():
            raise FileNotFoundError(
                f"Labels file not found at {self.labels_path}."
            )
        
        with open(self.labels_path, 'r') as f:
            labels_dict = json.load(f)
        
        # Convert string keys to integers if needed
        if isinstance(list(labels_dict.keys())[0], str):
            self.class_labels = {int(k): v for k, v in labels_dict.items()}
        else:
            self.class_labels = labels_dict
            
        print(f"✓ Loaded {len(self.class_labels)} class labels")
    
    def preprocess_image(self, image_path):
        """
        Preprocess image for model input
        
        Args:
            image_path: Path to image file or PIL Image object
            
        Returns:
            Preprocessed image array
        """
        from tensorflow.keras.applications.efficientnet import preprocess_input
        
        # Load image
        if isinstance(image_path, str):
            img = Image.open(image_path)
            print(f"[DEBUG] Loaded image from file: {image_path}")
        else:
            img = image_path
            print(f"[DEBUG] Received PIL Image, mode: {img.mode}, size: {img.size}")
        
        # Convert to RGB if needed
        if img.mode != 'RGB':
            img = img.convert('RGB')
            print(f"[DEBUG] Converted to RGB")
        
        # Resize to model input size
        img = img.resize((self.img_size, self.img_size), Image.Resampling.LANCZOS)
        print(f"[DEBUG] Resized to {self.img_size}x{self.img_size}")
        
        # Convert to array - EfficientNet expects 0-255 range
        img_array = np.array(img, dtype=np.float32)
        print(f"[DEBUG] Array shape: {img_array.shape}, min: {img_array.min():.2f}, max: {img_array.max():.2f}")
        
        # Add batch dimension BEFORE preprocessing
        img_array = np.expand_dims(img_array, axis=0)
        
        # Apply EfficientNet preprocessing (scales to [-1, 1] range)
        img_array = preprocess_input(img_array)
        print(f"[DEBUG] After EfficientNet preprocessing: min: {img_array.min():.2f}, max: {img_array.max():.2f}")
        
        return img_array
    
    def predict(self, image_path, top_k=5):
        """
        Predict car part from image
        
        Args:
            image_path: Path to image file or PIL Image object
            top_k: Number of top predictions to return
            
        Returns:
            List of dicts with 'class', 'confidence' keys
        """
        # Preprocess image
        img_array = self.preprocess_image(image_path)
        
        # Get predictions
        predictions = self.model.predict(img_array, verbose=0)[0]
        
        # Get top K predictions
        top_indices = np.argsort(predictions)[-top_k:][::-1]
        
        results = []
        for idx in top_indices:
            results.append({
                'class': self.class_labels[idx],
                'confidence': float(predictions[idx]),
                'confidence_percent': f"{predictions[idx]*100:.2f}%"
            })
        
        return results
    
    def predict_batch(self, image_paths, top_k=5):
        """
        Predict multiple images at once
        
        Args:
            image_paths: List of image paths
            top_k: Number of top predictions per image
            
        Returns:
            List of prediction results
        """
        results = []
        for img_path in image_paths:
            results.append(self.predict(img_path, top_k))
        return results


# Example usage
if __name__ == "__main__":
    # Initialize service
    service = PretrainedInferenceService()
    
    # Test prediction (replace with actual image path)
    # predictions = service.predict('path/to/car_part_image.jpg')
    # print(predictions)
    
    print("\n✓ Inference service ready!")
    print(f"Model supports {len(service.class_labels)} car parts:")
    print(list(service.class_labels.values())[:10], "...")
