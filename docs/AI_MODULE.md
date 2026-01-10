# AI Module Documentation

> Python FastAPI-based AI services for visual search and product recommendations

## Table of Contents
- [Overview](#overview)
- [Architecture](#architecture)
- [Visual Search](#visual-search)
- [Recommendation Engine](#recommendation-engine)
- [API Endpoints](#api-endpoints)
- [Integration](#integration)
- [Model Training](#model-training)
- [Limitations & Future](#limitations--future-improvements)

---

## Overview

The AI Module is a standalone Python service that provides intelligent features for the AutoParts Store:

| Feature | Technology | Purpose |
|---------|------------|---------|
| **Visual Search** | EfficientNetB0/B2 + TensorFlow | Identify car parts from images |
| **Recommendations** | Hybrid Algorithm | Personalized product suggestions |
| **Data Collection** | FastAPI | Training data management |

**Service URL:** `http://localhost:5000`

---

## Architecture

```
┌────────────────────────────────────────────────────────────────────────────┐
│                            AI Module                                        │
│                         FastAPI Application                                 │
├────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                           API Layer                                  │   │
│  │  main.py - FastAPI app with CORS and route registration             │   │
│  └───────────────────────────────┬─────────────────────────────────────┘   │
│                                  │                                          │
│     ┌────────────────────────────┼────────────────────────────┐            │
│     ▼                            ▼                            ▼            │
│  ┌────────────────┐   ┌────────────────────┐   ┌────────────────────┐     │
│  │ visual_search_ │   │ recommendation_    │   │ data_collection_   │     │
│  │   routes.py    │   │    routes.py       │   │    routes.py       │     │
│  │                │   │                    │   │                    │     │
│  │ /api/v1/       │   │ /api/v1/           │   │ /api/v1/           │     │
│  │ visual-search  │   │ recommendations    │   │ data-collection    │     │
│  └───────┬────────┘   └─────────┬──────────┘   └────────────────────┘     │
│          │                      │                                          │
│          ▼                      ▼                                          │
│  ┌────────────────┐   ┌────────────────────┐                              │
│  │  Services      │   │  Services          │                              │
│  │                │   │                    │                              │
│  │ pretrained_    │   │ recommendation_    │                              │
│  │ inference.py   │   │ engine.py          │                              │
│  │                │   │                    │                              │
│  │ image_         │   │ • Content-based    │                              │
│  │ processor.py   │   │ • Collaborative    │                              │
│  └───────┬────────┘   │ • Trending         │                              │
│          │            │ • Personalized     │                              │
│          ▼            └────────────────────┘                              │
│  ┌────────────────┐                                                        │
│  │  Models        │                                                        │
│  │                │                                                        │
│  │ baseline_      │                                                        │
│  │  model.h5      │                                                        │
│  │                │                                                        │
│  │ class_labels   │                                                        │
│  │  .json         │                                                        │
│  └────────────────┘                                                        │
│                                                                             │
└────────────────────────────────────────────────────────────────────────────┘
```

---

## Visual Search

### Model: EfficientNetB0/B2

The visual search uses a fine-tuned EfficientNet CNN model for car part classification.

| Attribute | Value |
|-----------|-------|
| **Base Model** | EfficientNetB0 (or B2) |
| **Input Size** | 224 × 224 × 3 |
| **Classes** | 50 car parts |
| **Accuracy** | ~97% on test set |
| **Framework** | TensorFlow/Keras |

### Supported Car Parts (50 Classes)

The model can identify the following car part categories:

| Category | Parts |
|----------|-------|
| **Braking** | Brake Pads, Brake Discs, Brake Calipers, Brake Lines |
| **Engine** | Air Filter, Oil Filter, Spark Plugs, Alternator, Starter Motor |
| **Suspension** | Shock Absorbers, Struts, Control Arms, Ball Joints, Springs |
| **Transmission** | Clutch, Flywheel, Gearbox, CV Joints |
| **Exhaust** | Muffler, Catalytic Converter, Exhaust Pipe, Oxygen Sensor |
| **Cooling** | Radiator, Water Pump, Thermostat, Coolant Hose |
| **Electrical** | Battery, Fuses, Headlights, Tail Lights, Wiring Harness |
| **Steering** | Power Steering Pump, Steering Rack, Tie Rods |
| **Body** | Side Mirrors, Door Handles, Bumpers, Grilles |
| **Interior** | Dashboard Components, Switches, Knobs |

### Inference Pipeline

```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│   Image     │      │  Preprocess │      │   Model     │      │   Results   │
│   Upload    │─────▶│   Pipeline  │─────▶│  Inference  │─────▶│   + Match   │
└─────────────┘      └─────────────┘      └─────────────┘      └─────────────┘
                            │                    │                     │
                            ▼                    ▼                     ▼
                     • Resize 224×224     • EfficientNet       • Top-K predictions
                     • RGB conversion     • Forward pass        • Confidence scores
                     • Normalize [-1,1]   • Softmax output     • Matched products
```

### Preprocessing Steps

```python
def preprocess_image(image):
    # 1. Convert to RGB if needed
    if image.mode != 'RGB':
        image = image.convert('RGB')
    
    # 2. Resize to model input size
    image = image.resize((224, 224), Image.LANCZOS)
    
    # 3. Convert to numpy array
    img_array = np.array(image, dtype=np.float32)
    
    # 4. Add batch dimension
    img_array = np.expand_dims(img_array, axis=0)
    
    # 5. Apply EfficientNet preprocessing (scales to [-1, 1])
    img_array = tf.keras.applications.efficientnet.preprocess_input(img_array)
    
    return img_array
```

### Model Loading

The model uses a compatibility layer for different TensorFlow versions:

```python
class PretrainedInferenceService:
    def __init__(self, model_path='models/baseline_model.h5'):
        # Custom DepthwiseConv2D for version compatibility
        self.model = tf.keras.models.load_model(
            model_path,
            compile=False,
            custom_objects={'DepthwiseConv2D': CompatibleDepthwiseConv2D}
        )
        
        # Load class labels
        with open('models/class_labels.json', 'r') as f:
            self.class_labels = json.load(f)
```

---

## Recommendation Engine

### Strategy Overview

The recommendation engine uses a **hybrid approach** combining four strategies:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    HYBRID RECOMMENDATION ENGINE                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐       │
│  │   PERSONALIZED   │  │     SIMILAR      │  │   ALSO BOUGHT    │       │
│  │                  │  │                  │  │                  │       │
│  │ Based on user's  │  │ Content-based    │  │ Collaborative    │       │
│  │ browsing and     │  │ filtering by     │  │ filtering from   │       │
│  │ purchase history │  │ category, brand, │  │ purchase history │       │
│  │                  │  │ price range      │  │                  │       │
│  └────────┬─────────┘  └────────┬─────────┘  └────────┬─────────┘       │
│           │                     │                     │                  │
│           └─────────────────────┼─────────────────────┘                  │
│                                 ▼                                        │
│                     ┌──────────────────────┐                            │
│                     │   SCORE AGGREGATION  │                            │
│                     │   & DEDUPLICATION    │                            │
│                     └──────────────────────┘                            │
│                                 │                                        │
│                                 ▼                                        │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │                      TRENDING BOOST                               │   │
│  │      Popular products get additional score boost                  │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### 1. Personalized Recommendations

Based on user activity signals:

| Signal | Weight | Description |
|--------|--------|-------------|
| Category views | 0.1 per view (max 0.4) | Boost products in viewed categories |
| Brand affinity | 0.1 per interaction (max 0.3) | Boost preferred brands |
| Discovery bonus | 0.1 | New products user hasn't seen |
| Base score | 0.2 | In-stock products |

```python
def get_personalized_recommendations(user_activities, all_products, limit=10):
    # Track user preferences
    preferred_categories = count_category_interactions(user_activities)
    preferred_brands = count_brand_interactions(user_activities)
    
    recommendations = []
    for product in all_products:
        score = 0.0
        
        # Category preference boost
        if product.category_id in preferred_categories:
            score += min(preferred_categories[product.category_id] * 0.1, 0.4)
        
        # Brand preference boost
        if product.brand_id in preferred_brands:
            score += min(preferred_brands[product.brand_id] * 0.1, 0.3)
        
        # Discovery bonus
        if product.id not in viewed_products:
            score += 0.1
        
        recommendations.append({
            'productId': product.id,
            'score': score,
            'reason': 'Recommended for you'
        })
    
    return sorted(recommendations, key=lambda x: x['score'], reverse=True)[:limit]
```

### 2. Similar Products (Content-Based)

Find products similar to a given product:

| Criterion | Score | Description |
|-----------|-------|-------------|
| Same category | +0.5 | Strong similarity signal |
| Same brand | +0.3 | Moderate similarity |
| Similar price (±30%) | +0.2 | Price range match |

### 3. Also Bought (Collaborative)

Products frequently purchased together:

```python
def get_also_bought_products(product_id, purchase_data, limit=6):
    # purchase_data: [{productId, count}, ...] ordered by co-purchase frequency
    
    for data in purchase_data:
        score = data['count'] / max_count  # Normalized score
        
        recommendations.append({
            'productId': data['productId'],
            'score': score,
            'reason': 'Customers also bought'
        })
    
    return recommendations[:limit]
```

### 4. Trending Products

Based on recent view counts:

```python
def get_trending_products(trending_data, limit=10):
    # trending_data: [{productId, viewCount}, ...] from last 7 days
    
    for data in trending_data:
        score = data['viewCount'] / max_views  # Normalized
        
        recommendations.append({
            'productId': data['productId'],
            'score': score,
            'reason': 'Trending now'
        })
```

---

## API Endpoints

### Health Check

```http
GET /health

Response:
{
  "status": "healthy",
  "service": "ai-module",
  "version": "2.0.0",
  "features": ["visual-search", "recommendations"]
}
```

### Visual Search

**Predict from Image:**
```http
POST /api/v1/visual-search/predict
Content-Type: multipart/form-data

file: <image_file>

Response:
{
  "success": true,
  "predictions": [
    {
      "class": "brake_pad",
      "confidence": 0.9523,
      "confidence_percent": "95.23%"
    },
    {
      "class": "brake_disc",
      "confidence": 0.0312,
      "confidence_percent": "3.12%"
    }
  ],
  "processing_time_ms": 245
}
```

**Model Status:**
```http
GET /api/v1/visual-search/status

Response:
{
  "model_loaded": true,
  "model_path": "models/baseline_model.h5",
  "num_classes": 50,
  "input_size": [224, 224, 3]
}
```

### Recommendations

**Get Personalized Recommendations:**
```http
POST /api/v1/recommendations/personalized
Content-Type: application/json

{
  "userId": "uuid",
  "userActivities": [...],
  "allProducts": [...],
  "limit": 10
}

Response:
{
  "recommendations": [
    {
      "productId": "uuid",
      "score": 0.85,
      "reason": "Based on your category interests",
      "recommendationType": "PERSONALIZED"
    }
  ]
}
```

**Get Similar Products:**
```http
POST /api/v1/recommendations/similar
Content-Type: application/json

{
  "productId": "uuid",
  "product": {...},
  "allProducts": [...],
  "limit": 6
}
```

**Get Trending Products:**
```http
POST /api/v1/recommendations/trending
Content-Type: application/json

{
  "trendingData": [...],
  "allProducts": [...],
  "limit": 10
}
```

### Data Collection

**Upload Training Image:**
```http
POST /api/v1/data-collection/upload
Content-Type: multipart/form-data

file: <image_file>
category: "brake_pad"
```

**Get Dataset Statistics:**
```http
GET /api/v1/data-collection/stats

Response:
{
  "total_images": 5000,
  "categories": {
    "brake_pad": 150,
    "brake_disc": 120,
    ...
  }
}
```

---

## Integration

### Spring Boot Integration

The backend communicates with the AI module via HTTP:

```java
// IAController.java
@RestController
@RequestMapping("/api/ai")
public class IAController {
    
    @Value("${ai.module.url}")
    private String aiModuleUrl;
    
    @PostMapping("/predict")
    public ResponseEntity<?> predict(@RequestParam("image") MultipartFile image) {
        // Forward to AI module
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new InputStreamResource(image.getInputStream()));
        
        ResponseEntity<PredictionResponse> response = restTemplate.exchange(
            aiModuleUrl + "/api/v1/visual-search/predict",
            HttpMethod.POST,
            new HttpEntity<>(body, headers),
            PredictionResponse.class
        );
        
        // Match predictions with products in database
        List<Product> matchedProducts = productService.findByPartName(
            response.getBody().getPredictions().get(0).getClassName()
        );
        
        return ResponseEntity.ok(new AIPredictionResult(response.getBody(), matchedProducts));
    }
}
```

### Angular Integration

```typescript
// ai.service.ts
@Injectable({ providedIn: 'root' })
export class AiService {
  
  predictPart(imageFile: File): Observable<PredictionResult> {
    const formData = new FormData();
    formData.append('image', imageFile);
    
    return this.http.post<PredictionResult>('/api/ai/predict', formData);
  }
}

// ai-mechanic.component.ts
export class AiMechanicComponent {
  
  onImageSelected(event: Event) {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) {
      this.aiService.predictPart(file).subscribe(result => {
        this.predictions = result.predictions;
        this.matchedProducts = result.matchedProducts;
      });
    }
  }
}
```

---

## Model Training

### Training Environment

The model was trained using Google Colab with GPU acceleration:

| Resource | Specification |
|----------|---------------|
| Platform | Google Colab Pro |
| GPU | NVIDIA T4/V100 |
| Framework | TensorFlow 2.x |
| Training Time | ~2-4 hours |

### Dataset

| Attribute | Value |
|-----------|-------|
| Total Images | ~5,000 |
| Classes | 50 car parts |
| Split | 80% train, 20% validation |
| Augmentation | Rotation, flip, zoom, brightness |

### Training Script

See `train_high_accuracy.py` for the full training pipeline:

```python
# Key configuration
BASE_MODEL = 'EfficientNetB0'
IMG_SIZE = 224
BATCH_SIZE = 32
EPOCHS = 50
LEARNING_RATE = 0.001

# Data augmentation
train_datagen = ImageDataGenerator(
    preprocessing_function=preprocess_input,
    rotation_range=20,
    width_shift_range=0.2,
    height_shift_range=0.2,
    horizontal_flip=True,
    zoom_range=0.2
)

# Model architecture
base_model = EfficientNetB0(
    include_top=False,
    weights='imagenet',
    input_shape=(IMG_SIZE, IMG_SIZE, 3)
)

model = Sequential([
    base_model,
    GlobalAveragePooling2D(),
    Dense(256, activation='relu'),
    Dropout(0.5),
    Dense(NUM_CLASSES, activation='softmax')
])

# Training with early stopping
callbacks = [
    EarlyStopping(patience=5, restore_best_weights=True),
    ReduceLROnPlateau(factor=0.2, patience=3)
]

model.fit(train_generator, epochs=EPOCHS, callbacks=callbacks)
```

---

## Limitations & Future Improvements

### Current Limitations

| Limitation | Description |
|------------|-------------|
| **Class Coverage** | Limited to 50 predefined car parts |
| **Image Quality** | Requires clear, well-lit images |
| **Similar Parts** | May confuse visually similar parts |
| **Context** | Doesn't use vehicle make/model for filtering |
| **Cold Start** | Recommendations need user activity history |

### Future Improvements

1. **Expanded Class Coverage**
   - Add more car part categories
   - Support for vehicle-specific parts

2. **Multi-Object Detection**
   - Detect multiple parts in a single image
   - Bounding box localization

3. **Vehicle Context**
   - Filter predictions by vehicle make/model
   - Compatibility matching

4. **Improved Recommendations**
   - Deep learning-based collaborative filtering
   - Real-time learning from user interactions

5. **Edge Deployment**
   - TensorFlow Lite model for mobile inference
   - Reduce latency for mobile app

6. **A/B Testing**
   - Framework for testing recommendation strategies
   - Performance metrics tracking

---

## Running the AI Module

### Local Development

```bash
cd ai-module

# Create virtual environment
python -m venv venv
source venv/bin/activate  # Linux/Mac
# or
.\venv\Scripts\activate   # Windows

# Install dependencies
pip install -r requirements.txt

# Run server
python -m uvicorn src.api.main:app --port 5000 --reload
```

### Docker

```bash
docker build -t car-ecommerce-ai .
docker run -p 5000:5000 -v ./models:/app/models car-ecommerce-ai
```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `API_HOST` | 0.0.0.0 | Server host |
| `API_PORT` | 5000 | Server port |
| `MODEL_PATH` | models/ | Model directory |
| `BACKEND_URL` | http://localhost:8080 | Backend API URL |
| `ALLOWED_ORIGINS` | localhost:4200,localhost:8080 | CORS origins |

---

## Files Reference

```
ai-module/
├── src/
│   ├── api/
│   │   ├── main.py                    # FastAPI application
│   │   ├── visual_search_routes.py    # Visual search endpoints
│   │   ├── recommendation_routes.py   # Recommendation endpoints
│   │   └── data_collection_routes.py  # Data collection endpoints
│   ├── services/
│   │   ├── pretrained_inference.py    # Model inference service
│   │   ├── image_processor.py         # Image preprocessing
│   │   └── recommendation_engine.py   # Recommendation logic
│   └── database/                       # Database utilities
├── models/
│   ├── baseline_model.h5              # Trained Keras model
│   └── class_labels.json              # Class name mappings
├── requirements.txt                    # Python dependencies
├── train_high_accuracy.py             # Training script
└── README.md                          # Module-specific docs
```
