"""
Debug script to test model predictions directly
"""
import tensorflow as tf
import numpy as np
from PIL import Image
from pathlib import Path
import json

print("=" * 60)
print("AI Model Debug Test")
print("=" * 60)

# Check TensorFlow version
print(f"\n1. TensorFlow version: {tf.__version__}")

# Load pretrained model with compatibility fix
print("\n2. Loading pretrained model...")
MODELS_DIR = Path("models")
model_path = MODELS_DIR / "pretrained_model.h5"
labels_path = MODELS_DIR / "pretrained_labels.json"

# Custom layer for compatibility
class CompatibleDepthwiseConv2D(tf.keras.layers.DepthwiseConv2D):
    def __init__(self, *args, **kwargs):
        kwargs.pop('groups', None)
        super().__init__(*args, **kwargs)

try:
    model = tf.keras.models.load_model(
        str(model_path),
        compile=False,
        custom_objects={'DepthwiseConv2D': CompatibleDepthwiseConv2D}
    )
    print("✓ Model loaded successfully")
    print(f"  Input shape: {model.input_shape}")
    print(f"  Output shape: {model.output_shape}")
except Exception as e:
    print(f"❌ Failed to load pretrained model: {e}")
    print("\nTrying baseline model...")
    model_path = MODELS_DIR / "baseline_model.h5"
    labels_path = MODELS_DIR / "class_labels.json"
    model = tf.keras.models.load_model(str(model_path), compile=False)
    print("✓ Baseline model loaded")

# Load labels
with open(labels_path, 'r') as f:
    labels = json.load(f)
class_labels = {int(k): v for k, v in labels.items()}
print(f"✓ Loaded {len(class_labels)} classes")

# Create test images
print("\n3. Creating distinct test images...")

# Pure red image
red_img = np.ones((224, 224, 3), dtype=np.float32) * [1.0, 0.0, 0.0]
# Pure green image  
green_img = np.ones((224, 224, 3), dtype=np.float32) * [0.0, 1.0, 0.0]
# Pure blue image
blue_img = np.ones((224, 224, 3), dtype=np.float32) * [0.0, 0.0, 1.0]
# Random noise
random_img = np.random.rand(224, 224, 3).astype(np.float32)
# Gradient
gradient_img = np.zeros((224, 224, 3), dtype=np.float32)
for i in range(224):
    gradient_img[i, :, :] = i / 224.0

test_images = {
    "Pure Red": red_img,
    "Pure Green": green_img,
    "Pure Blue": blue_img,
    "Random Noise": random_img,
    "Gradient": gradient_img
}

print("\n4. Testing predictions with simple /255 normalization (already 0-1)...")
for name, img in test_images.items():
    img_batch = np.expand_dims(img, axis=0)
    preds = model.predict(img_batch, verbose=0)[0]
    top_idx = np.argmax(preds)
    top_conf = preds[top_idx]
    print(f"  {name:15} -> {class_labels[top_idx]:20} ({top_conf*100:.2f}%)")

# Check if all predictions are the same
print("\n5. Checking prediction variance...")
all_preds = []
for img in test_images.values():
    img_batch = np.expand_dims(img, axis=0)
    preds = model.predict(img_batch, verbose=0)[0]
    all_preds.append(preds)

pred_std = np.std(all_preds, axis=0)
if np.max(pred_std) < 0.01:
    print("❌ WARNING: All predictions are nearly identical!")
    print("   This indicates the model may not be responding to input.")
    print("   Possible causes:")
    print("   - Model weights are corrupted or not loading correctly")
    print("   - Preprocessing mismatch with training")
else:
    print("✓ Predictions vary between different inputs")

# Try with EfficientNet preprocessing
print("\n6. Testing with EfficientNet preprocessing...")
from tensorflow.keras.applications.efficientnet import preprocess_input

for name, img in list(test_images.items())[:3]:
    # EfficientNet expects 0-255 input
    img_255 = (img * 255.0).astype(np.float32)
    img_batch = np.expand_dims(img_255, axis=0)
    img_preprocessed = preprocess_input(img_batch)
    preds = model.predict(img_preprocessed, verbose=0)[0]
    top_idx = np.argmax(preds)
    top_conf = preds[top_idx]
    print(f"  {name:15} -> {class_labels[top_idx]:20} ({top_conf*100:.2f}%)")

print("\n" + "=" * 60)
print("Debug complete")
print("=" * 60)
