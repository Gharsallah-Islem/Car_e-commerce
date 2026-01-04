"""
Download Pre-trained Car Parts Model from Kaggle

This script downloads a pre-trained EfficientNetB2 model with 96.9% accuracy
for 50 types of car parts classification.

Model: EfficientNetB2-40-(224 X 224)- 96.90.h5
Source: Kaggle - 50 Types of Car Parts Dataset
Accuracy: 96.9%
Classes: 50 car parts (internal & external)
"""

import os
import subprocess

def download_pretrained_model():
    """Download pre-trained model from Kaggle"""
    
    print("="*60)
    print("Downloading Pre-trained Car Parts Model")
    print("="*60)
    print("Model: EfficientNetB2")
    print("Accuracy: 96.9%")
    print("Classes: 50 car parts")
    print("="*60)
    
    # Create models directory
    os.makedirs('models', exist_ok=True)
    
    # Download dataset (includes pre-trained model)
    print("\n📥 Downloading from Kaggle...")
    try:
        subprocess.run([
            'kaggle', 'datasets', 'download', '-d',
            'ahmedabdelmadgid/50-types-of-car-parts-image-classification',
            '-p', 'models', '--unzip'
        ], check=True)
        
        print("\n✓ Download complete!")
        print("\nModel saved to: models/")
        print("Model file: EfficientNetB2-40-(224 X 224)- 96.90.h5")
        
        return True
        
    except subprocess.CalledProcessError as e:
        print(f"\n❌ Download failed: {e}")
        print("\nMANUAL DOWNLOAD INSTRUCTIONS:")
        print("1. Go to: https://www.kaggle.com/datasets/ahmedabdelmadgid/50-types-of-car-parts-image-classification")
        print("2. Click 'Download' button")
        print("3. Extract the ZIP file")
        print("4. Copy 'EfficientNetB2-40-(224 X 224)- 96.90.h5' to ai-module/models/")
        
        return False

if __name__ == "__main__":
    download_pretrained_model()
