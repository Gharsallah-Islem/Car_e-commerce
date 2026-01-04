# AI Module - Spare Parts Visual Search System

## Overview
Python-based AI/ML module for visual search and spare part recognition using deep learning.

## Features
- 🖼️ **Visual Search** - Upload spare part images and find matches in inventory
- 🧠 **Deep Learning** - EfficientNet-B0 model with transfer learning
- ⚡ **GPU Accelerated** - Optimized for NVIDIA RTX 3050
- 📊 **Product Matching** - Similarity-based product ranking
- 🎯 **High Accuracy** - 80%+ accuracy for trained categories

## Prerequisites
- Python 3.12+
- NVIDIA GPU with CUDA 12.2
- 8GB+ RAM
- 50GB+ disk space (for models and datasets)

## Quick Start

### 1. Setup Environment
```bash
# Navigate to ai-module directory
cd ai-module

# Create virtual environment
python -m venv venv

# Activate virtual environment
# Windows:
venv\Scripts\activate
# Linux/Mac:
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt
```

### 2. Verify GPU Setup
```bash
# Test if GPU is detected
python gpu_test.py
```

**Expected output:**
```
✓ GPU DETECTED! Found 1 GPU(s):
  - GPU 0: /physical_device:GPU:0
✓ GPU is working correctly!
```

### 3. Configure Environment
```bash
# Copy environment template
copy .env.example .env

# Edit .env file with your settings
notepad .env
```

### 4. Collect Training Data
```bash
# Start data collection server
python src/api/data_collection_server.py
```

Access at: `http://localhost:5000/admin/data-collection`

### 5. Train Model (After collecting data)
```bash
# Train visual search model
python scripts/train_model.py --epochs 50 --batch-size 32
```

### 6. Generate Product Embeddings
```bash
# Generate embeddings for all products
python scripts/generate_embeddings.py
```

### 7. Start API Server
```bash
# Start FastAPI server
python src/api/main.py
```

API will be available at: `http://localhost:5000`

## Project Structure

```
ai-module/
├── src/
│   ├── api/                      # API endpoints
│   │   ├── main.py              # FastAPI application
│   │   ├── visual_search_routes.py
│   │   └── data_collection_routes.py
│   ├── models/                   # ML models
│   │   ├── visual_search_model.py
│   │   └── efficientnet_wrapper.py
│   ├── services/                 # Business logic
│   │   ├── image_processor.py
│   │   ├── product_matcher.py
│   │   └── training_service.py
│   ├── database/                 # Data storage
│   │   ├── vector_store.py
│   │   └── dataset_manager.py
│   └── utils/                    # Utilities
│       ├── logger.py
│       └── validators.py
├── models/                       # Trained model files
│   └── visual_search_v1.h5
├── data/                         # Training datasets
│   ├── spare_parts/
│   │   ├── engine_parts/
│   │   ├── brakes/
│   │   └── filters/
│   └── validation/
├── embeddings/                   # Product embeddings
│   └── product_embeddings.pkl
├── logs/                         # Application logs
├── tests/                        # Unit tests
├── scripts/                      # Utility scripts
│   ├── train_model.py
│   └── generate_embeddings.py
├── notebooks/                    # Jupyter notebooks
├── requirements.txt
├── .env.example
├── .env
├── gpu_test.py
└── README.md
```

## API Endpoints

### Visual Search
```http
POST /api/v1/visual-search/analyze
Content-Type: multipart/form-data

Body: { "image": <file> }

Response:
{
  "matches": [
    {
      "product_id": "uuid",
      "name": "Brake Pad Set",
      "similarity": 0.95,
      "confidence": "exact",
      "image_url": "...",
      "price": 49.99
    }
  ],
  "processing_time_ms": 250
}
```

### Data Collection (Admin)
```http
POST /api/v1/data-collection/upload
Content-Type: multipart/form-data

Body: {
  "product_id": "uuid",
  "category": "brakes",
  "images": [<file1>, <file2>, ...]
}
```

### Model Training (Admin)
```http
POST /api/v1/admin/train
Body: {
  "epochs": 50,
  "batch_size": 32,
  "learning_rate": 0.001
}

Response:
{
  "job_id": "uuid",
  "status": "training",
  "estimated_time_minutes": 120
}
```

## GPU Configuration

### Verify GPU is Being Used
The `gpu_test.py` script will:
1. Detect your RTX 3050
2. Run a test computation
3. Compare GPU vs CPU speed
4. Show GPU memory usage

### Common GPU Issues

**Issue: TensorFlow uses CPU instead of GPU**
```bash
# Solution 1: Reinstall TensorFlow
pip uninstall tensorflow
pip install tensorflow==2.15.0

# Solution 2: Check CUDA path
echo %CUDA_PATH%
# Should show: C:\Program Files\NVIDIA GPU Computing Toolkit\CUDA\v12.2
```

**Issue: Out of memory errors**
```python
# In .env file, reduce GPU memory fraction:
GPU_MEMORY_FRACTION=0.6
```

## Training the Model

### Phase 1: Data Collection (Current)
Collect 50-100 images per product category:
- Various angles (front, side, top)
- Different lighting conditions
- Clean backgrounds preferred
- High resolution (>500x500px)

### Phase 2: Model Training
```bash
# Train with default settings
python scripts/train_model.py

# Train with custom parameters
python scripts/train_model.py \
  --epochs 100 \
  --batch-size 16 \
  --learning-rate 0.0001 \
  --augmentation
```

**Training Time Estimates (RTX 3050)**:
- 1000 images: ~30 minutes
- 5000 images: ~2 hours
- 10000 images: ~4 hours

### Phase 3: Evaluation
```bash
# Evaluate model accuracy
python scripts/evaluate_model.py

# Test on sample images
python scripts/test_inference.py --image path/to/test.jpg
```

## Performance Optimization

### GPU Memory Management
```python
# Automatic memory growth (recommended)
tf.config.experimental.set_memory_growth(gpu, True)

# Fixed memory allocation
tf.config.set_logical_device_configuration(
    gpu,
    [tf.config.LogicalDeviceConfiguration(memory_limit=4096)]  # 4GB
)
```

### Batch Processing
```python
# Process multiple images at once
results = model.predict_batch(images, batch_size=32)
```

## Monitoring

### View Logs
```bash
# Real-time logs
tail -f logs/ai_module.log

# Windows PowerShell
Get-Content logs/ai_module.log -Wait
```

### TensorBoard (Training Visualization)
```bash
# Start TensorBoard
tensorboard --logdir=logs/training

# Access at http://localhost:6006
```

## Troubleshooting

### GPU Not Detected
1. Check CUDA installation: `nvcc --version`
2. Check GPU drivers: `nvidia-smi`
3. Reinstall TensorFlow: `pip install tensorflow==2.15.0`
4. Restart terminal/IDE

### Slow Training
1. Verify GPU is being used (check `gpu_test.py`)
2. Increase batch size (if memory allows)
3. Enable mixed precision training
4. Check GPU utilization: `nvidia-smi -l 1`

### Low Accuracy
1. Collect more training data (50+ images per category)
2. Increase training epochs
3. Use data augmentation
4. Fine-tune learning rate

## Development

### Run Tests
```bash
pytest tests/ -v
```

### Code Style
```bash
# Format code
black src/

# Lint code
flake8 src/
```

## Production Deployment

### Docker
```bash
# Build image
docker build -t car-ecommerce-ai .

# Run container
docker run -p 5000:5000 --gpus all car-ecommerce-ai
```

### Performance Targets
- Inference time: <1 second per image
- Accuracy: >80% for trained categories
- Uptime: 99.9%
- Concurrent requests: 100+

## License
MIT

## Support
For issues or questions, check the main project documentation or create an issue on GitHub.
