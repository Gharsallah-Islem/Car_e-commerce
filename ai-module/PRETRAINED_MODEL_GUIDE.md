# Using Pre-trained Car Parts Model (96.9% Accuracy)

## Quick Start

### Step 1: Download Pre-trained Model

**Option A: Automatic (if Kaggle API works)**
```powershell
cd ai-module
python scripts\download_pretrained_model.py
```

**Option B: Manual Download**
1. Go to: https://www.kaggle.com/datasets/ahmedabdelmadgid/50-types-of-car-parts-image-classification
2. Click "Download" button
3. Extract the ZIP file
4. Copy `EfficientNetB2-40-(224 X 224)- 96.90.h5` to `ai-module/models/`

### Step 2: Test the Model

```powershell
cd ai-module
python
```

```python
from src.services.pretrained_inference import PretrainedInferenceService

# Initialize
service = PretrainedInferenceService()

# Predict
predictions = service.predict('path/to/car_part_image.jpg', top_k=5)

# Results
for pred in predictions:
    print(f"{pred['class']}: {pred['confidence_percent']}")
```

---

## Model Details

- **Architecture**: EfficientNetB2
- **Accuracy**: 96.9%
- **Input Size**: 224x224 pixels
- **Classes**: 50 car parts (internal & external)

### Supported Car Parts

**Internal Parts**:
- Battery
- Brake Pads/Caliper/Disc
- Engine Block
- Alternator
- Radiator
- Spark Plug
- Oil Filter
- Air Filter
- Fuel Injector/Pump
- Piston
- Crankshaft
- Camshaft
- Turbocharger
- And more...

**External Parts**:
- Headlight/Taillight
- Bumper
- Hood
- Door
- Fender
- Tire/Wheel
- And more...

---

## Integration with FastAPI

Update `src/api/visual_search_routes.py`:

```python
from fastapi import APIRouter, UploadFile, File
from ..services.pretrained_inference import PretrainedInferenceService

router = APIRouter(prefix="/visual-search", tags=["visual-search"])

# Initialize service once
inference_service = PretrainedInferenceService()

@router.post("/analyze")
async def analyze_part(image: UploadFile = File(...)):
    """Analyze car part image and return predictions"""
    
    # Read image
    contents = await image.read()
    
    # Convert to PIL Image
    from PIL import Image
    import io
    img = Image.open(io.BytesIO(contents))
    
    # Get predictions
    predictions = inference_service.predict(img, top_k=5)
    
    return {
        "success": True,
        "predictions": predictions
    }
```

---

## Fine-tuning (Optional)

To fine-tune this model with your own data later:

```python
import tensorflow as tf

# Load pre-trained model
model = tf.keras.models.load_model('models/EfficientNetB2-40-(224 X 224)- 96.90.h5')

# Freeze early layers
for layer in model.layers[:-10]:
    layer.trainable = False

# Compile with lower learning rate
model.compile(
    optimizer=tf.keras.optimizers.Adam(1e-5),
    loss='categorical_crossentropy',
    metrics=['accuracy']
)

# Fine-tune on your data
model.fit(your_train_data, epochs=10, validation_data=your_val_data)
```

---

## Next Steps

1. ✅ Download the pre-trained model
2. ✅ Test it with sample images
3. ✅ Integrate with your FastAPI backend
4. ✅ Connect to Spring Boot
5. ⏳ (Optional) Fine-tune with your own inventory photos later

This model is **production-ready** with 96.9% accuracy!
