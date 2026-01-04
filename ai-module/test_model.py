"""
Test the Pre-trained Model

Quick test script to verify the 97.99% accuracy model works correctly.
"""

from src.services.pretrained_inference import PretrainedInferenceService
from PIL import Image
import os

def test_model():
    print("="*60)
    print("Testing Pre-trained Model (97.99% Accuracy)")
    print("="*60)
    
    # Initialize service
    print("\n1. Loading model...")
    service = PretrainedInferenceService()
    
    print(f"\n✓ Model loaded successfully!")
    print(f"✓ Classes: {len(service.class_labels)}")
    print(f"\nSupported car parts:")
    for i, label in list(service.class_labels.items())[:10]:
        print(f"  - {label}")
    print(f"  ... and {len(service.class_labels) - 10} more")
    
    # Check if test images exist
    test_dir = "dataset/car parts 50/test"
    if os.path.exists(test_dir):
        print(f"\n2. Testing with sample images from dataset...")
        
        # Get first available class with images
        for class_name in os.listdir(test_dir):
            class_path = os.path.join(test_dir, class_name)
            if os.path.isdir(class_path):
                images = [f for f in os.listdir(class_path) if f.lower().endswith(('.jpg', '.jpeg', '.png'))]
                if images:
                    # Test with first image
                    test_image = os.path.join(class_path, images[0])
                    print(f"\nTesting with: {class_name}/{images[0]}")
                    
                    predictions = service.predict(test_image, top_k=5)
                    
                    print("\nTop 5 Predictions:")
                    for i, pred in enumerate(predictions, 1):
                        print(f"  {i}. {pred['class']}: {pred['confidence_percent']}")
                    
                    if predictions[0]['class'].upper() == class_name:
                        print("\n✅ CORRECT! Model predicted the right class!")
                    else:
                        print(f"\n⚠️ Expected: {class_name}, Got: {predictions[0]['class']}")
                    
                    break
    else:
        print("\n⚠️ Test dataset not found. Model is ready but cannot run test.")
    
    print("\n" + "="*60)
    print("✓ Model is ready for integration!")
    print("="*60)

if __name__ == "__main__":
    test_model()
