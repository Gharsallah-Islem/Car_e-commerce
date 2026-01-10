# System Architecture

> **AutoParts Store** - A production-grade e-commerce platform for automotive spare parts

## Table of Contents
- [Architecture Overview](#architecture-overview)
- [Component Architecture](#component-architecture)
- [Technology Stack](#technology-stack)
- [Data Flow](#data-flow)
- [Real-Time Architecture](#real-time-architecture)
- [Security Architecture](#security-architecture)

---

## Architecture Overview

The AutoParts Store is built on a **microservices-inspired monolithic architecture** with clear separation of concerns across four main components:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CLIENT LAYER                                    │
├─────────────────────────────┬───────────────────────────────────────────────┤
│     Angular 18 (Web)        │           Kotlin (Android Mobile)             │
│   - Customer Storefront     │         - Mirror of Web Features              │
│   - Admin Dashboard         │         - Native Android Experience           │
│   - Support Portal          │         - Local Data Caching                  │
│   - Driver Dashboard        │                                               │
└─────────────┬───────────────┴───────────────────────┬───────────────────────┘
              │                                       │
              │  REST API / WebSocket                 │  REST API
              ▼                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          API GATEWAY LAYER                                   │
│                     Spring Boot 3.5.6 (Java 21)                             │
├─────────────────────────────────────────────────────────────────────────────┤
│  Security │ Controllers │ Services │ WebSocket │ Email │ Stripe │ AI Proxy │
└─────────────────────────────────────────────────────────────────────────────┘
              │                                       │
              │  JDBC / JPA                           │  HTTP REST
              ▼                                       ▼
┌─────────────────────────────┐     ┌─────────────────────────────────────────┐
│      PostgreSQL 15          │     │           AI Module (FastAPI)           │
│   - User & Order Data       │     │   - Visual Search (EfficientNet CNN)    │
│   - Inventory Management    │     │   - Recommendation Engine               │
│   - Delivery Tracking       │     │   - TensorFlow/Keras Inference          │
└─────────────────────────────┘     └─────────────────────────────────────────┘
```

---

## Component Architecture

### 1. Backend (Spring Boot)

The backend follows a **layered architecture** pattern:

```
┌──────────────────────────────────────────────────────────────────────┐
│                        CONTROLLER LAYER                               │
│  26 REST Controllers handling all API endpoints                       │
│  AuthController, ProductController, OrderController, DeliveryController...
├──────────────────────────────────────────────────────────────────────┤
│                         SERVICE LAYER                                 │
│  29 Services containing business logic                                │
│  OrderService, InventoryService, DeliverySimulationService...        │
├──────────────────────────────────────────────────────────────────────┤
│                        REPOSITORY LAYER                               │
│  28 JPA Repositories for data access                                  │
│  Spring Data JPA with PostgreSQL                                      │
├──────────────────────────────────────────────────────────────────────┤
│                         ENTITY LAYER                                  │
│  33 JPA Entities modeling the domain                                  │
│  User, Order, Product, Delivery, Driver, Reclamation...              │
└──────────────────────────────────────────────────────────────────────┘
```

**Key Packages:**
| Package | Purpose |
|---------|---------|
| `controller` | REST API endpoints (26 controllers) |
| `service` | Business logic layer |
| `repository` | Data access layer (JPA) |
| `entity` | Domain models (33 entities) |
| `dto` | Data Transfer Objects (30 DTOs) |
| `config` | Application configuration |
| `security` | JWT & OAuth2 implementation |

### 2. Frontend (Angular 18)

The frontend uses Angular's **standalone components** architecture:

```
┌──────────────────────────────────────────────────────────────────────┐
│                          APP MODULE                                   │
│  Standalone Angular 18 application                                    │
├──────────────────────────────────────────────────────────────────────┤
│                      FEATURE MODULES (14)                             │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐        │
│  │  Home   │ │Products │ │  Cart   │ │Checkout │ │ Profile │        │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘        │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐        │
│  │  Admin  │ │ Support │ │  Chat   │ │ Driver  │ │Tracking │        │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘        │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐                    │
│  │  Auth   │ │ Orders  │ │AI Mech. │ │Reclam.  │                    │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘                    │
├──────────────────────────────────────────────────────────────────────┤
│                        CORE MODULE                                    │
│  Services (25) │ Guards │ Interceptors │ Models                      │
└──────────────────────────────────────────────────────────────────────┘
```

### 3. AI Module (FastAPI/Python)

```
┌──────────────────────────────────────────────────────────────────────┐
│                       FastAPI Application                             │
│                           main.py                                     │
├──────────────────────────────────────────────────────────────────────┤
│  ┌────────────────────┐  ┌────────────────────┐  ┌────────────────┐ │
│  │ Visual Search API  │  │ Recommendation API │  │Data Collection │ │
│  │ /api/v1/visual-    │  │ /api/v1/recommend- │  │     API        │ │
│  │     search         │  │     ations         │  │                │ │
│  └────────┬───────────┘  └────────┬───────────┘  └────────────────┘ │
│           │                       │                                  │
│           ▼                       ▼                                  │
│  ┌────────────────────┐  ┌────────────────────────────────────────┐ │
│  │ PretrainedInferen- │  │      Recommendation Engine             │ │
│  │    ceService       │  │  - Content-based filtering             │ │
│  │ - EfficientNetB0   │  │  - Collaborative filtering             │ │
│  │ - 50 car parts     │  │  - Trending products                   │ │
│  │ - ~97% accuracy    │  │  - Personalized suggestions            │ │
│  └────────────────────┘  └────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────┘
```

### 4. Mobile App (Kotlin/Android)

```
┌──────────────────────────────────────────────────────────────────────┐
│                     Android Application                               │
│                    Clean Architecture                                 │
├──────────────────────────────────────────────────────────────────────┤
│  UI Layer          │  Data Layer           │  Util Layer             │
│  ┌────────────┐    │  ┌─────────────────┐  │  ┌───────────────────┐  │
│  │ Activities │    │  │ Remote (API)    │  │  │ Session Manager   │  │
│  │ Fragments  │    │  │ Local (Room)    │  │  │ Network Utils     │  │
│  │ ViewModels │    │  │ Repositories    │  │  │ Extensions        │  │
│  └────────────┘    │  └─────────────────┘  │  └───────────────────┘  │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Technology Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 (LTS) | Runtime |
| Spring Boot | 3.5.6 | Framework |
| Spring Security | 6.x | Authentication/Authorization |
| Spring Data JPA | 3.x | ORM |
| PostgreSQL | 15 | Database |
| JWT | - | Token Authentication |
| OAuth2 | - | Google SSO |
| Stripe | - | Payment Processing |
| WebSocket | STOMP | Real-time Communication |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| Angular | 18 | SPA Framework |
| TypeScript | 5.x | Language |
| Angular Material | 18 | UI Components |
| SCSS | - | Styling |
| ECharts | - | Data Visualization |
| Leaflet | - | Map Integration |
| SockJS/STOMP | - | WebSocket Client |

### AI Module
| Technology | Version | Purpose |
|------------|---------|---------|
| Python | 3.10+ | Runtime |
| FastAPI | - | API Framework |
| TensorFlow/Keras | 2.x | ML Framework |
| EfficientNetB0/B2 | - | CNN Model |
| NumPy/Pandas | - | Data Processing |
| Uvicorn | - | ASGI Server |

### Mobile
| Technology | Version | Purpose |
|------------|---------|---------|
| Kotlin | 1.9+ | Language |
| Android SDK | 34 | Platform |
| Retrofit | - | HTTP Client |
| Room | - | Local Database |
| Coroutines | - | Async Operations |

### Infrastructure
| Technology | Purpose |
|------------|---------|
| Docker Compose | Container Orchestration |
| Redis | Caching & Sessions |
| pgAdmin | Database Management |
| SMTP (Gmail) | Email Service |

---

## Data Flow

### Order Lifecycle Flow

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   PENDING    │───▶│  CONFIRMED   │───▶│   SHIPPED    │───▶│  DELIVERED   │
│              │    │              │    │              │    │              │
│ Order placed │    │ Payment done │    │ Driver picks │    │ Order done   │
└──────────────┘    └──────────────┘    └──────────────┘    └──────────────┘
       │                   │                   │                   │
       ▼                   ▼                   ▼                   ▼
  ┌─────────┐        ┌─────────┐        ┌─────────┐        ┌─────────┐
  │ Stock   │        │ Payment │        │Delivery │        │ Stock   │
  │ Check   │        │ Process │        │ Assign  │        │ Deduct  │
  └─────────┘        └─────────┘        └─────────┘        └─────────┘
```

### Delivery Tracking Flow

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│ PROCESSING  │────▶│ IN_TRANSIT  │────▶│OUT_FOR_DELV │────▶│  DELIVERED  │
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
                           │
                           ▼
                    ┌─────────────┐
                    │  WebSocket  │
                    │ GPS Updates │
                    │   to UI     │
                    └─────────────┘
```

---

## Real-Time Architecture

### WebSocket Communication

```
┌──────────────────────────────────────────────────────────────────────┐
│                     STOMP over WebSocket                              │
├──────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  Topics:                                                              │
│  ├── /topic/delivery/{trackingNumber}  → Delivery location updates   │
│  ├── /topic/driver/{driverId}/location → Driver GPS broadcasts       │
│  ├── /topic/notifications              → Real-time notifications     │
│  └── /topic/admin/dashboard            → Dashboard metrics           │
│                                                                       │
│  Queues (User-specific):                                              │
│  ├── /user/queue/notifications         → Personal notifications      │
│  └── /user/queue/order-updates         → Order status changes        │
│                                                                       │
└──────────────────────────────────────────────────────────────────────┘
```

### Live Tracking System

```
┌────────────┐      ┌────────────┐      ┌────────────┐      ┌────────────┐
│   Driver   │      │  Backend   │      │ WebSocket  │      │   Client   │
│   Mobile   │      │  Server    │      │  Server    │      │    Web     │
└─────┬──────┘      └─────┬──────┘      └─────┬──────┘      └─────┬──────┘
      │                   │                   │                   │
      │  POST /location   │                   │                   │
      │──────────────────▶│                   │                   │
      │                   │  Broadcast        │                   │
      │                   │──────────────────▶│                   │
      │                   │                   │  Push Update      │
      │                   │                   │──────────────────▶│
      │                   │                   │                   │
      │                   │                   │                   │  Update Map
      │                   │                   │                   │  (Leaflet)
```

---

## Security Architecture

### Authentication Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         AUTHENTICATION                                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Method 1: JWT Authentication                                            │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐          │
│  │  Login   │───▶│ Validate │───▶│ Generate │───▶│  Return  │          │
│  │ Request  │    │  Creds   │    │   JWT    │    │  Token   │          │
│  └──────────┘    └──────────┘    └──────────┘    └──────────┘          │
│                                                                          │
│  Method 2: OAuth2 (Google SSO)                                           │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐          │
│  │  Google  │───▶│ Callback │───▶│ Create/  │───▶│  Return  │          │
│  │  Login   │    │ Handler  │    │  Link    │    │  JWT     │          │
│  └──────────┘    └──────────┘    └──────────┘    └──────────┘          │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### Role-Based Access Control (RBAC)

| Role | Access Level |
|------|-------------|
| **CLIENT** | Browse products, place orders, track deliveries, chat support |
| **ADMIN** | All CLIENT + Manage inventory, orders, deliveries, view analytics |
| **SUPER_ADMIN** | All ADMIN + Manage user roles, system configuration |
| **SUPPORT** | View reclamations, manage tickets, review AI conversations |
| **DRIVER** | View assigned deliveries, update location, mark completion |

### Security Configuration

```
Endpoints Security Matrix:
├── /api/auth/**           → Public (login, register, OAuth)
├── /api/products/**       → Public (GET), Authenticated (POST/PUT/DELETE)
├── /api/orders/**         → Authenticated
├── /api/admin/**          → ADMIN, SUPER_ADMIN
├── /api/support/**        → SUPPORT, ADMIN, SUPER_ADMIN
├── /api/driver/**         → DRIVER
└── /api/super-admin/**    → SUPER_ADMIN only
```

---

## Integration Points

### External Services

| Service | Integration Point | Purpose |
|---------|------------------|---------|
| **Stripe** | Payment Controller | Credit card processing |
| **Google OAuth2** | Auth Controller | Social login |
| **SMTP (Gmail)** | Email Service | Transactional emails |
| **OpenStreetMap** | Frontend (Leaflet) | Map display |
| **OSRM** | Frontend | Route calculation |

### AI Module Integration

```
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│   Angular    │         │ Spring Boot  │         │   FastAPI    │
│   Frontend   │         │   Backend    │         │  AI Module   │
└──────┬───────┘         └──────┬───────┘         └──────┬───────┘
       │                        │                        │
       │ POST /api/ai/predict   │                        │
       │───────────────────────▶│                        │
       │                        │ POST /api/v1/visual-   │
       │                        │      search/predict    │
       │                        │───────────────────────▶│
       │                        │                        │
       │                        │◀───────────────────────│
       │                        │   Predictions          │
       │◀───────────────────────│                        │
       │   Matched Products     │                        │
```

---

## Deployment Architecture

See [DEPLOYMENT.md](./DEPLOYMENT.md) for detailed deployment instructions.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        Docker Compose Stack                              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │
│  │  Frontend   │  │   Backend   │  │  AI Module  │  │  PostgreSQL │    │
│  │   :4200     │  │    :8080    │  │    :5000    │  │    :5432    │    │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘    │
│                                                                          │
│  ┌─────────────┐  ┌─────────────┐                                       │
│  │    Redis    │  │   pgAdmin   │                                       │
│  │    :6379    │  │    :5050    │                                       │
│  └─────────────┘  └─────────────┘                                       │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```
