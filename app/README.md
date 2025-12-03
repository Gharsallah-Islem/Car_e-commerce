# Car Parts Android App

A modern Android e-commerce application for car parts, built with Kotlin and following MVVM architecture with offline-first capabilities.

## 📱 Features

### Authentication
- ✅ User registration with email verification
- ✅ Email/password login
- ✅ Google Sign-In integration
- ✅ Password reset with OTP verification
- ✅ Secure JWT token storage
- ✅ Automatic token refresh

### Product Catalog
- ✅ Browse all products
- ✅ Search products by name/description
- ✅ Filter products by category, price, brand
- ✅ View detailed product information
- ✅ Product images with Glide
- ✅ Offline product caching

### Shopping Cart
- ✅ Add/remove items
- ✅ Update quantities
- ✅ Real-time price calculation
- ✅ Persistent cart across sessions
- ✅ Sync with backend

### Checkout & Orders
- ✅ Google Maps location picker
- ✅ Multiple payment methods:
  - Stripe credit card payment
  - Cash on delivery
- ✅ Order history
- ✅ Order tracking
- ✅ Order details view
- ✅ Cancel orders
- ✅ Submit order claims/returns

### User Profile
- ✅ View profile information
- ✅ Update profile details
- ✅ Logout functionality

## 🏗️ Architecture

### MVVM Pattern
```
UI Layer (Activities/Fragments)
    ↓
ViewModel Layer (Business Logic)
    ↓
Repository Layer (Data Abstraction)
    ↓
Data Sources (API + Local DB)
```

### Project Structure
```
app/src/main/java/com/integmobile/
├── CarPartsApplication.kt          # Application class
├── data/                            # Data Layer
│   ├── api/                         # Retrofit API Services
│   │   ├── AuthApiService.kt
│   │   ├── CartApiService.kt
│   │   ├── OrderApiService.kt
│   │   ├── PaymentApiService.kt
│   │   ├── ProductApiService.kt
│   │   └── RetrofitClient.kt       # Retrofit configuration
│   ├── db/                          # Room Database
│   │   ├── AppDatabase.kt
│   │   ├── dao/                     # Data Access Objects
│   │   ├── entity/                  # Database entities
│   │   └── converters/              # Type converters
│   ├── model/                       # Data models
│   │   ├── request/                 # API request models
│   │   └── response/                # API response models
│   └── repository/                  # Repository pattern
│       ├── AuthRepository.kt
│       ├── CartRepository.kt
│       ├── OrderRepository.kt
│       ├── PaymentRepository.kt
│       └── ProductRepository.kt
├── ui/                              # UI Layer
│   ├── MainActivity.kt              # Main container
│   ├── SplashActivity.kt            # Splash screen
│   ├── auth/                        # Authentication screens
│   │   ├── LoginActivity.kt
│   │   ├── SignUpActivity.kt
│   │   ├── VerifyEmailActivity.kt
│   │   ├── ResetPasswordActivity.kt
│   │   └── viewmodel/
│   ├── products/                    # Product catalog
│   │   ├── ProductsFragment.kt
│   │   ├── ProductDetailFragment.kt
│   │   ├── adapter/
│   │   └── viewmodel/
│   ├── cart/                        # Shopping cart
│   │   ├── CartFragment.kt
│   │   ├── adapter/
│   │   └── viewmodel/
│   ├── checkout/                    # Checkout flow
│   │   ├── CheckoutFragment.kt
│   │   ├── LocationPickerFragment.kt
│   │   ├── PaymentMethodFragment.kt
│   │   └── viewmodel/
│   ├── orders/                      # Order management
│   │   ├── OrdersFragment.kt
│   │   ├── OrderDetailFragment.kt
│   │   ├── ClaimOrderFragment.kt
│   │   ├── adapter/
│   │   └── viewmodel/
│   └── profile/                     # User profile
│       └── ProfileFragment.kt
└── utils/                           # Utilities
    ├── Constants.kt                 # App constants
    ├── Extensions.kt                # Kotlin extensions
    ├── Result.kt                    # Result wrapper
    ├── TokenInterceptor.kt          # JWT interceptor
    ├── TokenManager.kt              # Token storage
    └── Validators.kt                # Input validation
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 24+ (minimum)
- Android SDK 34 (target)
- Google Maps API Key
- Stripe Publishable Key

### Setup Instructions

#### 1. Clone and Open Project
```bash
# Open in Android Studio
# File > Open > Select IntegMobile directory
```

#### 2. Configure API Keys

Create or edit `local.properties` in the project root:
```properties
sdk.dir=/path/to/Android/Sdk
MAPS_API_KEY=your_google_maps_api_key_here
STRIPE_PUBLISHABLE_KEY=your_stripe_publishable_key_here
```

#### 3. Configure Backend URL

Edit `app/src/main/java/com/integmobile/utils/Constants.kt`:

```kotlin
object Constants {
    // For Android Emulator
    const val BASE_URL = "http://10.0.2.2:8080/api/"
    
    // For Physical Device (replace with your computer's IP)
    // const val BASE_URL = "http://192.168.1.100:8080/api/"
}
```

#### 4. Sync and Build
```bash
# In Android Studio
File > Sync Project with Gradle Files

# Or via command line
./gradlew :app:assembleDebug
```

#### 5. Run the App
- Click Run button in Android Studio
- Or use: `./gradlew :app:installDebug`

## 🔧 Configuration

### Backend Connection

**For Emulator:**
```kotlin
const val BASE_URL = "http://10.0.2.2:8080/api/"
```

**For Physical Device:**
1. Find your computer's IP address:
   ```bash
   # Linux/Mac
   ifconfig | grep "inet "
   
   # Windows
   ipconfig
   ```
2. Update Constants.kt:
   ```kotlin
   const val BASE_URL = "http://YOUR_IP:8080/api/"
   ```
3. Ensure device and computer are on the same network

### Google Maps Setup

1. Get API key from [Google Cloud Console](https://console.cloud.google.com/)
2. Enable Maps SDK for Android
3. Add key to `local.properties`:
   ```properties
   MAPS_API_KEY=your_api_key_here
   ```

### Stripe Setup

1. Get publishable key from [Stripe Dashboard](https://dashboard.stripe.com/)
2. Add to `local.properties`:
   ```properties
   STRIPE_PUBLISHABLE_KEY=pk_test_your_key_here
   ```

## 📦 Dependencies

### Core Android
```gradle
androidx.core:core-ktx:1.12.0
androidx.appcompat:appcompat:1.6.1
com.google.android.material:material:1.10.0
androidx.constraintlayout:constraintlayout:2.1.4
```

### Architecture Components
```gradle
// Lifecycle & ViewModel
androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2
androidx.lifecycle:lifecycle-livedata-ktx:2.6.2

// Navigation Component
androidx.navigation:navigation-fragment-ktx:2.7.5
androidx.navigation:navigation-ui-ktx:2.7.5
```

### Networking
```gradle
// Retrofit
com.squareup.retrofit2:retrofit:2.9.0
com.squareup.retrofit2:converter-gson:2.9.0
com.squareup.okhttp3:okhttp:4.12.0
com.squareup.okhttp3:logging-interceptor:4.12.0
```

### Database
```gradle
// Room
androidx.room:room-runtime:2.6.1
androidx.room:room-ktx:2.6.1
```

### Async
```gradle
// Coroutines
org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3
org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3
```

### Third-Party Services
```gradle
// Google Maps & Location
com.google.android.gms:play-services-maps:18.2.0
com.google.android.gms:play-services-location:21.0.1

// Google Sign-In
com.google.android.gms:play-services-auth:20.7.0

// Stripe
com.stripe:stripe-android:20.35.1

// Glide (Image Loading)
com.github.bumptech.glide:glide:4.16.0
```

### Security
```gradle
// Encrypted SharedPreferences
androidx.security:security-crypto:1.1.0-alpha06
```

## 🎨 UI Components

### Navigation
- Bottom Navigation with 4 tabs:
  - Products
  - Cart
  - Orders
  - Profile
- Navigation Component for fragment transitions
- Safe Args for type-safe navigation

### Layouts
- Material Design 3 components
- ConstraintLayout for responsive UI
- RecyclerView with custom adapters
- ViewBinding for type-safe view access

### Screens

#### Authentication Flow
1. **SplashActivity** - App initialization
2. **LoginActivity** - Email/password login
3. **SignUpActivity** - User registration
4. **VerifyEmailActivity** - OTP verification
5. **ResetPasswordActivity** - Password recovery

#### Main App Flow
1. **ProductsFragment** - Browse products
2. **ProductDetailFragment** - Product details
3. **CartFragment** - Shopping cart
4. **CheckoutFragment** - Order summary
5. **LocationPickerFragment** - Delivery address
6. **PaymentMethodFragment** - Payment selection
7. **OrdersFragment** - Order history
8. **OrderDetailFragment** - Order details
9. **ClaimOrderFragment** - Submit claims
10. **ProfileFragment** - User profile

## 🔐 Security

### Token Management
- JWT tokens stored in Encrypted SharedPreferences
- Automatic token injection via OkHttp Interceptor
- Secure token refresh mechanism

### Data Protection
- All network traffic over HTTPS (production)
- Encrypted local storage
- No sensitive data in logs (production builds)

### Permissions
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

## 🧪 Testing

### Run Unit Tests
```bash
./gradlew :app:test
```

### Run Instrumented Tests
```bash
./gradlew :app:connectedAndroidTest
```

### Manual Testing Checklist
- [ ] User registration and email verification
- [ ] Login with valid/invalid credentials
- [ ] Password reset flow
- [ ] Browse and search products
- [ ] Add/remove items from cart
- [ ] Update cart quantities
- [ ] Complete checkout with Stripe
- [ ] Complete checkout with Cash on Delivery
- [ ] View order history
- [ ] View order details
- [ ] Cancel order
- [ ] Submit order claim
- [ ] Update profile
- [ ] Logout and login again
- [ ] Test offline functionality

## 🐛 Troubleshooting

### Build Errors

**Gradle sync failed**
```bash
# Clean and rebuild
./gradlew clean
./gradlew build

# Or in Android Studio
Build > Clean Project
Build > Rebuild Project
```

**Dependency resolution errors**
```bash
# Invalidate caches
File > Invalidate Caches / Restart
```

### Runtime Errors

**Network errors**
- Check BASE_URL in Constants.kt
- Ensure backend is running
- Verify device/emulator has internet access
- Check firewall settings

**Google Maps not showing**
- Verify MAPS_API_KEY in local.properties
- Check API key is enabled in Google Cloud Console
- Ensure Maps SDK for Android is enabled
- Check API key restrictions (remove for testing)

**Stripe payment fails**
- Verify STRIPE_PUBLISHABLE_KEY
- Use test card: 4242 4242 4242 4242
- Check Stripe dashboard for errors

**App crashes on startup**
- Check logcat for stack trace
- Verify all required permissions
- Ensure backend is accessible
- Clear app data and reinstall

### Common Issues

**Cannot connect to backend from emulator**
```kotlin
// Use 10.0.2.2 instead of localhost
const val BASE_URL = "http://10.0.2.2:8080/api/"
```

**Cannot connect from physical device**
```kotlin
// Use computer's IP address
const val BASE_URL = "http://192.168.1.100:8080/api/"

// Ensure both devices are on same network
// Check firewall allows connections on port 8080
```

**Token expired errors**
- Token automatically refreshes on 401 errors
- If persistent, logout and login again
- Check backend JWT expiration settings

## 📱 App Flow

### First Time User
1. Launch app → Splash screen
2. No token → Navigate to Login
3. Click "Sign Up"
4. Enter details → Register
5. Verify email with OTP
6. Login with credentials
7. Browse products

### Returning User
1. Launch app → Splash screen
2. Valid token → Navigate to Products
3. Continue shopping

### Shopping Flow
1. Browse products
2. Search/filter products
3. View product details
4. Add to cart
5. View cart
6. Proceed to checkout
7. Select delivery location (Google Maps)
8. Choose payment method
9. Confirm order
10. View order in Orders tab

## 🔄 Offline Support

### Cached Data
- Products are cached in Room database
- Cart persists locally
- Orders synced when online
- Automatic sync on network restore

### Sync Strategy
- Fetch from network first
- Fallback to local cache on error
- Update cache on successful fetch
- Queue operations when offline

## 📊 Performance

### Optimizations
- Image loading with Glide (caching, resizing)
- RecyclerView with ViewHolder pattern
- Coroutines for async operations
- Room database for fast local queries
- Retrofit with OkHttp connection pooling

### Best Practices
- ViewBinding (no findViewById)
- Lifecycle-aware components
- Repository pattern for data abstraction
- Single Activity architecture
- Proper memory management

## 🚀 Building for Production

### Release Build
```bash
# Generate signed APK
./gradlew :app:assembleRelease

# Or create App Bundle
./gradlew :app:bundleRelease
```

### ProGuard Configuration
ProGuard rules are defined in `app/proguard-rules.pro`

### Checklist
- [ ] Update BASE_URL to production server
- [ ] Use production Stripe keys
- [ ] Enable ProGuard/R8
- [ ] Remove debug logging
- [ ] Test on multiple devices
- [ ] Verify all API keys
- [ ] Update version code/name
- [ ] Generate signed APK/Bundle

## 📄 Additional Resources

- [Android Developer Guide](https://developer.android.com/)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Material Design](https://material.io/design)
- [Retrofit](https://square.github.io/retrofit/)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Stripe Android SDK](https://stripe.com/docs/mobile/android)
- [Google Maps Android](https://developers.google.com/maps/documentation/android-sdk)

## 📞 Support

For backend API documentation, see [../backend/README.md](../backend/README.md)

For project overview, see [../README.md](../README.md)
