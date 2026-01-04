"""
Dataset Manager
Handles dataset organization, loading, and management for training
"""

import os
import json
import shutil
from pathlib import Path
from typing import List, Dict, Tuple
import logging
from collections import defaultdict

logger = logging.getLogger(__name__)

class DatasetManager:
    """Manage training dataset organization and metadata"""
    
    def __init__(self, data_root: str = "./data"):
        """
        Initialize dataset manager
        
        Args:
            data_root: Root directory for datasets
        """
        self.data_root = Path(data_root)
        self.spare_parts_dir = self.data_root / "spare_parts"
        self.validation_dir = self.data_root / "validation"
        self.metadata_file = self.data_root / "dataset_metadata.json"
        
        # Create directories if they don't exist
        self.spare_parts_dir.mkdir(parents=True, exist_ok=True)
        self.validation_dir.mkdir(parents=True, exist_ok=True)
        
        # Load or initialize metadata
        self.metadata = self._load_metadata()
    
    def _load_metadata(self) -> Dict:
        """Load dataset metadata from file"""
        if self.metadata_file.exists():
            with open(self.metadata_file, 'r') as f:
                return json.load(f)
        else:
            return {
                "categories": {},
                "total_images": 0,
                "last_updated": None
            }
    
    def _save_metadata(self):
        """Save dataset metadata to file"""
        import datetime
        self.metadata["last_updated"] = datetime.datetime.now().isoformat()
        with open(self.metadata_file, 'w') as f:
            json.dump(self.metadata, f, indent=2)
    
    def create_category(self, category_name: str) -> Path:
        """
        Create a new category directory
        
        Args:
            category_name: Name of the category (e.g., 'brakes', 'engine_parts')
            
        Returns:
            Path to category directory
        """
        category_dir = self.spare_parts_dir / category_name
        category_dir.mkdir(exist_ok=True)
        
        if category_name not in self.metadata["categories"]:
            self.metadata["categories"][category_name] = {
                "image_count": 0,
                "products": []
            }
            self._save_metadata()
        
        logger.info(f"Created category: {category_name}")
        return category_dir
    
    def add_product_images(self, 
                          product_id: str,
                          category: str,
                          image_paths: List[str],
                          product_name: str = None) -> int:
        """
        Add product images to dataset
        
        Args:
            product_id: Unique product ID
            category: Product category
            image_paths: List of paths to image files
            product_name: Optional product name for metadata
            
        Returns:
            Number of images added
        """
        # Create category if it doesn't exist
        category_dir = self.create_category(category)
        
        # Create product subdirectory
        product_dir = category_dir / product_id
        product_dir.mkdir(exist_ok=True)
        
        # Copy images
        added_count = 0
        for i, image_path in enumerate(image_paths):
            try:
                src = Path(image_path)
                if not src.exists():
                    logger.warning(f"Image not found: {image_path}")
                    continue
                
                # Generate filename
                ext = src.suffix
                dst = product_dir / f"{product_id}_{i:03d}{ext}"
                
                # Copy file
                shutil.copy2(src, dst)
                added_count += 1
                
            except Exception as e:
                logger.error(f"Failed to copy image {image_path}: {e}")
        
        # Update metadata
        if category in self.metadata["categories"]:
            self.metadata["categories"][category]["image_count"] += added_count
            
            # Add product to list if not exists
            product_info = {
                "product_id": product_id,
                "name": product_name or product_id,
                "image_count": added_count
            }
            
            # Update or add product
            products = self.metadata["categories"][category]["products"]
            existing = next((p for p in products if p["product_id"] == product_id), None)
            if existing:
                existing["image_count"] += added_count
            else:
                products.append(product_info)
        
        self.metadata["total_images"] += added_count
        self._save_metadata()
        
        logger.info(f"Added {added_count} images for product {product_id} in category {category}")
        return added_count
    
    def get_dataset_stats(self) -> Dict:
        """
        Get dataset statistics
        
        Returns:
            Dictionary with dataset statistics
        """
        stats = {
            "total_images": self.metadata["total_images"],
            "total_categories": len(self.metadata["categories"]),
            "categories": {}
        }
        
        for category, info in self.metadata["categories"].items():
            stats["categories"][category] = {
                "image_count": info["image_count"],
                "product_count": len(info["products"])
            }
        
        return stats
    
    def get_training_data(self, validation_split: float = 0.2) -> Tuple[List, List]:
        """
        Get training and validation data paths
        
        Args:
            validation_split: Fraction of data to use for validation (0.0 to 1.0)
            
        Returns:
            Tuple of (training_data, validation_data)
            Each is a list of (image_path, category_index) tuples
        """
        import random
        
        all_data = []
        category_to_index = {}
        
        # Collect all images with their categories
        for idx, (category, info) in enumerate(self.metadata["categories"].items()):
            category_to_index[category] = idx
            category_dir = self.spare_parts_dir / category
            
            # Get all images in category
            for product_dir in category_dir.iterdir():
                if product_dir.is_dir():
                    for image_path in product_dir.glob("*"):
                        if image_path.suffix.lower() in ['.jpg', '.jpeg', '.png', '.webp']:
                            all_data.append((str(image_path), idx))
        
        # Shuffle data
        random.shuffle(all_data)
        
        # Split into training and validation
        split_idx = int(len(all_data) * (1 - validation_split))
        training_data = all_data[:split_idx]
        validation_data = all_data[split_idx:]
        
        logger.info(f"Dataset split: {len(training_data)} training, {len(validation_data)} validation")
        return training_data, validation_data
    
    def get_category_mapping(self) -> Dict[int, str]:
        """
        Get mapping from category index to category name
        
        Returns:
            Dictionary mapping index to category name
        """
        return {idx: category for idx, category in enumerate(self.metadata["categories"].keys())}
    
    def validate_dataset(self, min_images_per_category: int = 50) -> Tuple[bool, List[str]]:
        """
        Validate dataset is ready for training
        
        Args:
            min_images_per_category: Minimum images required per category
            
        Returns:
            Tuple of (is_valid, issues)
        """
        issues = []
        
        # Check if we have any categories
        if not self.metadata["categories"]:
            issues.append("No categories found. Please add training data.")
            return False, issues
        
        # Check each category
        for category, info in self.metadata["categories"].items():
            image_count = info["image_count"]
            
            if image_count < min_images_per_category:
                issues.append(
                    f"Category '{category}' has only {image_count} images. "
                    f"Minimum required: {min_images_per_category}"
                )
        
        is_valid = len(issues) == 0
        
        if is_valid:
            logger.info("Dataset validation passed!")
        else:
            logger.warning(f"Dataset validation failed with {len(issues)} issues")
        
        return is_valid, issues
    
    def export_dataset_report(self, output_path: str = None) -> str:
        """
        Export detailed dataset report
        
        Args:
            output_path: Path to save report (default: data/dataset_report.txt)
            
        Returns:
            Report content as string
        """
        if output_path is None:
            output_path = self.data_root / "dataset_report.txt"
        
        report_lines = []
        report_lines.append("=" * 60)
        report_lines.append("DATASET REPORT")
        report_lines.append("=" * 60)
        report_lines.append(f"\nTotal Images: {self.metadata['total_images']}")
        report_lines.append(f"Total Categories: {len(self.metadata['categories'])}")
        report_lines.append(f"Last Updated: {self.metadata.get('last_updated', 'Unknown')}")
        
        report_lines.append("\n" + "=" * 60)
        report_lines.append("CATEGORIES")
        report_lines.append("=" * 60)
        
        for category, info in sorted(self.metadata["categories"].items()):
            report_lines.append(f"\n{category}:")
            report_lines.append(f"  Images: {info['image_count']}")
            report_lines.append(f"  Products: {len(info['products'])}")
            
            # List products
            for product in info['products']:
                report_lines.append(f"    - {product['name']} ({product['image_count']} images)")
        
        report_content = "\n".join(report_lines)
        
        # Save to file
        with open(output_path, 'w') as f:
            f.write(report_content)
        
        logger.info(f"Dataset report saved to: {output_path}")
        return report_content
