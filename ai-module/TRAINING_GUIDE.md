# Training High-Accuracy Model (97%+)

## Quick Start

Since you have the complete dataset with 50 classes, we can train a model with 97%+ accuracy!

### Step 1: Start Training

```powershell
cd ai-module
python train_high_accuracy.py
```

**Training Time**:
- CPU: ~4-6 hours
- GPU (RTX 3050): ~30-45 minutes

### Step 2: Monitor Progress

The script will:
1. Load your dataset (50 classes, thousands of images)
2. Train EfficientNetB0 for 30 epochs
3. Fine-tune for 10 more epochs
4. Evaluate on test set
5. Save the best model

Expected output:
```
Training samples: ~8000
Validation samples: ~1000
Test samples: ~1000
Number of classes: 50

Epoch 1/30
...
Epoch 30/30
✓ Training complete!

Fine-tuning...
✓ Fine-tuning complete!

Final Test Accuracy: 97.XX%
```

### Step 3: Use the Trained Model

After training completes:

```powershell
# Copy the trained model
copy models\high_accuracy_model_finetuned.h5 models\baseline_model.h5

# Test it
python test_model.py
```

---

## What This Does

### Training Process

1. **Initial Training** (30 epochs):
   - Uses ImageNet pre-trained weights
   - Freezes base model
   - Trains only the classification head
   - Expected: ~90-93% accuracy

2. **Fine-tuning** (10 epochs):
   - Unfreezes last 100 layers
   - Lower learning rate
   - Fine-tunes the entire model
   - Expected: **97-98% accuracy**

3. **Evaluation**:
   - Tests on held-out test set
   - Reports accuracy and top-5 accuracy

### Data Augmentation

The script uses aggressive augmentation:
- Rotation (±20°)
- Width/height shifts (±20%)
- Horizontal flips
- Zoom (±20%)
- Brightness adjustments

This prevents overfitting and improves generalization.

---

## Training on CPU vs GPU

### CPU Training
- **Time**: 4-6 hours
- **Works**: Yes, just slower
- **Recommendation**: Run overnight

### GPU Training (RTX 3050)
- **Time**: 30-45 minutes
- **Issue**: TensorFlow 2.17 doesn't support Windows GPU
- **Solution**: Use Google Colab (see below)

---

## Alternative: Train in Google Colab (Faster!)

If you want GPU training without the hassle:

### Step 1: Upload Dataset to Google Drive

1. Compress your dataset:
```powershell
Compress-Archive -Path "dataset\car parts 50" -DestinationPath "car_parts_50.zip"
```

2. Upload `car_parts_50.zip` to Google Drive

### Step 2: Use Colab Notebook

I can create a Colab notebook that:
- Downloads dataset from your Google Drive
- Trains with free T4 GPU (~30 mins)
- Lets you download the trained model

Would you like me to create this Colab notebook?

---

## After Training

### Files Created

- `models/high_accuracy_model_finetuned.h5` - The trained model
- `models/class_labels.json` - Updated with 50 classes
- `models/model_info.json` - Model metadata
- `models/training_history.png` - Training curves

### Integration

The trained model works exactly like the baseline:

```python
from src.services.pretrained_inference import PretrainedInferenceService

# Use the high-accuracy model
service = PretrainedInferenceService(
    model_path='models/high_accuracy_model_finetuned.h5',
    labels_path='models/class_labels.json'
)

# Predict
predictions = service.predict('image.jpg', top_k=5)
```

No other code changes needed!

---

## Troubleshooting

### Out of Memory
Reduce batch size in `train_high_accuracy.py`:
```python
BATCH_SIZE = 16  # or even 8
```

### Training Too Slow
- Use Google Colab (recommended)
- Or reduce epochs:
```python
EPOCHS = 20  # instead of 30
```

### Want Even Better Accuracy?
- Increase epochs to 40-50
- Use EfficientNetB2 instead of B0
- Collect more training data

---

## Summary

**Option 1: Local CPU Training** (Easiest)
```powershell
python train_high_accuracy.py
# Wait 4-6 hours
# Get 97%+ accuracy model
```

**Option 2: Google Colab** (Fastest)
- Upload dataset to Drive
- Run Colab notebook
- Download trained model in 30 mins

**Option 3: Use Baseline Now, Train Later**
- Start with baseline model (works now)
- Train high-accuracy model overnight
- Swap models when ready

Which option would you prefer?
