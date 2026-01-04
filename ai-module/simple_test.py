"""
Simple Model Test - Direct Loading

Test if we can load the model at all, and check its structure.
"""

import tensorflow as tf
import warnings
warnings.filterwarnings('ignore')

print("Testing model loading...")
print(f"TensorFlow version: {tf.__version__}")

try:
    # Try loading without compile
    print("\nAttempt 1: Loading with compile=False...")
    model = tf.keras.models.load_model(
        'models/pretrained_model.h5',
        compile=False
    )
    print("✓ Model loaded!")
    print(f"Input shape: {model.input_shape}")
    print(f"Output shape: {model.output_shape}")
    print(f"Number of classes: {model.output_shape[-1]}")
    
    # Try a dummy prediction
    import numpy as np
    dummy_input = np.random.rand(1, 224, 224, 3).astype(np.float32)
    print("\nTesting prediction...")
    output = model.predict(dummy_input, verbose=0)
    print(f"✓ Prediction works! Output shape: {output.shape}")
    print(f"✓ Model is functional!")
    
except Exception as e:
    print(f"❌ Error: {e}")
    print("\nTrying alternative: Use the 40-class model instead...")
    
    try:
        model = tf.keras.models.load_model(
            'dataset/car parts/EfficientNetB2-40-(224 X 224)- 96.90.h5',
            compile=False
        )
        print("✓ Alternative model loaded!")
        print(f"Classes: {model.output_shape[-1]}")
    except Exception as e2:
        print(f"❌ Also failed: {e2}")
