"""
Train High-Accuracy Model with Your Dataset

This script trains an EfficientNetB0 model using your existing
'car parts 50' dataset to achieve 97%+ accuracy.

The trained model will be compatible with TensorFlow 2.17.
"""

import tensorflow as tf
import numpy as np
import json
import os
from pathlib import Path
import matplotlib.pyplot as plt

# Configuration
DATASET_DIR = 'dataset/car parts 50'
TRAIN_DIR = os.path.join(DATASET_DIR, 'train')
VALID_DIR = os.path.join(DATASET_DIR, 'valid')
TEST_DIR = os.path.join(DATASET_DIR, 'test')

IMG_SIZE = 224
BATCH_SIZE = 32
EPOCHS = 30
LEARNING_RATE = 0.001

def create_data_generators():
    """Create data generators for training and validation"""
    
    print("Creating data generators...")
    
    # Training data augmentation
    train_datagen = tf.keras.preprocessing.image.ImageDataGenerator(
        rescale=1./255,
        rotation_range=20,
        width_shift_range=0.2,
        height_shift_range=0.2,
        horizontal_flip=True,
        zoom_range=0.2,
        brightness_range=[0.8, 1.2],
        fill_mode='nearest'
    )
    
    # Validation data (no augmentation)
    val_datagen = tf.keras.preprocessing.image.ImageDataGenerator(rescale=1./255)
    
    # Create generators
    train_generator = train_datagen.flow_from_directory(
        TRAIN_DIR,
        target_size=(IMG_SIZE, IMG_SIZE),
        batch_size=BATCH_SIZE,
        class_mode='categorical',
        shuffle=True
    )
    
    val_generator = val_datagen.flow_from_directory(
        VALID_DIR,
        target_size=(IMG_SIZE, IMG_SIZE),
        batch_size=BATCH_SIZE,
        class_mode='categorical',
        shuffle=False
    )
    
    test_generator = val_datagen.flow_from_directory(
        TEST_DIR,
        target_size=(IMG_SIZE, IMG_SIZE),
        batch_size=BATCH_SIZE,
        class_mode='categorical',
        shuffle=False
    )
    
    num_classes = len(train_generator.class_indices)
    
    print(f"\n✓ Data generators created!")
    print(f"  Training samples: {train_generator.samples}")
    print(f"  Validation samples: {val_generator.samples}")
    print(f"  Test samples: {test_generator.samples}")
    print(f"  Number of classes: {num_classes}")
    
    return train_generator, val_generator, test_generator, num_classes

def create_model(num_classes):
    """Create EfficientNetB0 model"""
    
    print("\nCreating model...")
    
    # Load pre-trained EfficientNetB0
    base_model = tf.keras.applications.EfficientNetB0(
        include_top=False,
        weights='imagenet',
        input_shape=(IMG_SIZE, IMG_SIZE, 3)
    )
    
    # Freeze base model initially
    base_model.trainable = False
    
    # Build model
    model = tf.keras.Sequential([
        base_model,
        tf.keras.layers.GlobalAveragePooling2D(),
        tf.keras.layers.BatchNormalization(),
        tf.keras.layers.Dropout(0.3),
        tf.keras.layers.Dense(512, activation='relu'),
        tf.keras.layers.BatchNormalization(),
        tf.keras.layers.Dropout(0.3),
        tf.keras.layers.Dense(num_classes, activation='softmax')
    ])
    
    # Compile
    model.compile(
        optimizer=tf.keras.optimizers.Adam(LEARNING_RATE),
        loss='categorical_crossentropy',
        metrics=['accuracy', tf.keras.metrics.TopKCategoricalAccuracy(k=5, name='top_5_accuracy')]
    )
    
    print("✓ Model created!")
    model.summary()
    
    return model

def train_model(model, train_gen, val_gen):
    """Train the model"""
    
    print("\n" + "="*60)
    print("Starting Training")
    print("="*60)
    
    # Callbacks
    callbacks = [
        tf.keras.callbacks.ModelCheckpoint(
            'models/high_accuracy_model.h5',
            monitor='val_accuracy',
            save_best_only=True,
            verbose=1
        ),
        tf.keras.callbacks.EarlyStopping(
            monitor='val_loss',
            patience=10,
            restore_best_weights=True,
            verbose=1
        ),
        tf.keras.callbacks.ReduceLROnPlateau(
            monitor='val_loss',
            factor=0.5,
            patience=5,
            min_lr=1e-7,
            verbose=1
        )
    ]
    
    # Train
    history = model.fit(
        train_gen,
        epochs=EPOCHS,
        validation_data=val_gen,
        callbacks=callbacks,
        verbose=1
    )
    
    print("\n✓ Training complete!")
    
    return history

def fine_tune_model(model, train_gen, val_gen):
    """Fine-tune the model by unfreezing some layers"""
    
    print("\n" + "="*60)
    print("Fine-tuning Model")
    print("="*60)
    
    # Unfreeze the base model
    base_model = model.layers[0]
    base_model.trainable = True
    
    # Freeze early layers, unfreeze later layers
    for layer in base_model.layers[:100]:
        layer.trainable = False
    
    # Recompile with lower learning rate
    model.compile(
        optimizer=tf.keras.optimizers.Adam(LEARNING_RATE / 10),
        loss='categorical_crossentropy',
        metrics=['accuracy', tf.keras.metrics.TopKCategoricalAccuracy(k=5, name='top_5_accuracy')]
    )
    
    # Fine-tune
    callbacks = [
        tf.keras.callbacks.ModelCheckpoint(
            'models/high_accuracy_model_finetuned.h5',
            monitor='val_accuracy',
            save_best_only=True,
            verbose=1
        ),
        tf.keras.callbacks.EarlyStopping(
            monitor='val_loss',
            patience=5,
            restore_best_weights=True,
            verbose=1
        )
    ]
    
    history_fine = model.fit(
        train_gen,
        epochs=10,
        validation_data=val_gen,
        callbacks=callbacks,
        verbose=1
    )
    
    print("\n✓ Fine-tuning complete!")
    
    return history_fine

def evaluate_model(model, test_gen):
    """Evaluate the model on test set"""
    
    print("\n" + "="*60)
    print("Evaluating Model")
    print("="*60)
    
    results = model.evaluate(test_gen, verbose=1)
    
    print(f"\n✓ Test Results:")
    print(f"  Loss: {results[0]:.4f}")
    print(f"  Accuracy: {results[1]*100:.2f}%")
    print(f"  Top-5 Accuracy: {results[2]*100:.2f}%")
    
    return results

def save_artifacts(train_gen, history):
    """Save model artifacts"""
    
    print("\nSaving artifacts...")
    
    # Create models directory
    os.makedirs('models', exist_ok=True)
    
    # Save class labels
    class_labels = {v: k for k, v in train_gen.class_indices.items()}
    with open('models/class_labels.json', 'w') as f:
        json.dump(class_labels, f, indent=2)
    print("✓ Saved class_labels.json")
    
    # Save model info
    model_info = {
        'model_name': 'High Accuracy EfficientNetB0',
        'architecture': 'EfficientNetB0 + Custom Head',
        'num_classes': len(class_labels),
        'input_size': IMG_SIZE,
        'trained_on': 'car parts 50 dataset',
        'tensorflow_version': tf.__version__,
        'status': 'Production Ready'
    }
    with open('models/model_info.json', 'w') as f:
        json.dump(model_info, f, indent=2)
    print("✓ Saved model_info.json")
    
    # Plot training history
    plt.figure(figsize=(12, 4))
    
    plt.subplot(1, 2, 1)
    plt.plot(history.history['accuracy'], label='Train Accuracy')
    plt.plot(history.history['val_accuracy'], label='Val Accuracy')
    plt.title('Model Accuracy')
    plt.xlabel('Epoch')
    plt.ylabel('Accuracy')
    plt.legend()
    plt.grid(True)
    
    plt.subplot(1, 2, 2)
    plt.plot(history.history['loss'], label='Train Loss')
    plt.plot(history.history['val_loss'], label='Val Loss')
    plt.title('Model Loss')
    plt.xlabel('Epoch')
    plt.ylabel('Loss')
    plt.legend()
    plt.grid(True)
    
    plt.tight_layout()
    plt.savefig('models/training_history.png', dpi=150)
    print("✓ Saved training_history.png")
    
    print("\n✓ All artifacts saved!")

def main():
    """Main training pipeline"""
    
    print("="*60)
    print("Training High-Accuracy Car Parts Model")
    print("="*60)
    print(f"TensorFlow version: {tf.__version__}")
    print(f"GPU Available: {tf.config.list_physical_devices('GPU')}")
    print("="*60)
    
    # Check if dataset exists
    if not os.path.exists(TRAIN_DIR):
        print(f"\n❌ Error: Dataset not found at {TRAIN_DIR}")
        print("Please ensure the 'car parts 50' dataset is in the dataset folder.")
        return
    
    # Create data generators
    train_gen, val_gen, test_gen, num_classes = create_data_generators()
    
    # Create model
    model = create_model(num_classes)
    
    # Train model
    history = train_model(model, train_gen, val_gen)
    
    # Fine-tune model
    history_fine = fine_tune_model(model, train_gen, val_gen)
    
    # Evaluate model
    results = evaluate_model(model, test_gen)
    
    # Save artifacts
    save_artifacts(train_gen, history)
    
    print("\n" + "="*60)
    print("✓ TRAINING COMPLETE!")
    print("="*60)
    print(f"\nFinal Test Accuracy: {results[1]*100:.2f}%")
    print(f"\nModel saved to: models/high_accuracy_model_finetuned.h5")
    print("\nNext steps:")
    print("1. Copy model to models/baseline_model.h5 (or update inference service)")
    print("2. Test with: python test_model.py")
    print("3. Start FastAPI server")
    print("4. Integrate with Spring Boot")
    print("="*60)

if __name__ == "__main__":
    main()
