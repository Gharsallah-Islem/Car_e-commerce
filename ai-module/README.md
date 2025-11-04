# AI Module (Python)

## Overview
Python-based AI/ML services for the Car E-Commerce platform.

## Features
- 🖼️ **Image Recognition** - Identify car parts from photos
- 🤖 **Chatbot** - AI-powered customer support
- 📊 **Recommendation System** - Personalized product suggestions
- 🔍 **Part Matching** - Find compatible parts based on vehicle info

## Prerequisites
- Python 3.10+
- pip
- Virtual environment (recommended)

## Setup

```bash
# Create virtual environment
python -m venv venv

# Activate virtual environment
# Windows
venv\Scripts\activate
# Linux/Mac
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Run the application
python app.py
```

## Project Structure

```
ai-module/
├── src/
│   ├── api/              # API endpoints
│   ├── models/           # ML models
│   ├── services/         # Business logic
│   └── utils/            # Utilities
├── models/               # Trained models
├── data/                 # Training data
├── tests/                # Tests
├── notebooks/            # Jupyter notebooks
├── requirements.txt
└── app.py
```

## API Endpoints

### Image Recognition
```
POST /api/recognize
Content-Type: multipart/form-data
Body: { "image": <file> }
```

### Recommendations
```
GET /api/recommendations/{user_id}
```

### Chatbot
```
POST /api/chat
Body: { "message": "string", "conversation_id": "uuid" }
```

## Development

### Running Tests
```bash
pytest
```

### Training Models
```bash
python src/models/train_image_model.py
python src/models/train_recommendation_model.py
```

## Technologies
- **Flask/FastAPI** - Web framework
- **TensorFlow/PyTorch** - ML framework
- **OpenCV** - Image processing
- **NumPy/Pandas** - Data manipulation
- **scikit-learn** - ML utilities

## Environment Variables
Create `.env` file:
```
FLASK_ENV=development
MODEL_PATH=./models
API_KEY=your_api_key
```

## Docker

```bash
# Build
docker build -t car-ecommerce-ai .

# Run
docker run -p 5000:5000 car-ecommerce-ai
```

## Documentation
See [docs/ai](../docs/ai) for detailed documentation.

## License
MIT
