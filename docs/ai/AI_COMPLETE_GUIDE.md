# 🧠 Complete AI Guide - From Zero to Understanding

> **For your AI tutor interview** - This document explains everything in simple terms with deep technical details.

---

## Table of Contents

1. [What is This Project About?](#1-what-is-this-project-about)
2. [The CNN Model - Visual Search](#2-the-cnn-model---visual-search)
3. [The Recommendation System](#3-the-recommendation-system)
4. [The AI Chatbot (Virtual Mechanic)](#4-the-ai-chatbot-virtual-mechanic)
5. [How Everything Connects](#5-how-everything-connects)
6. [Common Interview Questions & Answers](#6-common-interview-questions--answers)

---

## 1. What is This Project About?

### The Big Picture

This is an **e-commerce platform for car spare parts** (like an Amazon for car parts) built for the Tunisian market. It has 3 AI features:

| Feature | What it does | Real-world example |
|---------|--------------|-------------------|
| **Visual Search (CNN)** | You upload a photo of a car part, AI identifies what it is | Like Google Lens but for car parts |
| **Recommendation System** | Suggests products you might want to buy | Like "Customers also bought" on Amazon |
| **AI Chatbot** | A virtual mechanic that answers questions | Like ChatGPT but specialized in car parts |

### Why These 3 AI Features?

1. **Visual Search** → Customers often don't know the name of a part. They can just take a photo!
2. **Recommendations** → Increases sales by showing relevant products
3. **Chatbot** → 24/7 customer support without human agents

---

## 2. The CNN Model - Visual Search

### 2.1 What is a CNN?

**CNN = Convolutional Neural Network**

Think of it like this:
- Your brain recognizes a cat by looking at its ears, whiskers, fur pattern, etc.
- A CNN does the same thing with images - it learns to recognize patterns

```
Simple Analogy:

Human Brain:                          CNN:
  See image                             Receive pixels
      ↓                                      ↓
  Notice ears, whiskers                 Detect edges, shapes
      ↓                                      ↓
  Combine features                      Combine patterns
      ↓                                      ↓
  "It's a cat!"                         "It's a brake pad!"
```

### 2.2 What is EfficientNet?

**EfficientNet** is a pre-built CNN architecture created by Google in 2019.

**Why use it instead of building our own?**
- Google researchers spent years and millions of dollars developing it
- It's already trained on 14 million images (ImageNet dataset)
- It achieves 97%+ accuracy with fewer parameters (faster, smaller)

**The "B0" in EfficientNetB0:**
- EfficientNet comes in sizes: B0, B1, B2... B7
- B0 = smallest and fastest (5.3 million parameters)
- B7 = largest and most accurate (66 million parameters)
- We chose B0 because it's fast enough for real-time use and still very accurate

### 2.3 What is Transfer Learning?

**Transfer Learning** = Using knowledge from one task to help with another task.

```
Analogy:
If you know how to ride a bicycle, learning to ride a motorcycle is easier.
You "transfer" your balance skills.

In AI:
EfficientNet learned to recognize 1000 objects (cats, dogs, cars, etc.)
We "transfer" this knowledge to recognize 50 car parts.
```

**How it works technically:**

```
Original EfficientNet (trained on ImageNet):
┌─────────────────────────────────────────────────────────────┐
│  Input Image (224x224)                                      │
│         ↓                                                   │
│  [Convolutional Layers] ← These learn general features      │
│  - Edge detection                                           │
│  - Shape recognition                                         │
│  - Texture patterns                                          │
│         ↓                                                    │
│  [Classification Head] ← This is specific to 1000 classes    │
│         ↓                                                    │
│  Output: 1000 probabilities (cat, dog, car, etc.)           │
└─────────────────────────────────────────────────────────────┘

Our Modified Version:
┌─────────────────────────────────────────────────────────────┐
│  Input Image (224x224)                                      │
│         ↓                                                   │
│  [Convolutional Layers] ← KEEP THESE (frozen at first)      │
│  - Same edge detection                                      │
│  - Same shape recognition                                   │
│  - Same texture patterns                                    │
│         ↓                                                   │
│  [NEW Classification Head] ← REPLACE THIS                   │
│  - GlobalAveragePooling2D                                   │
│  - Dense(512) + Dropout                                     │
│  - Dense(50, softmax)                                       │
│         ↓                                                   │
│  Output: 50 probabilities (brake pad, battery, etc.)        │
└─────────────────────────────────────────────────────────────┘
```

### 2.4 The Training Process (Step by Step)

#### Step 1: Prepare the Dataset

We have a dataset with 50 car part categories:
- ~8,000 training images
- ~1,000 validation images
- ~1,000 test images

The dataset is organized in folders:
```
dataset/car parts 50/
├── train/
│   ├── BRAKE PAD/
│   │   ├── image001.jpg
│   │   ├── image002.jpg
│   │   └── ...
│   ├── BATTERY/
│   │   └── ...
│   └── ... (50 folders)
├── valid/
│   └── ... (same structure)
└── test/
    └── ... (same structure)
```

#### Step 2: Data Augmentation

**Problem:** 8,000 images might not be enough. The model might memorize instead of learning.

**Solution:** Create variations of existing images!

```python
# What data augmentation does:
Original Image → Rotate 15° → New Image 1
Original Image → Flip horizontally → New Image 2
Original Image → Zoom 20% → New Image 3
Original Image → Make brighter → New Image 4
Original Image → Shift left → New Image 5
```

This artificially increases our dataset size and teaches the model that a brake pad is still a brake pad even if it's rotated or zoomed.

**Our augmentation settings:**
```python
ImageDataGenerator(
    rotation_range=20,        # Rotate ±20 degrees
    width_shift_range=0.2,    # Shift left/right by 20%
    height_shift_range=0.2,   # Shift up/down by 20%
    horizontal_flip=True,     # Mirror image
    zoom_range=0.2,           # Zoom in/out by 20%
    brightness_range=[0.8, 1.2]  # Darker to brighter
)
```

#### Step 3: Build the Model Architecture

```python
# Our complete model structure:

model = Sequential([
    # 1. Base Model (EfficientNetB0)
    EfficientNetB0(weights='imagenet', include_top=False),
    
    # 2. Global Average Pooling
    GlobalAveragePooling2D(),
    
    # 3. Regularization
    BatchNormalization(),
    Dropout(0.3),  # Randomly turn off 30% of neurons
    
    # 4. Dense Layer
    Dense(512, activation='relu'),
    BatchNormalization(),
    Dropout(0.3),
    
    # 5. Output Layer
    Dense(50, activation='softmax')  # 50 car part classes
])
```

**What each layer does:**

| Layer | Purpose | Simple Explanation |
|-------|---------|-------------------|
| EfficientNetB0 | Feature extraction | "Eyes" that see patterns in images |
| GlobalAveragePooling2D | Dimensionality reduction | Summarizes all features into a smaller vector |
| BatchNormalization | Stabilize training | Keeps numbers in a good range |
| Dropout(0.3) | Prevent overfitting | Forces model to not rely on any single neuron |
| Dense(512, relu) | Learn combinations | Combines features to make decisions |
| Dense(50, softmax) | Final prediction | Outputs probability for each of 50 classes |

#### Step 4: Two-Phase Training

**Phase 1: Feature Extraction (30 epochs)**

```python
# Freeze the base model (don't change its weights)
base_model.trainable = False

# Only train our new layers
model.compile(
    optimizer=Adam(learning_rate=0.001),
    loss='categorical_crossentropy',
    metrics=['accuracy']
)

model.fit(train_data, epochs=30, validation_data=val_data)
```

**Why freeze?**
- EfficientNet already knows how to detect edges, shapes, textures
- We don't want to mess up this knowledge
- We only train the new classification head
- Result: ~90-93% accuracy

**Phase 2: Fine-Tuning (10 epochs)**

```python
# Unfreeze the last 100 layers
base_model.trainable = True
for layer in base_model.layers[:100]:
    layer.trainable = False

# Use smaller learning rate (10x smaller)
model.compile(
    optimizer=Adam(learning_rate=0.0001),  # Notice: 0.0001 not 0.001
    loss='categorical_crossentropy',
    metrics=['accuracy']
)

model.fit(train_data, epochs=10, validation_data=val_data)
```

**Why fine-tune?**
- Now we slightly adjust the pre-trained layers
- They adapt specifically to car parts
- Smaller learning rate = small careful adjustments
- Result: **97-98% accuracy**

#### Step 5: Save the Model

```python
model.save('models/pretrained_model.h5')

# Also save class labels
class_labels = {
    0: "AIR COMPRESSOR",
    1: "ALTERNATOR",
    2: "BATTERY",
    # ... 50 classes total
}
json.dump(class_labels, open('models/pretrained_labels.json', 'w'))
```

### 2.5 How Prediction Works (Inference)

When a user uploads an image, here's exactly what happens:

```
┌─────────────────────────────────────────────────────────────────┐
│                    PREDICTION PIPELINE                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Step 1: RECEIVE IMAGE                                          │
│  ───────────────────                                            │
│  User uploads: brake_photo.jpg (1200x800 pixels, JPEG)          │
│                                                                  │
│  Step 2: VALIDATE                                                │
│  ────────────────                                                │
│  - Check file size (max 10MB)                                   │
│  - Check format (JPEG, PNG, WEBP only)                          │
│  - Check dimensions (min 100x100, max 4096x4096)                │
│  - Check if corrupted                                           │
│                                                                  │
│  Step 3: PREPROCESS                                              │
│  ─────────────────                                               │
│  a) Convert to RGB (remove transparency if PNG)                 │
│  b) Resize to 224x224 pixels                                    │
│  c) Convert to numpy array [0-255]                              │
│  d) Normalize to [0-1]: pixel / 255.0                           │
│  e) Apply ImageNet normalization:                               │
│     - Subtract mean: [0.485, 0.456, 0.406]                      │
│     - Divide by std: [0.229, 0.224, 0.225]                      │
│  f) Add batch dimension: (224,224,3) → (1,224,224,3)            │
│                                                                  │
│  Step 4: MODEL PREDICTION                                        │
│  ────────────────────────                                        │
│  model.predict(preprocessed_image)                              │
│  Returns: [0.02, 0.01, 0.03, 0.89, 0.01, ...]  (50 numbers)    │
│           Each number = probability for that class              │
│                                                                  │
│  Step 5: INTERPRET RESULTS                                       │
│  ─────────────────────────                                       │
│  Highest probability: index 3 = 0.89 (89%)                      │
│  class_labels[3] = "BRAKE CALIPER"                              │
│                                                                  │
│  Step 6: RETURN TOP 5                                            │
│  ────────────────────                                            │
│  [                                                               │
│    {"class": "BRAKE CALIPER", "confidence": 0.89},              │
│    {"class": "BRAKE PAD", "confidence": 0.05},                  │
│    {"class": "BRAKE ROTOR", "confidence": 0.03},                │
│    {"class": "PRESSURE PLATE", "confidence": 0.02},             │
│    {"class": "CLUTCH PLATE", "confidence": 0.01}                │
│  ]                                                               │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 2.6 Why 224x224? Why These Specific Numbers?

| Parameter | Value | Why? |
|-----------|-------|------|
| Image size | 224x224 | EfficientNetB0 was trained on this size. Using different size = bad results |
| Normalization mean | [0.485, 0.456, 0.406] | These are ImageNet dataset averages (R, G, B channels) |
| Normalization std | [0.229, 0.224, 0.225] | These are ImageNet dataset standard deviations |
| Batch size | 32 | Balance between speed and memory usage |
| Learning rate | 0.001 → 0.0001 | Start fast, then slow down for fine adjustments |
| Dropout | 0.3 | 30% is a common value that works well |

### 2.7 The 50 Car Part Classes

Our model can recognize these parts:

| Category | Parts |
|----------|-------|
| **Brakes** | Brake Caliper, Brake Pad, Brake Rotor, Vacuum Brake Booster |
| **Engine** | Engine Block, Cylinder Head, Piston, Camshaft, Crankshaft, Engine Valve |
| **Electrical** | Battery, Alternator, Starter, Ignition Coil, Fuse Box, Distributor |
| **Cooling** | Radiator, Radiator Fan, Radiator Hose, Thermostat, Water Pump, Overflow Tank |
| **Fuel** | Carburetor, Fuel Injector, Gas Cap |
| **Transmission** | Transmission, Torque Converter, Clutch Plate, Pressure Plate, Shift Knob |
| **Suspension** | Coil Spring, Leaf Spring, Lower Control Arm, Idler Arm |
| **Exhaust** | Muffler, Oxygen Sensor |
| **Filters** | Oil Filter, Oil Pan, Oil Pressure Sensor, Air Compressor |
| **Exterior** | Headlights, Taillights, Side Mirror, Spoiler, Rim, Window Regulator |
| **Interior** | Instrument Cluster, Radio |
| **Other** | Valve Lifter |

---

## 3. The Recommendation System

### 3.1 What is a Recommendation System?

A recommendation system suggests items a user might like based on various signals.

**Real-world examples:**
- Netflix: "Because you watched Breaking Bad..."
- Amazon: "Customers who bought this also bought..."
- Spotify: "Made for you" playlists

### 3.2 Types of Recommendation Algorithms

There are 3 main types:

```
┌─────────────────────────────────────────────────────────────────┐
│           RECOMMENDATION ALGORITHM TYPES                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. CONTENT-BASED FILTERING                                     │
│     ─────────────────────────                                   │
│     "If you like Product A, you'll like similar Product B"     │
│                                                                  │
│     How it works:                                                │
│     - Analyze product attributes (category, brand, price)       │
│     - Find products with similar attributes                     │
│     - Recommend those products                                   │
│                                                                  │
│     Example:                                                     │
│     You bought: Bosch Brake Pads for Golf                       │
│     We recommend: Bosch Brake Rotors for Golf (same brand,      │
│                   same category, same car)                       │
│                                                                  │
│  2. COLLABORATIVE FILTERING                                      │
│     ──────────────────────────                                   │
│     "Users similar to you liked these products"                 │
│                                                                  │
│     How it works:                                                │
│     - Find users with similar purchase/view history             │
│     - See what they bought that you haven't                     │
│     - Recommend those products                                   │
│                                                                  │
│     Example:                                                     │
│     Users who bought brake pads also bought:                    │
│     - Brake rotors (80% of users)                               │
│     - Brake fluid (60% of users)                                │
│     - Brake cleaner (40% of users)                              │
│                                                                  │
│  3. HYBRID (What we use!)                                        │
│     ──────────────────────                                       │
│     Combines both approaches for better results                 │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 3.3 Our Implementation (4 Strategies)

We use a **Hybrid Recommendation Engine** with 4 strategies:

#### Strategy 1: Personalized Recommendations

Based on the user's browsing and purchase history.

```python
def get_personalized_recommendations(user_id, user_activities, all_products):
    # Step 1: Analyze user behavior
    viewed_products = set()      # Products they looked at
    purchased_products = set()   # Products they bought
    preferred_categories = {}    # Categories they like
    preferred_brands = {}        # Brands they like
    
    for activity in user_activities:
        if activity.type == 'VIEW':
            viewed_products.add(activity.product_id)
        elif activity.type == 'PURCHASE':
            purchased_products.add(activity.product_id)
        
        # Count category preferences
        preferred_categories[activity.category_id] += 1
        preferred_brands[activity.brand_id] += 1
    
    # Step 2: Score each product
    for product in all_products:
        score = 0.0
        
        # Skip already purchased
        if product.id in purchased_products:
            continue
        
        # Boost products in preferred categories
        if product.category_id in preferred_categories:
            score += min(count * 0.1, 0.4)  # Max +0.4
        
        # Boost products from preferred brands
        if product.brand_id in preferred_brands:
            score += min(count * 0.1, 0.3)  # Max +0.3
        
        # Bonus for new products (discovery)
        if product.id not in viewed_products:
            score += 0.1
        
        recommendations.append({
            'product_id': product.id,
            'score': score,
            'reason': 'Based on your interests'
        })
    
    # Step 3: Sort by score and return top 10
    return sorted(recommendations, key=lambda x: x.score, reverse=True)[:10]
```

**Example:**
```
User Ahmed's history:
- Viewed: 5 brake products, 2 oil filters
- Purchased: Bosch brake pads

Recommendations for Ahmed:
1. Bosch Brake Rotors (score: 0.9) - Same brand, same category
2. Brake Fluid (score: 0.7) - Same category
3. Mann Oil Filter (score: 0.5) - Viewed category
```

#### Strategy 2: Similar Products

When viewing a product, show similar ones.

```python
def get_similar_products(source_product, all_products):
    similar = []
    
    for candidate in all_products:
        if candidate.id == source_product.id:
            continue  # Skip the product itself
        
        score = 0.0
        reason = "Similar product"
        
        # Same category = strong signal
        if candidate.category_id == source_product.category_id:
            score += 0.5
            reason = "Same category"
        
        # Same brand
        if candidate.brand_id == source_product.brand_id:
            score += 0.3
            reason = "Same brand"
        
        # Similar price (within 30%)
        price_diff = abs(candidate.price - source_product.price) / source_product.price
        if price_diff < 0.3:
            score += 0.2
            reason = "Similar price range"
        
        similar.append({'product_id': candidate.id, 'score': score, 'reason': reason})
    
    return sorted(similar, key=lambda x: x.score, reverse=True)[:6]
```

**Example:**
```
Viewing: Bosch Brake Pads (Category: Brakes, Brand: Bosch, Price: 89 TND)

Similar Products:
1. TRW Brake Pads (score: 0.7) - Same category, similar price
2. Bosch Brake Rotors (score: 0.8) - Same brand, same category
3. Bosch Brake Fluid (score: 0.5) - Same brand
```

#### Strategy 3: "Customers Also Bought" (Collaborative)

Based on purchase patterns across all users.

```python
def get_also_bought_products(product_id, purchase_data):
    # purchase_data comes from database:
    # "For product X, what other products were bought in same orders?"
    
    recommendations = []
    max_count = max(d.count for d in purchase_data)
    
    for data in purchase_data:
        # Normalize score (0 to 1)
        score = data.count / max_count
        
        recommendations.append({
            'product_id': data.product_id,
            'score': score,
            'reason': 'Customers also bought'
        })
    
    return recommendations[:6]
```

**Example:**
```
Product: Brake Pads

Purchase analysis (from all orders containing brake pads):
- 150 orders also had Brake Rotors
- 89 orders also had Brake Fluid  
- 45 orders also had Brake Cleaner

Recommendations:
1. Brake Rotors (score: 1.0) - 150 co-purchases
2. Brake Fluid (score: 0.59) - 89 co-purchases
3. Brake Cleaner (score: 0.30) - 45 co-purchases
```

#### Strategy 4: Trending Products

Show what's popular right now.

```python
def get_trending_products(trending_data, all_products):
    # trending_data: products sorted by recent view count
    
    recommendations = []
    max_views = max(d.view_count for d in trending_data)
    
    for data in trending_data:
        score = data.view_count / max_views
        
        recommendations.append({
            'product_id': data.product_id,
            'score': score,
            'reason': 'Trending now'
        })
    
    return recommendations[:10]
```

### 3.4 How the Scoring Works

Each strategy assigns a score from 0.0 to 1.0:

```
SCORING BREAKDOWN:

Personalized Recommendations:
├── Category match: +0.1 per interaction (max +0.4)
├── Brand match: +0.1 per interaction (max +0.3)
├── New product bonus: +0.1
└── Base score: +0.2
    Maximum possible: 1.0

Similar Products:
├── Same category: +0.5
├── Same brand: +0.3
└── Similar price (±30%): +0.2
    Maximum possible: 1.0

Also Bought:
└── Normalized by max co-purchase count: 0.0 to 1.0

Trending:
└── Normalized by max view count: 0.0 to 1.0
```

### 3.5 The API Endpoints

```
POST /api/v1/recommendations/personalized
Body: { userId, userActivities, products, limit }
Returns: Top 10 personalized recommendations

POST /api/v1/recommendations/similar
Body: { productId, product, allProducts, limit }
Returns: 6 similar products

POST /api/v1/recommendations/also-bought
Body: { productId, purchaseData, allProducts, limit }
Returns: 6 frequently bought together products

POST /api/v1/recommendations/trending
Body: { trendingData, allProducts, limit }
Returns: 10 trending products
```

---

## 4. The AI Chatbot (Virtual Mechanic)

### 4.1 What is the Chatbot?

A conversational AI assistant that:
- Answers questions about car parts
- Helps find the right product
- Provides technical advice
- Acts as 24/7 customer support

### 4.2 Technology: Google Gemini

We use **Google Gemini** (Google's latest LLM - Large Language Model).

**What is an LLM?**
- A massive neural network trained on text from the internet
- Can understand and generate human-like text
- Examples: ChatGPT (OpenAI), Gemini (Google), Claude (Anthropic)

**Why Gemini?**
- Free tier available
- Good performance
- Easy API integration
- Supports French (important for Tunisia)

### 4.3 How the Chatbot Works

```
┌─────────────────────────────────────────────────────────────────┐
│                    CHATBOT ARCHITECTURE                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  User: "I need brake pads for my Renault Clio 2019"            │
│                          │                                       │
│                          ↓                                       │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                 SPRING BOOT BACKEND                         │ │
│  │                                                             │ │
│  │  1. ChatServiceImpl.sendMessage()                          │ │
│  │     - Save user message to database                        │ │
│  │     - Get conversation history (last 10 messages)          │ │
│  │                                                             │ │
│  │  2. Search relevant products                                │ │
│  │     SELECT * FROM products                                  │ │
│  │     WHERE name LIKE '%brake%' OR name LIKE '%pad%'         │ │
│  │     → Found: 5 brake pad products                          │ │
│  │                                                             │ │
│  │  3. Call GeminiService.generateResponseWithProducts()      │ │
│  └─────────────────────────┬──────────────────────────────────┘ │
│                            │                                     │
│                            ↓                                     │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                 GEMINI SERVICE                              │ │
│  │                                                             │ │
│  │  Build the prompt:                                          │ │
│  │  ┌────────────────────────────────────────────────────────┐│ │
│  │  │ SYSTEM PROMPT:                                         ││ │
│  │  │ You are an AI assistant for an automotive spare        ││ │
│  │  │ parts e-commerce in Tunisia. You are a virtual        ││ │
│  │  │ mechanic and stock manager...                          ││ │
│  │  │                                                        ││ │
│  │  │ AVAILABLE PRODUCTS:                                    ││ │
│  │  │ - Bosch Brake Pads (ID: abc-123)                      ││ │
│  │  │   Brand: Bosch | Price: 89.99 TND | Stock: 15         ││ │
│  │  │ - TRW Brake Pads (ID: def-456)                        ││ │
│  │  │   Brand: TRW | Price: 75.00 TND | Stock: 8            ││ │
│  │  │                                                        ││ │
│  │  │ CONVERSATION HISTORY:                                  ││ │
│  │  │ User: Hello                                            ││ │
│  │  │ Assistant: Bonjour! How can I help?                   ││ │
│  │  │                                                        ││ │
│  │  │ User: I need brake pads for my Renault Clio 2019      ││ │
│  │  │ Assistant: [GENERATE RESPONSE]                         ││ │
│  │  └────────────────────────────────────────────────────────┘│ │
│  │                                                             │ │
│  │  Call Gemini API:                                          │ │
│  │  POST https://generativelanguage.googleapis.com/v1beta/   │ │
│  │       models/gemini-pro:generateContent                   │ │
│  │                                                             │ │
│  │  API Parameters:                                            │ │
│  │  - temperature: 0.7 (creativity level)                     │ │
│  │  - topK: 40 (consider top 40 tokens)                       │ │
│  │  - topP: 0.95 (nucleus sampling)                           │ │
│  │  - maxOutputTokens: 1024                                   │ │
│  └─────────────────────────┬──────────────────────────────────┘ │
│                            │                                     │
│                            ↓                                     │
│  Gemini Response:                                               │
│  "Bonjour! Pour votre Renault Clio 2019, je vous recommande    │
│   les plaquettes de frein Bosch à 89.99 TND. Nous avons        │
│   15 unités en stock. C'est une excellente qualité qui         │
│   conviendra parfaitement à votre véhicule."                   │
│                            │                                     │
│                            ↓                                     │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  4. Save AI response to database                           │ │
│  │  5. Send to user via WebSocket (real-time)                 │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 4.4 The System Prompt (AI Personality)

This is the "instruction manual" we give to Gemini:

```java
String systemPrompt = """
    You are an intelligent AI assistant for an automotive spare parts 
    e-commerce platform in Tunisia.
    
    Your responsibilities:
    
    1. VIRTUAL MECHANIC:
       - Help customers identify the right spare parts
       - Provide technical advice about compatibility
       - Explain installation procedures
       - Answer questions about specifications
    
    2. STOCK MANAGER:
       - Inform customers about product availability
       - Provide accurate stock information
       - Suggest alternatives when items are out of stock
       - Help find the best deals
    
    3. CUSTOMER SERVICE:
       - Be friendly, professional, and helpful
       - Use simple, clear language (French preferred)
       - Provide accurate information
       - If you don't know something, admit it
    
    Important guidelines:
    - All prices are in TND (Tunisian Dinar)
    - Focus on Tunisian market
    - Keep responses concise (2-4 sentences)
    - Prioritize customer safety
""";
```

### 4.5 Product Context Injection

When a user asks about products, we inject real product data:

```java
private String buildProductContext(List<Product> products) {
    StringBuilder context = new StringBuilder();
    context.append("\n\nAVAILABLE PRODUCTS:\n");
    
    for (Product product : products.subList(0, min(10, products.size()))) {
        context.append(String.format(
            "- %s (ID: %s)\n" +
            "  Brand: %s | Category: %s | Price: %.2f TND | Stock: %d units\n" +
            "  Description: %s\n\n",
            product.getName(),
            product.getId(),
            product.getBrand().getName(),
            product.getCategory().getName(),
            product.getPrice(),
            product.getStock(),
            product.getDescription().substring(0, 100)
        ));
    }
    
    return context.toString();
}
```

### 4.6 The API Call to Gemini

```java
private String callGeminiAPI(String prompt) {
    // Build request body
    Map<String, Object> requestBody = new HashMap<>();
    
    // Content structure
    Map<String, Object> content = Map.of(
        "parts", List.of(Map.of("text", prompt))
    );
    requestBody.put("contents", List.of(content));
    
    // Generation settings
    Map<String, Object> generationConfig = Map.of(
        "temperature", 0.7,      // 0 = deterministic, 1 = creative
        "topK", 40,              // Consider top 40 probable tokens
        "topP", 0.95,            // Nucleus sampling threshold
        "maxOutputTokens", 1024  // Max response length
    );
    requestBody.put("generationConfig", generationConfig);
    
    // Make HTTP POST request
    String url = apiUrl + "?key=" + apiKey;
    ResponseEntity<String> response = restTemplate.postForEntity(url, requestBody, String.class);
    
    // Parse response
    return parseGeminiResponse(response.getBody());
}
```

### 4.7 API Parameters Explained

| Parameter | Value | What it does |
|-----------|-------|--------------|
| **temperature** | 0.7 | Controls randomness. 0 = always same answer, 1 = very creative. 0.7 is balanced. |
| **topK** | 40 | Only consider top 40 most likely next words. Filters unlikely words. |
| **topP** | 0.95 | Nucleus sampling. Consider words until cumulative probability reaches 95%. |
| **maxOutputTokens** | 1024 | Maximum response length. 1024 tokens ≈ 750 words. |

### 4.8 Conversation History

We maintain context by sending recent messages:

```java
private String buildConversationHistory(UUID conversationId) {
    List<Message> recentMessages = messageRepository
        .findByConversationIdOrderByCreatedAtDesc(conversationId)
        .stream()
        .limit(10)  // Last 10 messages
        .toList();
    
    StringBuilder history = new StringBuilder();
    for (Message msg : recentMessages.reversed()) {
        String role = msg.getSenderType().equals("USER") ? "User" : "Assistant";
        history.append(role).append(": ").append(msg.getContent()).append("\n");
    }
    
    return history.toString();
}
```

---

## 5. How Everything Connects

### 5.1 The Complete System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              COMPLETE SYSTEM                                     │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│    ┌─────────────────────────────────────────────────────────────────────────┐  │
│    │                         FRONTEND (Angular)                               │  │
│    │                          Port: 4200                                      │  │
│    │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │  │
│    │  │  Product    │  │   Visual    │  │  Chatbot    │  │   User      │    │  │
│    │  │  Catalog    │  │   Search    │  │   Widget    │  │  Dashboard  │    │  │
│    │  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘    │  │
│    └─────────┼────────────────┼────────────────┼────────────────┼───────────┘  │
│              │                │                │                │              │
│              │      HTTP REST │                │ WebSocket      │              │
│              └────────────────┴────────────────┴────────────────┘              │
│                                      │                                          │
│                                      ↓                                          │
│    ┌─────────────────────────────────────────────────────────────────────────┐  │
│    │                      BACKEND (Spring Boot)                               │  │
│    │                          Port: 8080                                      │  │
│    │                                                                          │  │
│    │  ┌──────────────────────────────────────────────────────────────────┐   │  │
│    │  │                        CONTROLLERS                                │   │  │
│    │  │  ProductController  IAController  ChatController  UserController │   │  │
│    │  └─────────────────────────────┬────────────────────────────────────┘   │  │
│    │                                │                                         │  │
│    │  ┌─────────────────────────────┴────────────────────────────────────┐   │  │
│    │  │                         SERVICES                                  │   │  │
│    │  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐               │   │  │
│    │  │  │ IAService   │  │ ChatService │  │GeminiService│               │   │  │
│    │  │  │             │  │             │  │             │               │   │  │
│    │  │  │ • analyze   │  │ • send msg  │  │ • call API  │               │   │  │
│    │  │  │   image     │  │ • get AI    │  │ • build     │               │   │  │
│    │  │  │ • get recs  │  │   response  │  │   prompt    │               │   │  │
│    │  │  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘               │   │  │
│    │  └─────────┼────────────────┼────────────────┼──────────────────────┘   │  │
│    │            │                │                │                           │  │
│    └────────────┼────────────────┼────────────────┼───────────────────────────┘  │
│                 │                │                │                              │
│        HTTP     │                │                │    HTTP                      │
│        ─────────┘                │                └──────────┐                   │
│        ↓                         │                           ↓                   │
│  ┌─────────────────┐             │                 ┌─────────────────────┐      │
│  │  PYTHON AI      │             │                 │   GOOGLE GEMINI     │      │
│  │  MODULE         │             │                 │   API               │      │
│  │  Port: 5000     │             │                 │   (Cloud)           │      │
│  │                 │             │                 │                     │      │
│  │  • CNN Model    │             │                 │  • LLM              │      │
│  │  • Recommend.   │             │                 │  • Text Generation  │      │
│  │  • Image Proc.  │             │                 │                     │      │
│  └─────────────────┘             │                 └─────────────────────┘      │
│                                  │                                               │
│                                  ↓                                               │
│                     ┌─────────────────────────────┐                             │
│                     │        PostgreSQL           │                             │
│                     │         Database            │                             │
│                     │                             │                             │
│                     │  • Products                 │                             │
│                     │  • Users                    │                             │
│                     │  • Orders                   │                             │
│                     │  • Conversations            │                             │
│                     │  • Recommendations          │                             │
│                     │  • User Activities          │                             │
│                     └─────────────────────────────┘                             │
│                                                                                  │
└──────────────────────────────────────────────────────────────────────────────────┘
```

### 5.2 Data Flow for Visual Search

```
User                    Angular              Spring Boot           Python AI           Database
 │                         │                      │                    │                  │
 │  1. Upload image        │                      │                    │                  │
 │─────────────────────────>                      │                    │                  │
 │                         │                      │                    │                  │
 │                         │ 2. POST /api/ia/     │                    │                  │
 │                         │    analyze-image     │                    │                  │
 │                         │─────────────────────>│                    │                  │
 │                         │                      │                    │                  │
 │                         │                      │ 3. POST /api/v1/   │                  │
 │                         │                      │    visual-search/  │                  │
 │                         │                      │    predict         │                  │
 │                         │                      │───────────────────>│                  │
 │                         │                      │                    │                  │
 │                         │                      │                    │ 4. Load model    │
 │                         │                      │                    │ 5. Preprocess    │
 │                         │                      │                    │ 6. Predict       │
 │                         │                      │                    │                  │
 │                         │                      │ 7. Return:         │                  │
 │                         │                      │    "BRAKE PAD"     │                  │
 │                         │                      │    confidence: 0.89│                  │
 │                         │                      │<───────────────────│                  │
 │                         │                      │                    │                  │
 │                         │                      │ 8. Search products │                  │
 │                         │                      │────────────────────│──────────────────>
 │                         │                      │                    │                  │
 │                         │                      │                    │ 9. Return matches│
 │                         │                      │<───────────────────│──────────────────│
 │                         │                      │                    │                  │
 │                         │                      │ 10. Save recommendation               │
 │                         │                      │────────────────────│──────────────────>
 │                         │                      │                    │                  │
 │                         │ 11. Return results   │                    │                  │
 │                         │<─────────────────────│                    │                  │
 │                         │                      │                    │                  │
 │ 12. Display:            │                      │                    │                  │
 │ "Brake Pad - 89%"       │                      │                    │                  │
 │ + matching products     │                      │                    │                  │
 │<─────────────────────────                      │                    │                  │
```

### 5.3 Why Separate Python and Java?

| Concern | Solution | Why? |
|---------|----------|------|
| ML/AI computations | Python (TensorFlow) | Python has the best ML libraries |
| Business logic | Java (Spring Boot) | Java is great for enterprise apps |
| Database operations | Java (JPA/Hibernate) | Mature ORM ecosystem |
| Real-time chat | Java (WebSocket) | Spring WebSocket is excellent |
| API gateway | Java (Spring Boot) | Single entry point for frontend |

---

## 6. Common Interview Questions & Answers

### About CNN / Visual Search

**Q: Why did you choose EfficientNetB0?**
> A: EfficientNetB0 is the smallest in the EfficientNet family with 5.3M parameters, making it fast for real-time inference. It was designed using Neural Architecture Search (NAS) to find the optimal balance between accuracy and efficiency. It achieves better accuracy than older architectures like ResNet while being smaller.

**Q: What is transfer learning and why use it?**
> A: Transfer learning reuses knowledge from a model trained on a large dataset (ImageNet - 14M images) for a new task. We use it because training from scratch would require millions of images. By reusing the learned features (edges, shapes, textures), we only need to train the final classification layer with our smaller dataset.

**Q: What is the difference between training and inference?**
> A: Training is the learning phase where the model adjusts its weights to minimize errors. It's computationally expensive and done once. Inference is using the trained model to make predictions on new data. It's fast and happens every time a user uploads an image.

**Q: Why do you normalize images before feeding to the model?**
> A: Neural networks work better with small, standardized numbers. We normalize using ImageNet statistics (mean and std) because EfficientNet was trained with these values. Without normalization, the model's predictions would be wrong.

**Q: What is overfitting and how do you prevent it?**
> A: Overfitting is when the model memorizes training data instead of learning patterns. It performs well on training data but poorly on new data. We prevent it with:
> - Dropout (randomly disable neurons)
> - Data augmentation (create image variations)
> - Early stopping (stop training when validation loss stops improving)
> - Regularization (BatchNormalization)

**Q: What is the softmax function?**
> A: Softmax converts the model's raw outputs into probabilities that sum to 1. For 50 classes, it outputs 50 probabilities. The class with highest probability is our prediction.

**Q: Why use categorical_crossentropy loss?**
> A: It's the standard loss function for multi-class classification. It measures how different the predicted probabilities are from the true labels. Lower loss = better predictions.

### About Recommendation System

**Q: What's the difference between content-based and collaborative filtering?**
> A: Content-based uses product attributes (category, brand, price) to find similar products. Collaborative filtering uses user behavior patterns - if users A and B have similar purchase history, recommend to A what B bought. We use both (hybrid approach) for better results.

**Q: How do you handle cold start problem?**
> A: Cold start is when a new user has no history. We handle it by:
> - Showing trending products
> - Using category-based recommendations from products they view
> - Asking users about preferences during signup

**Q: Why limit recommendations to top 10?**
> A: Too many recommendations overwhelm users. Research shows users engage most with the first 5-10 suggestions. More suggestions also slow down the UI.

### About Chatbot

**Q: Why use Google Gemini instead of building your own LLM?**
> A: Training an LLM requires:
> - Millions of dollars in compute resources
> - Billions of training examples
> - Months of training time
> Using an API is practical and cost-effective for a real-world application.

**Q: What is prompt engineering?**
> A: It's the art of writing instructions for the AI model. Our system prompt tells Gemini:
> - Its role (virtual mechanic, stock manager)
> - Guidelines (French language, TND currency)
> - Behavior (professional, helpful)
> Good prompts dramatically improve response quality.

**Q: How do you inject real-time product data into the chatbot?**
> A: Before calling Gemini, we search the database for relevant products based on keywords in the user's message. We then include these products in the prompt context so Gemini can give accurate stock and pricing information.

**Q: What is temperature in LLM parameters?**
> A: Temperature controls randomness:
> - 0 = Deterministic (always same answer)
> - 1 = Maximum creativity (unpredictable)
> - 0.7 = Balanced (our choice)

### System Design Questions

**Q: Why separate Python and Java services?**
> A: Python excels at ML/AI (TensorFlow, NumPy). Java excels at enterprise applications (Spring Boot, database operations). Separating them allows each to use the best tools for its job.

**Q: How do the services communicate?**
> A: Via REST APIs over HTTP. The Spring Boot backend acts as an API gateway, forwarding AI requests to the Python module. This is called a microservices architecture.

**Q: How would you scale this system?**
> A: 
> - Multiple Python instances behind a load balancer for CNN inference
> - Database read replicas for recommendations
> - Redis cache for frequent queries
> - Kubernetes for container orchestration

**Q: What happens if the Python AI service is down?**
> A: We have error handling that catches failures and returns friendly error messages. The backend continues working for non-AI features. We log errors for debugging.

---

## Quick Reference Card

```
┌──────────────────────────────────────────────────────────────────┐
│                    QUICK REFERENCE CARD                           │
├──────────────────────────────────────────────────────────────────┤
│                                                                   │
│  CNN MODEL                                                        │
│  ─────────                                                        │
│  Architecture: EfficientNetB0 + Custom Head                      │
│  Input: 224×224×3 RGB image                                      │
│  Output: 50 class probabilities                                   │
│  Accuracy: 97-98%                                                 │
│  Training: Transfer learning (freeze → fine-tune)                │
│                                                                   │
│  RECOMMENDATION SYSTEM                                            │
│  ─────────────────────                                            │
│  Type: Hybrid (Content + Collaborative)                          │
│  Strategies: Personalized, Similar, Also-Bought, Trending        │
│  Scoring: 0.0 to 1.0 normalized                                  │
│                                                                   │
│  CHATBOT                                                          │
│  ───────                                                          │
│  Model: Google Gemini Pro                                         │
│  Features: Context-aware, Product-aware                          │
│  Language: French (Tunisia market)                               │
│  Parameters: temp=0.7, topK=40, topP=0.95                        │
│                                                                   │
│  KEY TECHNOLOGIES                                                 │
│  ────────────────                                                 │
│  ML Framework: TensorFlow 2.17                                   │
│  AI API: FastAPI (Python)                                        │
│  Backend: Spring Boot 3.x (Java)                                 │
│  Frontend: Angular 18+                                           │
│  Database: PostgreSQL                                            │
│  LLM: Google Gemini API                                          │
│                                                                   │
│  KEY ENDPOINTS                                                    │
│  ─────────────                                                    │
│  POST /api/v1/visual-search/predict → Image classification       │
│  POST /api/v1/recommendations/* → Product recommendations        │
│  POST /api/chat/messages → Chatbot conversation                  │
│                                                                   │
└──────────────────────────────────────────────────────────────────┘
```

---

**Good luck with your AI tutor interview! 🎓**

Remember: It's okay to say "I don't know" if asked something beyond this scope. Being honest about limitations is better than guessing wrong.
