"""
Create a Working Baseline Model

Since all external datasets are failing, this script creates a simple
but functional baseline model using ImageNet pre-trained EfficientNetB0.

This model can:
1. Work immediately for testing your pipeline
2. Be fine-tuned later with your own data
3. Serve as a placeholder until you collect real images
"""

import tensorflow as tf
import numpy as np
import json
from pathlib import Path

def create_baseline_model(num_classes=15, save_path='models/baseline_model.h5'):
    """
    Create a baseline car parts classification model
    
    Args:
        num_classes: Number of car part categories
        save_path: Where to save the model
    """
    
    print("="*60)
    print("Creating Baseline Car Parts Model")
    print("="*60)
    print(f"Classes: {num_classes}")
    print("Architecture: EfficientNetB0 (ImageNet weights)")
    print("="*60)
    
    # Create model with ImageNet pre-trained weights
    base_model = tf.keras.applications.EfficientNetB0(
        include_top=False,
        weights='imagenet',  # This downloads automatically from TensorFlow
        input_shape=(224, 224, 3)
    )
    
    # Freeze base model
    base_model.trainable = False
    
    # Add classification head
    model = tf.keras.Sequential([
        base_model,
        tf.keras.layers.GlobalAveragePooling2D(),
        tf.keras.layers.Dropout(0.3),
        tf.keras.layers.Dense(512, activation='relu'),
        tf.keras.layers.Dropout(0.3),
        tf.keras.layers.Dense(num_classes, activation='softmax')
    ])
    
    # Compile
    model.compile(
        optimizer=tf.keras.optimizers.Adam(0.001),
        loss='categorical_crossentropy',
        metrics=['accuracy']
    )
    
    # Create models directory
    Path(save_path).parent.mkdir(parents=True, exist_ok=True)
    
    # Save model
    model.save(save_path)
    print(f"\n✓ Model saved to: {save_path}")
    
    # Create class labels (common car parts)
    class_labels = {
        0: "battery",
        1: "brake_pads",
        2: "engine",
        3: "alternator",
        4: "radiator",
        5: "spark_plug",
        6: "oil_filter",
        7: "air_filter",
        8: "headlight",
        9: "tire",
        10: "steering_wheel",
        11: "turbocharger",
        12: "shock_absorber",
        13: "exhaust_pipe",
        14: "transmission"
    }
    
    # Save labels
    labels_path = Path(save_path).parent / 'class_labels.json'
    with open(labels_path, 'w') as f:
        json.dump(class_labels, f, indent=2)
    print(f"✓ Labels saved to: {labels_path}")
    
    # Save model info
    model_info = {
        'model_name': 'Baseline EfficientNetB0',
        'architecture': 'EfficientNetB0 + Custom Head',
        'num_classes': num_classes,
        'input_size': 224,
        'pretrained_weights': 'ImageNet',
        'status': 'Ready for fine-tuning',
        'note': 'This model uses ImageNet weights. Fine-tune with your car parts data for best results.'
    }
    
    info_path = Path(save_path).parent / 'model_info.json'
    with open(info_path, 'w') as f:
        json.dump(model_info, f, indent=2)
    print(f"✓ Model info saved to: {info_path}")
    
    print("\n" + "="*60)
    print("✓ Baseline Model Created Successfully!")
    print("="*60)
    print("\nThis model is ready to use for:")
    print("1. Testing your integration pipeline")
    print("2. Demonstrating the visual search feature")
    print("3. Fine-tuning with your own car parts images")
    print("\nNOTE: For production use, fine-tune with real car parts data.")
    print("="*60)
    
    return model

if __name__ == "__main__":
    model = create_baseline_model()
    print("\n✓ Done! Model ready at: models/baseline_model.h5")
