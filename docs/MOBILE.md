# Mobile App Documentation

> Kotlin Android application for the AutoParts Store e-commerce platform

## Table of Contents
- [Overview](#overview)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Features](#features)
- [API Integration](#api-integration)
- [Build & Run](#build--run)

---

## Overview

The mobile application is a native Android app built with Kotlin that mirrors the customer-facing features of the web application.

| Attribute | Value |
|-----------|-------|
| **Platform** | Android |
| **Language** | Kotlin |
| **Min SDK** | 24 (Android 7.0) |
| **Target SDK** | 34 (Android 14) |
| **Architecture** | Clean Architecture + MVVM |

---

## Architecture

The app follows **Clean Architecture** principles with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           PRESENTATION LAYER                                 │
│                                                                              │
│  ┌────────────────┐    ┌────────────────┐    ┌────────────────┐            │
│  │   Activities   │    │   Fragments    │    │   ViewModels   │            │
│  │                │    │                │    │                │            │
│  │ MainActivity   │    │ HomeFragment   │    │ ProductVM      │            │
│  │                │    │ ProductFragment│    │ CartVM         │            │
│  │                │    │ CartFragment   │    │ OrderVM        │            │
│  │                │    │ ProfileFragment│    │ AuthVM         │            │
│  └────────────────┘    └────────────────┘    └────────────────┘            │
│                                                                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                              DATA LAYER                                      │
│                                                                              │
│  ┌────────────────────┐    ┌────────────────────┐    ┌──────────────────┐  │
│  │    Repositories    │    │       Remote       │    │      Local       │  │
│  │                    │    │                    │    │                  │  │
│  │ ProductRepository  │◄───│   RetrofitService  │    │   RoomDatabase   │  │
│  │ OrderRepository    │    │   API Interfaces   │    │   SharedPrefs    │  │
│  │ UserRepository     │    │                    │    │                  │  │
│  │ CartRepository     │    │                    │    │                  │  │
│  └────────────────────┘    └────────────────────┘    └──────────────────┘  │
│                                                                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                              UTIL LAYER                                      │
│                                                                              │
│  ┌────────────────┐    ┌────────────────┐    ┌────────────────┐            │
│  │ SessionManager │    │   Extensions   │    │  NetworkUtils  │            │
│  │                │    │                │    │                │            │
│  │ Token handling │    │ Helper funcs   │    │ Connectivity   │            │
│  │ User session   │    │ View binding   │    │ Error handling │            │
│  └────────────────┘    └────────────────┘    └────────────────┘            │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Project Structure

```
mobile-app/CarPartsEcom/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/carpartsecom/
│   │   │   │   ├── MainActivity.kt           # Main activity
│   │   │   │   ├── data/                     # Data layer
│   │   │   │   │   ├── local/                # Local storage
│   │   │   │   │   │   ├── dao/              # Room DAOs
│   │   │   │   │   │   ├── entity/           # Room entities
│   │   │   │   │   │   └── AppDatabase.kt    # Room database
│   │   │   │   │   ├── remote/               # Remote API
│   │   │   │   │   │   ├── api/              # Retrofit interfaces
│   │   │   │   │   │   ├── dto/              # API DTOs
│   │   │   │   │   │   └── RetrofitClient.kt # HTTP client
│   │   │   │   │   └── repository/           # Repositories
│   │   │   │   ├── ui/                       # Presentation layer
│   │   │   │   │   ├── auth/                 # Login/Register
│   │   │   │   │   ├── home/                 # Home screen
│   │   │   │   │   ├── products/             # Product catalog
│   │   │   │   │   ├── cart/                 # Shopping cart
│   │   │   │   │   ├── orders/               # Order history
│   │   │   │   │   ├── profile/              # User profile
│   │   │   │   │   ├── tracking/             # Delivery tracking
│   │   │   │   │   └── ai/                   # AI mechanic
│   │   │   │   └── util/                     # Utilities
│   │   │   │       ├── SessionManager.kt     # Session handling
│   │   │   │       ├── Extensions.kt         # Kotlin extensions
│   │   │   │       └── Constants.kt          # App constants
│   │   │   ├── res/                          # Resources
│   │   │   │   ├── layout/                   # XML layouts
│   │   │   │   ├── values/                   # Strings, colors, themes
│   │   │   │   ├── drawable/                 # Images, icons
│   │   │   │   └── navigation/               # Nav graphs
│   │   │   └── AndroidManifest.xml
│   │   └── test/                             # Unit tests
│   └── build.gradle.kts                      # App-level gradle
├── build.gradle.kts                          # Project-level gradle
└── README.md
```

---

## Features

### Feature Parity with Web

| Feature | Status | Description |
|---------|--------|-------------|
| **Home** | ✅ | Featured products, categories, search |
| **Product Catalog** | ✅ | Browse, filter, search products |
| **Product Details** | ✅ | Images, description, add to cart |
| **Shopping Cart** | ✅ | View, modify, checkout |
| **Checkout** | ✅ | Address, payment (Stripe/COD) |
| **Orders** | ✅ | Order history, details, tracking |
| **Delivery Tracking** | ✅ | Real-time map tracking |
| **Profile** | ✅ | Edit profile, change password |
| **AI Mechanic** | ✅ | Camera/upload, part recognition |
| **Chat Support** | ✅ | AI-powered chat |
| **Reclamations** | ✅ | Submit and track tickets |

### Mobile-Specific Features

| Feature | Description |
|---------|-------------|
| **Camera Integration** | Capture images for AI mechanic |
| **Push Notifications** | Order updates, delivery alerts |
| **Offline Caching** | View cached products/orders offline |
| **Biometric Auth** | Fingerprint/Face unlock |
| **Deep Linking** | Open specific products from links |

---

## Key Components

### MainActivity

The single activity that hosts all fragments using Navigation Component:

```kotlin
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var sessionManager: SessionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        
        setupNavigation()
        setupBottomNavigation()
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        // Handle auth state
        if (!sessionManager.isLoggedIn()) {
            navController.navigate(R.id.loginFragment)
        }
    }
}
```

### SessionManager

Handles authentication state and token storage:

```kotlin
class SessionManager(context: Context) {
    
    private val prefs = context.getSharedPreferences(
        "auth_prefs", 
        Context.MODE_PRIVATE
    )
    
    fun saveAuthToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }
    
    fun getAuthToken(): String? {
        return prefs.getString("auth_token", null)
    }
    
    fun saveUser(user: User) {
        val json = Gson().toJson(user)
        prefs.edit().putString("current_user", json).apply()
    }
    
    fun getCurrentUser(): User? {
        val json = prefs.getString("current_user", null) ?: return null
        return Gson().fromJson(json, User::class.java)
    }
    
    fun isLoggedIn(): Boolean {
        return getAuthToken() != null
    }
    
    fun logout() {
        prefs.edit().clear().apply()
    }
}
```

### RetrofitClient

HTTP client configuration with interceptors:

```kotlin
object RetrofitClient {
    
    private const val BASE_URL = "http://10.0.2.2:8080/api/"  // Emulator localhost
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = SessionManager.getInstance().getAuthToken()
        
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        
        return chain.proceed(request)
    }
}
```

---

## API Integration

### API Interfaces

```kotlin
// AuthApi.kt
interface AuthApi {
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<MessageResponse>
    
    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<MessageResponse>
}

// ProductApi.kt
interface ProductApi {
    
    @GET("products")
    suspend fun getProducts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("category") categoryId: String? = null,
        @Query("brand") brandId: String? = null,
        @Query("search") search: String? = null
    ): Response<PagedResponse<Product>>
    
    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: String): Response<Product>
}

// CartApi.kt
interface CartApi {
    
    @GET("cart")
    suspend fun getCart(): Response<Cart>
    
    @POST("cart/items")
    suspend fun addItem(@Body request: AddCartItemRequest): Response<Cart>
    
    @PUT("cart/items/{productId}")
    suspend fun updateQuantity(
        @Path("productId") productId: String,
        @Body request: UpdateQuantityRequest
    ): Response<Cart>
    
    @DELETE("cart/items/{productId}")
    suspend fun removeItem(@Path("productId") productId: String): Response<Cart>
}

// OrderApi.kt
interface OrderApi {
    
    @GET("orders")
    suspend fun getOrders(): Response<List<Order>>
    
    @GET("orders/{id}")
    suspend fun getOrder(@Path("id") id: String): Response<Order>
    
    @POST("orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<Order>
}
```

### Repositories

```kotlin
class ProductRepository(
    private val api: ProductApi,
    private val productDao: ProductDao
) {
    
    suspend fun getProducts(
        page: Int,
        categoryId: String? = null,
        brandId: String? = null,
        search: String? = null
    ): Result<PagedResponse<Product>> {
        return try {
            val response = api.getProducts(page, 20, categoryId, brandId, search)
            
            if (response.isSuccessful) {
                response.body()?.let { pagedResponse ->
                    // Cache products locally
                    productDao.insertAll(pagedResponse.content)
                    Result.success(pagedResponse)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Return cached data on network error
            val cached = productDao.getAll()
            if (cached.isNotEmpty()) {
                Result.success(PagedResponse(cached, cached.size, 1))
            } else {
                Result.failure(e)
            }
        }
    }
}
```

---

## UI Components

### Product List Fragment

```kotlin
class ProductListFragment : Fragment() {
    
    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ProductViewModel by viewModels()
    private lateinit var adapter: ProductAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupSearch()
        observeProducts()
        
        viewModel.loadProducts()
    }
    
    private fun setupRecyclerView() {
        adapter = ProductAdapter { product ->
            navigateToDetail(product.id)
        }
        
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = this@ProductListFragment.adapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    // Pagination logic
                }
            })
        }
    }
    
    private fun observeProducts() {
        viewModel.products.observe(viewLifecycleOwner) { products ->
            adapter.submitList(products)
        }
        
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }
    }
}
```

### Product Adapter

```kotlin
class ProductAdapter(
    private val onClick: (Product) -> Unit
) : ListAdapter<Product, ProductViewHolder>(ProductDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }
}

class ProductViewHolder(
    private val binding: ItemProductBinding
) : RecyclerView.ViewHolder(binding.root) {
    
    fun bind(product: Product, onClick: (Product) -> Unit) {
        binding.apply {
            productName.text = product.name
            productPrice.text = formatPrice(product.price)
            
            Glide.with(productImage.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.placeholder_product)
                .into(productImage)
            
            root.setOnClickListener { onClick(product) }
        }
    }
}
```

---

## Build & Run

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34
- Kotlin 1.9+

### Configuration

Update the API base URL in `Constants.kt`:

```kotlin
object Constants {
    // For emulator accessing localhost
    const val BASE_URL = "http://10.0.2.2:8080/api/"
    
    // For physical device (use your computer's IP)
    // const val BASE_URL = "http://192.168.1.x:8080/api/"
    
    // For production
    // const val BASE_URL = "https://api.yoursite.com/api/"
}
```

### Build Steps

```bash
cd mobile-app/CarPartsEcom

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

### Run in Android Studio

1. Open `mobile-app/CarPartsEcom` in Android Studio
2. Wait for Gradle sync
3. Select device/emulator
4. Click Run (▶️)

---

## Dependencies

```kotlin
// build.gradle.kts (app level)
dependencies {
    // AndroidX
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    
    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    
    // Image Loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Material Design
    implementation("com.google.android.material:material:1.11.0")
    
    // Maps (for tracking)
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    
    // Stripe
    implementation("com.stripe:stripe-android:20.37.0")
}
```

---

## Permissions

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```
