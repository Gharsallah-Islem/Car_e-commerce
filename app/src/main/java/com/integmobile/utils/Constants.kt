package com.integmobile.utils

object Constants {
    // API Configuration
    const val BASE_URL = "http://10.0.2.2:8080/api/" // For Android Emulator
    // For physical device, use: http://YOUR_IP_ADDRESS:8080/api/
    
    // API Keys (Replace with actual keys)
    const val GOOGLE_MAPS_API_KEY = "XXXXXXX"
    const val GOOGLE_CLIENT_ID = "XXXXXXX"
    const val STRIPE_PUBLISHABLE_KEY = "XXXXXXX"
    
    // SharedPreferences Keys
    const val PREFS_NAME = "car_parts_prefs"
    const val KEY_AUTH_TOKEN = "auth_token"
    const val KEY_REFRESH_TOKEN = "refresh_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_USER_EMAIL = "user_email"
    const val KEY_IS_LOGGED_IN = "is_logged_in"
    
    // Database
    const val DATABASE_NAME = "car_parts_db"
    const val DATABASE_VERSION = 1
    
    // API Endpoints
    object Endpoints {
        // Auth
        const val LOGIN = "auth/login"
        const val REGISTER = "auth/register"
        const val VERIFY_EMAIL = "auth/verify-email"
        const val RESET_PASSWORD = "auth/reset-password"
        const val GOOGLE_SIGNIN = "auth/google-signin"
        const val REQUEST_PASSWORD_RESET = "auth/forgot-password"
        const val VERIFY_OTP = "auth/verify-otp"
        
        // Products
        const val PRODUCTS = "products"
        const val PRODUCT_DETAIL = "products/{id}"
        const val SEARCH_PRODUCTS = "products/search"
        const val FILTER_PRODUCTS = "products/filter"
        
        // Cart
        const val CART_ITEMS = "cart/items"
        const val CART_ITEM = "cart/items/{id}"
        const val CART_UPDATE = "cart/items/{id}"
        const val CART_DELETE = "cart/items/{id}"
        
        // Orders
        const val ORDERS = "orders"
        const val ORDER_DETAIL = "orders/{id}"
        const val CANCEL_ORDER = "orders/{id}/cancel"
        const val SUBMIT_CLAIM = "orders/{id}/claim"
        
        // Payment
        const val CREATE_PAYMENT_INTENT = "payments/create-intent"
        const val CONFIRM_PAYMENT = "payments/confirm"
    }
    
    // Order Status
    object OrderStatus {
        const val PENDING = "PENDING"
        const val CONFIRMED = "CONFIRMED"
        const val SHIPPED = "SHIPPED"
        const val OUT_FOR_DELIVERY = "OUT_FOR_DELIVERY"
        const val DELIVERED = "DELIVERED"
        const val CANCELLED = "CANCELLED"
    }
    
    // Payment Methods
    object PaymentMethod {
        const val CASH_ON_DELIVERY = "CASH_ON_DELIVERY"
        const val STRIPE = "STRIPE"
    }
    
    // Claim Reasons
    object ClaimReason {
        const val NOT_RECEIVED = "Order not received"
        const val DAMAGED = "Order received damaged"
        const val WRONG_ITEM = "Wrong item received"
        const val PARTIAL_SHIPMENT = "Partial shipment"
    }
    
    // Sort Options
    object SortBy {
        const val PRICE_LOW_TO_HIGH = "price_asc"
        const val PRICE_HIGH_TO_LOW = "price_desc"
        const val QUANTITY_ASC = "quantity_asc"
        const val QUANTITY_DESC = "quantity_desc"
        const val BRAND_A_Z = "brand_asc"
        const val NEWEST = "newest"
        const val MOST_POPULAR = "popular"
    }
    
    // Validation
    const val MIN_PASSWORD_LENGTH = 8
    const val OTP_LENGTH = 6
}
