# AI Visual Search Module - Quick Start Guide

## 🚀 Getting Started (5 Minutes)

### Step 1: Run Setup Script
```bash
cd ai-module
setup.bat
```

This will:
- ✅ Check Python and CUDA installation
- ✅ Create virtual environment
- ✅ Install all dependencies (TensorFlow, FastAPI, etc.)

### Step 2: Verify GPU Setup
```bash
# Make sure virtual environment is activated
venv\Scripts\activate

# Test GPU
python gpu_test.py
```

**Expected output:**
```
✓ GPU DETECTED! Found 1 GPU(s):
  - GPU 0: /physical_device:GPU:0
✓ GPU is working correctly!
Speedup: 15.2x faster on GPU
```

### Step 3: Start Data Collection Server
```bash
python src/api/main.py
```

Server will start at: `http://localhost:5000`

### Step 4: Access Admin Interface
Open in browser: `http://localhost:5000/admin/data-collection`

### Step 5: Upload Product Images
1. Enter Product ID (from your database)
2. Enter Product Name
3. Select Category
4. Drag and drop images (or click to browse)
5. Click "Upload Images"

**Target:** 50-100 images per category for good accuracy

---

## 📊 Monitor Progress

Check dataset statistics in the admin interface:
- Total images collected
- Images per category
- Training readiness status

---

## ⚠️ Troubleshooting

### GPU Not Detected
```bash
# Check CUDA
nvcc --version

# Check GPU drivers
nvidia-smi

# Reinstall TensorFlow
pip uninstall tensorflow
pip install tensorflow==2.15.0
```

### Import Errors
```bash
# Make sure virtual environment is activated
venv\Scripts\activate

# Reinstall dependencies
pip install -r requirements.txt
```

### Port Already in Use
```bash
# Change port in .env file
API_PORT=5001
```

---

## 📁 Project Structure Created

```
ai-module/
├── src/
│   ├── api/
│   │   ├── main.py                    ✅ FastAPI server
│   │   └── data_collection_routes.py  ✅ Upload endpoints
│   ├── services/
│   │   └── image_processor.py         ✅ Image validation
│   └── database/
│       └── dataset_manager.py         ✅ Dataset organization
├── static/
│   └── data_collection.html           ✅ Admin interface
├── data/                              📁 Training images go here
├── models/                            📁 Trained models go here
├── requirements.txt                   ✅ Dependencies
├── gpu_test.py                        ✅ GPU verification
├── .env                               ✅ Configuration
└── setup.bat                          ✅ Setup script
```

---

## 🎯 Next Steps

1. **Collect Data** (Current Phase)
   - Upload 50-100 images per product category
   - Ensure variety (angles, lighting, backgrounds)
   - Use high-quality images (>500x500px)

2. **Train Model** (After data collection)
   - Run: `python scripts/train_model.py`
   - Training time: ~2-4 hours on RTX 3050
   - Model will be saved to `models/visual_search_v1.h5`

3. **Generate Embeddings**
   - Run: `python scripts/generate_embeddings.py`
   - Creates searchable index of all products

4. **Integrate with Backend**
   - Update Spring Boot IAService
   - Connect to AI module API
   - Test visual search from frontend

---

## 📞 Need Help?

Check the full README.md for detailed documentation.
