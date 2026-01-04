# 🚗 AutoParts Store - Advanced Car E-Commerce Platform

A production-grade e-commerce platform for auto parts featuring AI-powered tools, comprehensive delivery management, and advanced inventory control.

## 🌟 Key Features

### 🛒 E-Commerce Core
- **Product Catalog**: Advanced search and filtering for car parts.
- **Shopping Cart & Checkout**: Secure tracking with Stripe payment integration.
- **User Accounts**: OAuth2 (Google) authentication and profile management.

### 🧠 AI Module (Python/FastAPI)
- **Image Recognition**: Instantly identify car parts using a trained **EfficientNet** model. Upload a photo to find the exact part.
- **Recommendation System**: AI-driven product suggestions based on user behavior and product similarity.
- **Chatbot Assistant**: Intelligent assistant for customer queries.

### 🚚 Delivery Management System
- **Driver Management**: Track and manage delivery drivers.
- **Delivery Tracking**: Real-time status updates for orders (Pending, In Transit, Delivered).
- **Route Optimization**: Efficient delivery planning.

### 📦 Advanced Inventory Management
- **Dashboard**: Real-time stock alerts (Low Stock, Out of Stock).
- **Supplier Management**: Manage supplier relationships and contacts.
- **Purchase Orders**: automated reordering and PDF order generation.
- **Stock Movements**: Track every item entering or leaving the warehouse.

## 🛠️ Tech Stack

### Backend
- **Framework**: Spring Boot 3.5.6 (Java 21)
- **Database**: PostgreSQL
- **Security**: Spring Security, JWT, OAuth2
- **Build Tool**: Maven

### Frontend
- **Framework**: Angular 18 (TypeScript)
- **Styling**: SCSS, Angular Material
- **State Management**: Angular Signals
- **Charts**: ECharts

### AI & Data Science
- **Framework**: FastAPI (Python 3.10+)
- **ML Libraries**: TensorFlow/Keras, Scikit-learn, NumPy, Pandas
- **Models**: EfficientNetB0/B2 (Car Part Classification)
- **Server**: Uvicorn

### Mobile
- **Platform**: Android (Native/Java)

## 🚀 Getting Started

Follow these steps to run the complete system locally.

### Prerequisites
- Java 21 JDK
- Node.js 18+ & npm
- Python 3.10+
- PostgreSQL 15+
- Maven

### 1. Database Setup
Ensure PostgreSQL is running and create a database (e.g., `autoparts_db`). Update `application.properties` in the Backend if needed.

### 2. Backend (Spring Boot)
```bash
cd Backend
mvn clean install
mvn spring-boot:run
```
*Server runs on: `http://localhost:8080`*

### 3. AI Module (Python)
```bash
cd ai-module
# Create virtual environment (optional but recommended)
python -m venv venv
# Windows:
.\venv\Scripts\activate
# Linux/Mac:
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Run the API server
python -m uvicorn src.api.main:app --port 5000 --reload
```
*API runs on: `http://localhost:5000`*

### 4. Frontend (Angular)
```bash
cd frontend-web
npm install
ng serve
```
*Application runs on: `http://localhost:4200`*

## 📂 Project Structure

```
Car_e-commerce/
├── Backend/                # Spring Boot REST API
├── frontend-web/           # Angular Admin & Storefront
├── ai-module/              # Python AI Services
│   ├── src/                # FastAPI Source Code
│   ├── models/             # Trained .h5 Models
│   └── dataset/            # Training Data & Notebooks
├── mobile-app/             # Android Application
└── docs/                   # Documentation
```

## 🧠 AI Models

The project includes pre-trained models for car part recognition.
- **Location**: `ai-module/models/` and `ai-module/dataset/car parts/`
- **Architecture**: EfficientNetB0 / EfficientNetB2
- **Performance**: ~97% Accuracy on test set

## 👥 Contributors
- **Islem Gharsallah** - *Full Stack Developer*

## 📄 License
MIT License
