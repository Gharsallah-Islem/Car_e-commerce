"""
Dataset Downloader
Downloads and prepares car parts datasets from Kaggle and other sources
"""

import os
import sys
import zipfile
import requests
from pathlib import Path
import json
import shutil

class DatasetDownloader:
    """Download and prepare car parts datasets"""
    
    def __init__(self, data_root="./data"):
        self.data_root = Path(data_root)
        self.data_root.mkdir(exist_ok=True)
        
    def download_kaggle_dataset(self, dataset_name, output_dir):
        """
        Download dataset from Kaggle
        
        Args:
            dataset_name: Kaggle dataset name (e.g., 'username/dataset-name')
            output_dir: Directory to extract dataset
        """
        print(f"\n{'='*60}")
        print(f"Downloading Kaggle Dataset: {dataset_name}")
        print(f"{'='*60}\n")
        
        try:
            # Check if kaggle is installed
            import kaggle
            
            # Download dataset
            print("Downloading... This may take a few minutes...")
            kaggle.api.dataset_download_files(
                dataset_name,
                path=output_dir,
                unzip=True
            )
            
            print(f"\n✓ Dataset downloaded successfully to: {output_dir}")
            return True
            
        except ImportError:
            print("\n✗ Kaggle API not installed!")
            print("\nTo install:")
            print("  pip install kaggle")
            print("\nThen configure your API key:")
            print("  1. Go to https://www.kaggle.com/settings")
            print("  2. Click 'Create New API Token'")
            print("  3. Save kaggle.json to: C:\\Users\\<username>\\.kaggle\\kaggle.json")
            return False
            
        except Exception as e:
            print(f"\n✗ Download failed: {e}")
            print("\nMake sure:")
            print("  1. Kaggle API is configured (kaggle.json in ~/.kaggle/)")
            print("  2. You have accepted the dataset's terms on Kaggle website")
            print("  3. Dataset name is correct")
            return False
    
    def download_50_car_parts_dataset(self):
        """
        Download the '50 Types of Car Parts' dataset from Kaggle
        
        Dataset: https://www.kaggle.com/datasets/tolgadincer/50-types-of-car-parts-image-classification
        - 50 different car part types
        - 224x224 images
        - Train/Val/Test splits
        """
        dataset_name = "tolgadincer/50-types-of-car-parts-image-classification"
        output_dir = self.data_root / "kaggle_50_car_parts"
        
        success = self.download_kaggle_dataset(dataset_name, output_dir)
        
        if success:
            # Organize dataset
            self._organize_50_parts_dataset(output_dir)
        
        return success
    
    def download_14_automobile_parts_dataset(self):
        """
        Download the '14 Automobile Parts' dataset from Kaggle
        
        Dataset: https://www.kaggle.com/datasets/tolgadincer/image-classification-automobile-parts
        - 14 different automobile spare parts
        - Train/Val/Test splits
        """
        dataset_name = "tolgadincer/image-classification-automobile-parts"
        output_dir = self.data_root / "kaggle_14_auto_parts"
        
        success = self.download_kaggle_dataset(dataset_name, output_dir)
        
        if success:
            # Organize dataset
            self._organize_14_parts_dataset(output_dir)
        
        return success
    
    def _organize_50_parts_dataset(self, dataset_dir):
        """Organize 50 car parts dataset into our structure"""
        print("\nOrganizing dataset...")
        
        # Expected structure: train/, val/, test/ with class subdirectories
        train_dir = dataset_dir / "train"
        val_dir = dataset_dir / "val"
        test_dir = dataset_dir / "test"
        
        if train_dir.exists():
            # Copy to our spare_parts directory
            target_dir = self.data_root / "spare_parts"
            target_dir.mkdir(exist_ok=True)
            
            # Copy training images
            for class_dir in train_dir.iterdir():
                if class_dir.is_dir():
                    target_class = target_dir / class_dir.name
                    if not target_class.exists():
                        shutil.copytree(class_dir, target_class)
                        print(f"  ✓ Copied {class_dir.name}")
            
            print(f"\n✓ Dataset organized in: {target_dir}")
        else:
            print(f"\n⚠ Warning: Expected directory structure not found")
            print(f"  Please check: {dataset_dir}")
    
    def _organize_14_parts_dataset(self, dataset_dir):
        """Organize 14 automobile parts dataset into our structure"""
        print("\nOrganizing dataset...")
        
        # Similar to 50 parts dataset
        train_dir = dataset_dir / "train"
        
        if train_dir.exists():
            target_dir = self.data_root / "spare_parts"
            target_dir.mkdir(exist_ok=True)
            
            for class_dir in train_dir.iterdir():
                if class_dir.is_dir():
                    target_class = target_dir / class_dir.name
                    if not target_class.exists():
                        shutil.copytree(class_dir, target_class)
                        print(f"  ✓ Copied {class_dir.name}")
            
            print(f"\n✓ Dataset organized in: {target_dir}")
        else:
            print(f"\n⚠ Warning: Expected directory structure not found")
    
    def download_sample_dataset(self):
        """
        Download a small sample dataset for testing
        (Useful if Kaggle API is not configured)
        """
        print("\n" + "="*60)
        print("Creating Sample Dataset")
        print("="*60 + "\n")
        
        # Create sample categories
        categories = ["brakes", "filters", "engine_parts"]
        sample_dir = self.data_root / "spare_parts"
        sample_dir.mkdir(exist_ok=True)
        
        print("Creating sample structure...")
        for category in categories:
            category_dir = sample_dir / category
            category_dir.mkdir(exist_ok=True)
            print(f"  ✓ Created {category}")
        
        print(f"\n✓ Sample structure created in: {sample_dir}")
        print("\nNOTE: This is just a folder structure.")
        print("You'll need to add images manually or use Kaggle datasets.")
        
        return True
    
    def get_dataset_info(self):
        """Get information about downloaded datasets"""
        spare_parts_dir = self.data_root / "spare_parts"
        
        if not spare_parts_dir.exists():
            return {"status": "No dataset found", "categories": 0, "images": 0}
        
        categories = [d for d in spare_parts_dir.iterdir() if d.is_dir()]
        total_images = 0
        
        for category in categories:
            images = list(category.glob("**/*.jpg")) + list(category.glob("**/*.png"))
            total_images += len(images)
        
        return {
            "status": "Dataset found",
            "categories": len(categories),
            "images": total_images,
            "category_names": [c.name for c in categories]
        }

def main():
    """Main function"""
    print("\n" + "="*60)
    print("CAR PARTS DATASET DOWNLOADER")
    print("="*60)
    
    downloader = DatasetDownloader()
    
    print("\nAvailable datasets:")
    print("1. 50 Types of Car Parts (Kaggle) - Recommended")
    print("2. 14 Automobile Parts (Kaggle)")
    print("3. Create sample structure (for manual upload)")
    print("4. Check current dataset status")
    print("5. Exit")
    
    choice = input("\nEnter your choice (1-5): ").strip()
    
    if choice == "1":
        success = downloader.download_50_car_parts_dataset()
        if success:
            info = downloader.get_dataset_info()
            print(f"\n{'='*60}")
            print("DATASET SUMMARY")
            print(f"{'='*60}")
            print(f"Categories: {info['categories']}")
            print(f"Total Images: {info['images']}")
            print(f"\nYou're ready to train the model!")
            
    elif choice == "2":
        success = downloader.download_14_automobile_parts_dataset()
        if success:
            info = downloader.get_dataset_info()
            print(f"\n{'='*60}")
            print("DATASET SUMMARY")
            print(f"{'='*60}")
            print(f"Categories: {info['categories']}")
            print(f"Total Images: {info['images']}")
            
    elif choice == "3":
        downloader.download_sample_dataset()
        
    elif choice == "4":
        info = downloader.get_dataset_info()
        print(f"\n{'='*60}")
        print("CURRENT DATASET STATUS")
        print(f"{'='*60}")
        print(f"Status: {info['status']}")
        print(f"Categories: {info['categories']}")
        print(f"Total Images: {info['images']}")
        if info['categories'] > 0:
            print(f"\nCategories found:")
            for cat in info['category_names']:
                print(f"  - {cat}")
    
    else:
        print("\nExiting...")

if __name__ == "__main__":
    main()
