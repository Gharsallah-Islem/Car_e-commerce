# IntegMobile - Car Parts E-Commerce Platform

A complete mobile e-commerce solution for car parts, featuring a Spring Boot backend with PostgreSQL and a native Android application built with Kotlin.

## 📋 Project Overview

This project consists of two main components:

1. **Backend** - RESTful API built with Spring Boot 3.2, PostgreSQL, JWT authentication, and Stripe payment integration
2. **Android App** - Native Kotlin application with MVVM architecture, Room database, Retrofit, Google Maps, and modern Android components

## 🏗️ Project Structure

```
IntegMobile/
├── backend/                    # Spring Boot Backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/com/integmobile/backend/
│   │   │   │   ├── config/           # Security, CORS, Data Seeding
│   │   │   │   ├── controller/       # REST Controllers
│   │   │   │   ├── model/            # Entities, DTOs
│   │   │   │   ├── repository/       # JPA Repositories
│   │   │   │   ├── security/         # JWT Authentication
│   │   │   │   ├── service/          # Business Logic
│   │   │   │   └── util/             # Utilities
│   │   │   └── resources/
│   │   │       └── application.yml   # Configuration
│   │   └── build.gradle.kts
│   └── README.md
│
├── app/                        # Android Application
│   ├── src/main/
│   │   ├── java/com/integmobile/
│   │   │   ├── data/                 # Data Layer
│   │   │   │   ├── api/              # Retrofit API Services
│   │   │   │   ├── db/               # Room Database
│   │   │   │   ├── model/            # Data Models
│   │   │   │   └── repository/       # Repository Pattern
│   │   │   ├── ui/                   # UI Layer
│   │   │   │   ├── auth/             # Authentication Screens
│   │   │   │   ├── cart/             # Shopping Cart
│   │   │   │   ├── checkout/         # Checkout Flow
│   │   │   │   ├── orders/           # Order Management
│   │   │   │   ├── products/         # Product Catalog
│   │   │   │   └── profile/          # User Profile
│   │   │   └── utils/                # Utilities
│   │   ├── res/                      # Resources
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── README.md
│
├── build.gradle.kts            # Root Gradle Config
├── settings.gradle.kts
└── README.md                   # This file
```

## 🚀 Quick Start

### Prerequisites

#### Backend
- Java 17 or higher
- PostgreSQL 15+
- Gradle 8.0+

#### Android App
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 24+ (minimum)
- Android SDK 34 (target)
- Google Maps API Key
- Stripe Publishable Key

### Setup Instructions

#### 1. Backend Setup

```bash
# Navigate to backend directory
cd backend

# Create PostgreSQL database
createdb carparts

# Configure database credentials
# Edit src/main/resources/application.yml
# Update username, password, and other settings

# Build and run
./gradlew bootRun
```

The backend will start at `http://localhost:8080/api`

See [backend/README.md](backend/README.md) for detailed backend documentation.

#### 2. Android App Setup

```bash
# Navigate to project root
cd IntegMobile

# Create local.properties file
echo "sdk.dir=/path/to/Android/Sdk" > local.properties

# Add API keys to local.properties
echo "MAPS_API_KEY=your_google_maps_api_key" >> local.properties
echo "STRIPE_PUBLISHABLE_KEY=your_stripe_key" >> local.properties

# Build the app
./gradlew :app:assembleDebug

# Or open in Android Studio and run
```

See [app/README.md](app/README.md) for detailed Android app documentation.

## 🔑 Key Features

### Backend Features
- ✅ JWT-based authentication with email verification
- ✅ User registration and login
- ✅ Password reset with OTP
- ✅ Product catalog management
- ✅ Shopping cart operations
- ✅ Order processing and tracking
- ✅ Order claims/returns
- ✅ Google OAuth integration (placeholder)
- ✅ Stripe payment integration (placeholder)
- ✅ CORS configuration for mobile clients
- ✅ Automatic data seeding

### Android App Features
- ✅ Modern MVVM architecture
- ✅ User authentication (Login, Sign Up, Email Verification)
- ✅ Password reset flow
- ✅ Product browsing and search
- ✅ Product filtering
- ✅ Shopping cart management
- ✅ Google Maps location picker
- ✅ Multiple payment methods (Stripe, Cash on Delivery)
- ✅ Order history and tracking
- ✅ Order claims/returns
- ✅ User profile management
- ✅ Offline support with Room database
- ✅ Material Design 3 UI
- ✅ Bottom navigation
- ✅ Secure token storage

## 🛠️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.0
- **Language**: Kotlin 1.9.20
- **Database**: PostgreSQL
- **ORM**: Hibernate/JPA
- **Security**: Spring Security + JWT
- **Build Tool**: Gradle
- **Dependencies**:
  - Spring Web
  - Spring Data JPA
  - Spring Security
  - Spring Mail
  - JWT (io.jsonwebtoken)
  - Stripe Java SDK
  - Google API Client
  - PostgreSQL Driver

### Android App
- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **UI**: XML Layouts + ViewBinding
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Dependencies**:
  - AndroidX Core, AppCompat, Material
  - Lifecycle & ViewModel
  - Navigation Component
  - Coroutines
  - Retrofit + OkHttp
  - Room Database
  - Google Maps & Location Services
  - Google Sign-In
  - Stripe Android SDK
  - Glide (Image Loading)
  - Encrypted SharedPreferences

## 📡 API Endpoints

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/verify-email` - Email verification with OTP
- `POST /api/auth/request-password-reset` - Request password reset
- `POST /api/auth/verify-otp` - Verify OTP
- `POST /api/auth/reset-password` - Reset password
- `POST /api/auth/google-signin` - Google OAuth login

### Products (Requires Authentication)
- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/search?query={query}` - Search products
- `POST /api/products/filter` - Filter products

### Cart (Requires Authentication)
- `GET /api/cart` - Get user's cart
- `POST /api/cart` - Add item to cart
- `PUT /api/cart/{id}` - Update cart item quantity
- `DELETE /api/cart/{id}` - Remove item from cart
- `DELETE /api/cart` - Clear cart

### Orders (Requires Authentication)
- `GET /api/orders` - Get user's orders
- `GET /api/orders/{id}` - Get order details
- `POST /api/orders` - Create new order
- `PUT /api/orders/{id}/cancel` - Cancel order
- `POST /api/orders/claim` - Submit order claim

## 🔐 Authentication Flow

1. **Registration**: User signs up with email and password
2. **Email Verification**: OTP sent to email (printed to console in dev)
3. **Login**: User receives JWT token
4. **Token Storage**: Android app stores token securely
5. **API Requests**: Token included in Authorization header
6. **Token Refresh**: Automatic handling of expired tokens

## 🗄️ Database Schema

### Backend (PostgreSQL)
- `users` - User accounts
- `products` - Product catalog
- `cart_items` - Shopping cart items
- `orders` - Order records
- `otp_verifications` - Email verification OTPs

### Android (Room)
- Local caching of products, cart, and orders
- Offline-first architecture
- Automatic sync with backend

## 🧪 Testing

### Backend Testing
```bash
cd backend

# Run all tests
./gradlew test

# Test specific endpoint
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

### Android Testing
```bash
# Run unit tests
./gradlew :app:test

# Run instrumented tests
./gradlew :app:connectedAndroidTest
```

## 📱 Running the App

### On Emulator
1. Start the backend: `cd backend && ./gradlew bootRun`
2. Use `http://10.0.2.2:8080/api/` as BASE_URL in `Constants.kt`
3. Run the app from Android Studio

### On Physical Device
1. Find your computer's IP address
2. Update BASE_URL in `app/src/main/java/com/integmobile/utils/Constants.kt`:
   ```kotlin
   const val BASE_URL = "http://YOUR_IP:8080/api/"
   ```
3. Ensure device and computer are on the same network
4. Run the app

## 🐛 Troubleshooting

### Backend Issues

**Port already in use**
```yaml
# Change port in application.yml
server:
  port: 8081
```

**Database connection error**
- Ensure PostgreSQL is running: `sudo service postgresql status`
- Check credentials in `application.yml`
- Verify database exists: `psql -l`

**CORS errors**
- Add your device IP to `cors.allowed-origins` in `application.yml`

### Android App Issues

**Network error**
- Check BASE_URL in `Constants.kt`
- Ensure backend is running
- Check device/emulator network connectivity

**Build errors**
- Sync Gradle: `File > Sync Project with Gradle Files`
- Clean build: `./gradlew clean build`
- Invalidate caches: `File > Invalidate Caches / Restart`

**Google Maps not showing**
- Verify MAPS_API_KEY in `local.properties`
- Enable Maps SDK in Google Cloud Console
- Check API key restrictions

## 📄 License

This project is for educational purposes.

## 👥 Contributing

This is a student project. For questions or issues, please contact the development team.

## 📞 Support

For detailed documentation:
- Backend: See [backend/README.md](backend/README.md)
- Android App: See [app/README.md](app/README.md)

## 🔄 Version History

- **v1.0.0** - Initial release
  - Complete backend API
  - Full-featured Android app
  - Authentication and authorization
  - Product catalog and search
  - Shopping cart
  - Order management
  - Payment integration (Stripe)
  - Google Maps integration
