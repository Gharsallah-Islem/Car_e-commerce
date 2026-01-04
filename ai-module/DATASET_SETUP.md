# AI Module - Dataset Setup Guide

## 🎉 Good News - No Manual Data Collection Needed!

I found excellent pre-existing datasets on Kaggle with thousands of car parts images already labeled and ready to use!

---

## 📦 Available Datasets

### Option 1: 50 Types of Car Parts (Recommended) ⭐
- **Source**: Kaggle
- **Images**: 5000+ images
- **Categories**: 50 different car part types
- **Format**: 224x224 JPG (perfect for our model)
- **Splits**: Train/Validation/Test already organized
- **Link**: https://www.kaggle.com/datasets/tolgadincer/50-types-of-car-parts-image-classification

**Categories include:**
- Brake Pads, Rotors, Calipers
- Air Filters, Oil Filters, Fuel Filters
- Spark Plugs, Ignition Coils
- Alternators, Batteries, Starters
- Suspension components
- And 40+ more!

### Option 2: 14 Automobile Parts
- **Source**: Kaggle
- **Images**: 2000+ images
- **Categories**: 14 spare part types
- **Link**: https://www.kaggle.com/datasets/tolgadincer/image-classification-automobile-parts

---

## 🚀 Quick Setup (3 Steps)

### Step 1: Fix PowerShell Issue

The setup script needs to be run with `.\` prefix in PowerShell:

```powershell
cd ai-module
.\setup_simple.bat
```

**OR** activate virtual environment manually:

```powershell
# Create virtual environment
python -m venv venv

# Activate (PowerShell)
venv\Scripts\Activate.ps1

# If you get execution policy error, run this first:
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

# Then activate again
venv\Scripts\Activate.ps1

# Install dependencies
pip install -r requirements.txt
```

### Step 2: Install Kaggle API

```powershell
# Make sure virtual environment is activated
pip install kaggle
```

### Step 3: Configure Kaggle API Key

1. Go to https://www.kaggle.com/settings
2. Scroll to "API" section
3. Click "Create New API Token"
4. Save the downloaded `kaggle.json` file to:
   ```
   C:\Users\islem\.kaggle\kaggle.json
   ```

### Step 4: Download Dataset

```powershell
# Run the dataset downloader
python scripts\download_dataset.py

# Choose option 1 (50 Types of Car Parts)
```

The script will:
- Download ~500MB of car parts images
- Organize them into the correct folder structure
- Prepare them for training

---

## 📁 What Happens After Download

The dataset will be organized like this:

```
ai-module/data/spare_parts/
├── brake_pads/
│   ├── image_001.jpg
│   ├── image_002.jpg
│   └── ...
├── oil_filter/
│   ├── image_001.jpg
│   └── ...
├── spark_plug/
└── ... (50 categories total)
```

Each category will have 100-200 images - **perfect for training!**

---

## ⚡ Alternative: Manual Setup (If Kaggle API Doesn't Work)

If you have issues with Kaggle API:

1. **Download manually from Kaggle**:
   - Visit: https://www.kaggle.com/datasets/tolgadincer/50-types-of-car-parts-image-classification
   - Click "Download" button
   - Extract ZIP file

2. **Copy to project**:
   ```
   Extract to: ai-module/data/kaggle_50_car_parts/
   ```

3. **Organize dataset**:
   ```powershell
   python scripts\download_dataset.py
   # Choose option 4 to check status
   ```

---

## ✅ Verify Dataset

After downloading, check the dataset:

```powershell
python scripts\download_dataset.py
# Choose option 4 (Check current dataset status)
```

**Expected output:**
```
Status: Dataset found
Categories: 50
Total Images: 5000+
```

---

## 🎯 Next Steps After Dataset Download

1. **Test GPU**:
   ```powershell
   python gpu_test.py
   ```

2. **Validate Dataset**:
   ```powershell
   # Start API server
   python src\api\main.py
   
   # Open browser
   http://localhost:5000/admin/data-collection
   
   # Click "Check Dataset Status"
   ```

3. **Start Training** (Phase 1):
   ```powershell
   python scripts\train_model.py
   ```

---

## 🔧 Troubleshooting

### PowerShell Execution Policy Error

```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Kaggle API Not Found

```powershell
pip install kaggle
```

### Kaggle Authentication Error

Make sure `kaggle.json` is in:
```
C:\Users\islem\.kaggle\kaggle.json
```

### Download is Slow

The 50 car parts dataset is ~500MB. On slow internet:
- Use Option 2 (14 parts, smaller dataset)
- Or download manually from Kaggle website

---

## 📊 Dataset Comparison

| Dataset | Categories | Images | Size | Training Time |
|---------|-----------|--------|------|---------------|
| 50 Car Parts | 50 | 5000+ | ~500MB | 2-3 hours |
| 14 Auto Parts | 14 | 2000+ | ~200MB | 1-2 hours |

**Recommendation**: Use 50 Car Parts for better coverage of spare part types.

---

## 🎓 What's in the Dataset?

The 50 car parts dataset includes:

**Engine Components**:
- Spark Plugs, Ignition Coils
- Air Filters, Oil Filters, Fuel Filters
- Pistons, Valves

**Braking System**:
- Brake Pads, Brake Rotors
- Brake Calipers, Brake Lines

**Electrical**:
- Alternators, Batteries, Starters
- Sensors, Wiring

**Suspension**:
- Shock Absorbers, Struts
- Control Arms, Ball Joints

**And 30+ more categories!**

---

## 💡 Why This is Better Than Manual Collection

✅ **Saves Time**: No need to photograph 5000+ parts  
✅ **Quality**: Professional images, consistent lighting  
✅ **Variety**: Multiple angles, different brands  
✅ **Pre-organized**: Train/Val/Test splits ready  
✅ **Proven**: Used in research papers and competitions  

---

**Ready to download?** Run:
```powershell
.\setup_simple.bat
pip install kaggle
python scripts\download_dataset.py
```

Choose option 1, and you'll have a complete dataset in 10 minutes! 🚀
