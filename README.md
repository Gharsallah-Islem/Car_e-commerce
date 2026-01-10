# 🚗 AutoParts Store

> **A production-grade e-commerce platform for automotive spare parts**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-18-red?logo=angular)](https://angular.io/)
[![Python](https://img.shields.io/badge/Python-3.10+-blue?logo=python)](https://python.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-316192?logo=postgresql)](https://www.postgresql.org/)
[![TensorFlow](https://img.shields.io/badge/TensorFlow-2.x-orange?logo=tensorflow)](https://www.tensorflow.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

---

## ✨ Overview

AutoParts Store is a comprehensive e-commerce solution designed for automotive spare parts retailers. It features AI-powered tools for part recognition, real-time delivery tracking, intelligent product recommendations, and a complete inventory management system.

### Key Highlights

- 🔍 **AI Visual Search** - Identify car parts from photos using deep learning
- 🚚 **Real-Time Tracking** - Live delivery monitoring with GPS and maps
- 🤖 **AI Chat Support** - Intelligent assistant for customer queries
- 📊 **Advanced Analytics** - Real-time dashboards and insights
- 📱 **Mobile Ready** - Native Android app for customers

---

## 🎯 Features by Role

### 👤 **Customer**
| Feature | Description |
|---------|-------------|
| Product Catalog | Browse, search, and filter car parts |
| AI Mechanic | Upload photo to identify parts instantly |
| Shopping Cart | Add items, adjust quantities, checkout |
| Payment | Credit card (Stripe) or cash on delivery |
| Order Tracking | Real-time GPS tracking on map |
| AI Chat | Get product recommendations and support |
| Profile | Manage account, view order history |

### 🛡️ **Admin**
| Feature | Description |
|---------|-------------|
| Dashboard | Real-time charts, analytics, alerts |
| Product Management | CRUD operations, image upload |
| Order Management | Process orders, assign deliveries |
| Inventory | Stock levels, movements, auto-reorder |
| Suppliers | Manage vendors and purchase orders |
| Delivery | Assign drivers, monitor routes |
| User Management | View and manage all users |

### 👑 **Super Admin**
All Admin features, plus:
- Role management (assign/revoke admin privileges)
- System configuration

### 🎧 **Support**
| Feature | Description |
|---------|-------------|
| Ticket System | Handle customer reclamations |
| Chat Review | Monitor AI conversations |
| Intervention | Take over from AI when needed |
| Performance | Track response times and metrics |

### 🚗 **Driver**
| Feature | Description |
|---------|-------------|
| Assignments | View assigned deliveries |
| Navigation | Route guidance to destination |
| Status Updates | Mark delivery progress |
| Location Sharing | Real-time GPS broadcast |

---

## 🛠️ Technology Stack

### Backend
| Technology | Purpose |
|------------|---------|
| Java 21 | Runtime |
| Spring Boot 3.5.6 | REST API framework |
| Spring Security + JWT | Authentication |
| Spring Data JPA | ORM |
| PostgreSQL 15 | Database |
| WebSocket (STOMP) | Real-time |
| Stripe API | Payments |
| OAuth2 (Google) | Social login |

### Frontend
| Technology | Purpose |
|------------|---------|
| Angular 18 | SPA framework |
| TypeScript | Language |
| Angular Material | UI components |
| SCSS | Styling |
| ECharts | Data visualization |
| Leaflet | Maps |
| SockJS/STOMP | WebSocket client |

### AI Module
| Technology | Purpose |
|------------|---------|
| Python 3.10+ | Runtime |
| FastAPI | API framework |
| TensorFlow/Keras | ML framework |
| EfficientNetB0 | CNN model (~97% accuracy) |

### Mobile
| Technology | Purpose |
|------------|---------|
| Kotlin | Language |
| Android SDK 34 | Platform |
| Retrofit | HTTP client |
| Room | Local database |

### Infrastructure
| Technology | Purpose |
|------------|---------|
| Docker Compose | Container orchestration |
| Redis | Caching |
| Nginx | Reverse proxy (production) |

---

## 🚀 Quick Start

### Prerequisites
- Java 21 JDK
- Node.js 18+
- Python 3.10+
- PostgreSQL 15+
- Maven 3.9+

### 1. Database
```bash
psql -U postgres
CREATE DATABASE ecommercespareparts;
\q
```

### 2. Backend
```bash
cd Backend
mvn spring-boot:run
```
> Server: http://localhost:8080

### 3. AI Module
```bash
cd ai-module
python -m venv venv
.\venv\Scripts\activate  # Windows
pip install -r requirements.txt
python -m uvicorn src.api.main:app --port 5000 --reload
```
> API: http://localhost:5000

### 4. Frontend
```bash
cd frontend-web
npm install
ng serve
```
> App: http://localhost:4200

### Docker (Alternative)
```bash
docker-compose up -d
```

---

## 📂 Project Structure

```
Car_e-commerce/
├── Backend/                 # Spring Boot REST API
│   ├── src/main/java/      # Java source code
│   └── src/main/resources/ # Configuration
├── frontend-web/           # Angular 18 application
│   ├── src/app/features/   # Feature modules
│   └── src/app/core/       # Shared services
├── ai-module/              # Python AI services
│   ├── src/api/            # FastAPI routes
│   ├── src/services/       # ML services
│   └── models/             # Trained models
├── mobile-app/             # Android Kotlin app
│   └── CarPartsEcom/       # Android project
├── docs/                   # Documentation
└── docker-compose.yml      # Container setup
```

---

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [Architecture](docs/ARCHITECTURE.md) | System design and data flows |
| [Backend](docs/BACKEND.md) | API structure, entities, services |
| [API Reference](docs/API_REFERENCE.md) | Complete endpoint documentation |
| [Database](docs/DATABASE.md) | Schema, relationships, lifecycle |
| [Frontend](docs/FRONTEND.md) | Angular modules and services |
| [AI Module](docs/AI_MODULE.md) | Visual search and recommendations |
| [Mobile](docs/MOBILE.md) | Android app architecture |
| [Deployment](docs/DEPLOYMENT.md) | Setup and production guide |
| [User Guide](docs/USER_GUIDE.md) | Feature documentation by role |

---

## 🧠 AI Capabilities

### Visual Search
Upload a photo of a car part and our AI will:
1. ✅ Identify the part type (50 categories, ~97% accuracy)
2. ✅ Show confidence scores
3. ✅ Match with products in our store
4. ✅ Allow direct add-to-cart

**Supported Parts:** Brake pads, filters, spark plugs, alternators, radiators, and 45+ more categories.

### Recommendation Engine
Hybrid recommendation system combining:
- **Personalized** - Based on browsing/purchase history
- **Similar Products** - Content-based filtering
- **Also Bought** - Collaborative filtering
- **Trending** - Popular products

---

## 🔐 Security

- **JWT Authentication** with configurable expiration
- **OAuth2** Google social login
- **Role-Based Access Control** (CLIENT, ADMIN, SUPER_ADMIN, SUPPORT, DRIVER)
- **BCrypt** password hashing
- **CORS** configuration for cross-origin requests
- **HTTPS** ready for production

---

## 📊 Real-Time Features

- **Delivery Tracking** - Live driver location on Leaflet maps
- **Admin Dashboard** - Real-time order notifications
- **WebSocket** - STOMP over SockJS for bi-directional communication
- **Driver Updates** - GPS location broadcasting

---

## 🤝 Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on:
- Code style and standards
- Branch naming conventions
- Pull request process
- Issue reporting

---

## 👥 Contributors

- **Islem Gharsallah** - *Full Stack Developer* - [@Gharsallah-Islem](https://github.com/Gharsallah-Islem)

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- [EfficientNet](https://github.com/qubvel/efficientnet) - CNN architecture
- [Spring Boot](https://spring.io/projects/spring-boot) - Backend framework
- [Angular](https://angular.io/) - Frontend framework
- [Stripe](https://stripe.com/) - Payment processing
- [Leaflet](https://leafletjs.com/) - Interactive maps
