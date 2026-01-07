# 🚗 Car Parts E-Commerce App - Complete Testing Guide

## 📋 Project Overview

This is a full-stack Android e-commerce application for car parts with:
- **Backend**: Spring Boot REST API (http://localhost:8080)
- **Frontend**: Native Android (Kotlin)
- **Database**: PostgreSQL (backend) + Room (local cache)
- **Payment**: Stripe integration
- **AI**: Groq LLM-powered virtual assistant

---

## 🏗️ Architecture

### Android App Structure:
```
app/src/main/java/com/example/carpartsecom/
├── MainActivity.kt          # App entry, navigation, dependency injection
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt   # Room database
│   │   ├── dao/             # Data Access Objects
│   │   └── entities/        # Room entities
│   ├── remote/
│   │   ├── api/             # Retrofit API services
│   │   ├── dto/             # Data Transfer Objects
│   │   └── RetrofitClient.kt
│   └── repository/          # Repository pattern implementations
├── ui/
│   ├── fragment/            # All UI screens
│   ├── adapter/             # RecyclerView adapters
│   └── viewmodel/           # ViewModels + Factory
└── util/
    ├── Constants.kt         # App configuration
    ├── TokenManager.kt      # JWT token management
    ├── StripePaymentHelper.kt
    └── CarAssistant.kt      # Local AI fallback
```

### Key Features:
| Feature | Description |
|---------|-------------|
| Authentication | Register, Login, OTP Verification, Password Reset |
| Products | Browse, Search, Sort, Filter by Category |
| Cart | Add, Update Quantity, Remove, Clear |
| Checkout | Address, Phone, GPS Location, Payment Method |
| Payment | Cash on Delivery OR Stripe Card Payment |
| Orders | View Orders, Order Details, Cancel Orders |
| Claims | Submit claims for orders |
| AI Assistant | Groq LLM + Local fallback for car diagnostics |
| Profile | View/Edit profile, Change password, Logout |

---

## 🧪 COMPLETE TEST FLOW

### Prerequisites:
1. ✅ Backend running at `http://localhost:8080`
2. ✅ PostgreSQL database running
3. ✅ Android app installed on emulator/device
4. ✅ Internet connection (for Stripe & AI)

---

## 📱 TEST SCENARIO 1: New User Registration

### Steps:
1. **Launch App** → Should see Login screen
2. **Tap "Register"** → Navigate to Registration screen

### Registration Screen Test:
| Field | Test Input | Expected |
|-------|------------|----------|
| First Name | `J` | ❌ Error: "At least 2 characters" |
| First Name | `John123` | ❌ Error: "Only letters allowed" |
| First Name | `John` | ✅ Valid |
| Last Name | `D` | ❌ Error: "At least 2 characters" |
| Last Name | `Doe` | ✅ Valid |
| Email | `invalid` | ❌ Error: "Invalid email" |
| Email | `test@example.com` | ✅ Valid |
| Password | `123` | ❌ Error: "At least 6 characters" |
| Password | `password123` | ✅ Valid |
| Confirm Password | `different` | ❌ Error: "Passwords don't match" |
| Confirm Password | `password123` | ✅ Valid |

3. **Submit valid registration** → Should show "Check email for OTP"
4. **Check backend console/email** for 6-digit OTP code

### OTP Verification Test:
5. **Enter wrong OTP** (e.g., `000000`) → ❌ Error message
6. **Enter correct OTP** → ✅ "Email verified" → Navigate to Login

---

## 📱 TEST SCENARIO 2: Login Flow

### Steps:
1. **Enter wrong password** → ❌ "Invalid credentials" error
2. **Enter correct credentials** → ✅ Navigate to Products screen
3. **Verify bottom navigation visible** with: Products | Cart | Assistant | Orders | Profile

---

## 📱 TEST SCENARIO 3: Product Browsing

### Products Screen Tests:

| Action | Expected Result |
|--------|-----------------|
| Pull down to refresh | Products reload from API |
| Tap search icon | Search bar appears |
| Search "brake" | Only brake-related products show |
| Search "xyz123" | "No products found" message |
| Clear search | All products show |
| Tap Sort button | Sort options appear |
| Sort by Price (Low-High) | Products ordered by price ascending |
| Sort by Price (High-Low) | Products ordered by price descending |
| Sort by Rating | Products ordered by rating |
| Sort by Name | Products ordered alphabetically |
| Tap category chip "Brakes" | Only brake products show |
| Tap category chip "All" | All products show |
| Tap product card | Navigate to Product Detail |

### Product Detail Screen Tests:
| Action | Expected Result |
|--------|-----------------|
| View product info | Name, price, description, rating, stock visible |
| Tap "-" when quantity is 1 | Quantity stays at 1 |
| Tap "+" | Quantity increases |
| Tap "Add to Cart" | "Added to cart" toast, cart badge updates |
| Tap back button | Return to products list |

---

## 📱 TEST SCENARIO 4: Shopping Cart

### Cart Screen Tests:
1. **Navigate to Cart tab** → Shows cart items
2. **Verify cart item displays**: Product image, name, price, quantity controls

| Action | Expected Result |
|--------|-----------------|
| Tap "+" on item | Quantity increases, total updates |
| Tap "-" on item | Quantity decreases, total updates |
| Tap "-" when quantity is 1 | Item removed from cart |
| Swipe item left | Delete option appears |
| Tap delete | Item removed |
| Empty cart | "Your cart is empty" message |
| Add items back | Items appear with subtotal |
| Tap "Checkout" | Navigate to Checkout screen |

---

## 📱 TEST SCENARIO 5: Checkout Flow

### Checkout Screen Validation Tests:

| Field | Test Input | Expected |
|-------|------------|----------|
| Address | Empty | ❌ "Address is required" |
| Address | `Hi` | ❌ "Enter valid address" |
| Address | `123 Main Street` | ✅ Valid |
| Phone | Empty | ❌ "Phone required" |
| Phone | `123` | ❌ "Invalid phone (8-15 digits)" |
| Phone | `+1234567890` | ✅ Valid |
| Latitude | Empty | ❌ "Required" |
| Latitude | `abc` | ❌ "Invalid number" |
| Latitude | `100` | ❌ "Must be -90 to 90" |
| Longitude | `-200` | ❌ "Must be -180 to 180" |

### Map Picker Test:
1. **Tap "Select on Map"** → Map opens
2. **Pan/zoom map** → Map responds
3. **Tap location** → Marker placed
4. **Tap "Confirm Location"** → Returns to checkout
5. **Verify lat/lng fields populated** with selected coordinates

### Cash on Delivery Test:
1. Fill all fields correctly
2. Select "Cash on Delivery" radio
3. Tap "Place Order"
4. **Expected**: Order created, navigate to Orders tab
5. **Verify in Orders**: New order with status "PENDING"

### Card Payment (Stripe) Test:
1. Add items to cart, go to checkout
2. Fill delivery info
3. Select "Pay with Card" radio
4. Tap "Place Order"
5. **Stripe Payment Sheet appears**
6. Enter test card: `4242 4242 4242 4242`, any future date, any CVC
7. Complete payment
8. **Expected**: Order created with payment intent ID

---

## 📱 TEST SCENARIO 6: Orders Management

### Orders Screen Tests:
1. **Navigate to Orders tab** → Shows only YOUR orders (not other users')
2. **Pull to refresh** → Orders reload

| Action | Expected Result |
|--------|-----------------|
| Tap order card | Navigate to Order Details |
| View PENDING order | Shows "Cancel Order" button |
| Tap "Cancel Order" | Confirmation, order status changes to CANCELLED |
| View COMPLETED order | No cancel button |
| View CANCELLED order | No cancel button, red status badge |

### Order Details Screen Tests:
| Element | Verification |
|---------|--------------|
| Order ID | Shows "Order #X" |
| Status Badge | Color matches status (yellow=pending, green=completed, red=cancelled) |
| Date | Formatted date/time |
| Total | Currency formatted |
| Payment Method | "Cash on Delivery" or "Card Payment (Stripe)" |
| Payment Intent ID | Shows for card payments |
| GPS Coordinates | Shows lat/lng if provided |
| Delivery Address | Shows address |
| Phone | Shows contact phone |
| Notes | Shows delivery notes |
| Order Items | List of products with quantities and prices |

---

## 📱 TEST SCENARIO 7: AI Car Assistant

### Assistant Screen Tests:
1. **Navigate to Assistant tab** → Chat interface loads
2. **See welcome message** with capabilities

### Conversation Tests:
| User Message | Expected Response Contains |
|--------------|---------------------------|
| "Hello" | Greeting, ask how to help |
| "My brakes squeak" | Brake diagnosis, recommends Brake Pads |
| "Check engine light is on" | CEL causes, recommends Spark Plugs |
| "Car won't start" | Battery diagnosis, recommends Car Battery |
| "When should I change oil?" | Oil change intervals, recommends Oil Filter |
| "I have a 2020 Toyota Camry" | Acknowledges car, asks what help needed |
| "I hear a grinding noise when braking" | Urgent warning, mechanic referral |

### Quick Replies Test:
1. After assistant responds, **quick reply chips appear**
2. **Tap a chip** → Sends that message
3. **Chips update** based on conversation context

### Product Recommendations Test:
1. Mention brakes → **Brake Pads card appears** below chat
2. **Tap product card** → Navigate to Product Detail
3. **Clear chat button** → Clears conversation history

---

## 📱 TEST SCENARIO 8: Claims

### Access Claims:
1. **Go to Profile tab**
2. **Scroll to "Support" section**
3. **Tap "My Claims"** → Navigate to Claims screen

### Claims Screen Tests:
| Action | Expected Result |
|--------|-----------------|
| View claims list | Shows user's claims (if any) |
| Tap "New Claim" FAB | Claim form appears |
| Select order | Order dropdown populated |
| Enter subject | Required field |
| Enter description | Required field |
| Submit claim | Claim created, appears in list |
| View claim status | Shows PENDING/RESOLVED status |

---

## 📱 TEST SCENARIO 9: Profile Management

### Profile Screen Tests:
1. **Navigate to Profile tab**
2. **Verify header shows**: Avatar, Name, Email, Verified badge

### Edit Profile Test:
| Field | Test | Expected |
|-------|------|----------|
| First Name | Change to "Johnny" | ✅ Updates after save |
| Last Name | Change to "Smith" | ✅ Updates after save |
| Phone | Add "+1987654321" | ✅ Updates after save |
| First Name | Empty | ❌ "Required" error |

### Change Password Test:
1. **Tap "Change Password"** → Dialog opens
2. **Enter wrong current password** → ❌ "Current password incorrect"
3. **Enter correct current + new password** → ✅ "Password changed"
4. **Logout and login with new password** → ✅ Success

### Logout Test:
1. **Tap "Logout"** → Confirmation dialog
2. **Confirm logout** → Navigate to Login screen
3. **Verify bottom nav hidden**
4. **Press back button** → Should NOT go back to app (logged out)

---

## 📱 TEST SCENARIO 10: Edge Cases & Error Handling

### Network Error Tests:
1. **Turn off WiFi/Data**
2. **Try to load products** → Error message, retry button
3. **Try to checkout** → Error message
4. **Turn network back on** → Retry works

### Authentication Errors:
1. **Clear app data**
2. **Try to access cart/orders directly** → Redirected to login
3. **Use expired token** → Should handle gracefully

### Validation Edge Cases:
| Test | Expected |
|------|----------|
| Add 0 quantity to cart | Should not allow |
| Add negative quantity | Should not allow |
| Order with empty cart | Error: "Cart is empty" |
| Cancel already cancelled order | Error message |
| Access non-existent order | Error message, navigate back |

---

## 🔧 Backend API Endpoints Reference

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/auth/register` | POST | No | Register new user |
| `/api/auth/verify-email` | POST | No | Verify OTP |
| `/api/auth/login` | POST | No | Login |
| `/api/auth/forgot-password` | POST | No | Request password reset |
| `/api/auth/reset-password` | POST | No | Reset password with OTP |
| `/api/user/profile` | GET | Yes | Get user profile |
| `/api/user/profile` | PUT | Yes | Update profile |
| `/api/user/change-password` | PUT | Yes | Change password |
| `/api/products` | GET | No | Get all products |
| `/api/products/{id}` | GET | No | Get product by ID |
| `/api/products/search?query=` | GET | No | Search products |
| `/api/products/sort?by=` | GET | No | Sort products |
| `/api/products/category/{cat}` | GET | No | Filter by category |
| `/api/cart` | GET | Yes | Get cart items |
| `/api/cart/add` | POST | Yes | Add to cart |
| `/api/cart/update` | PUT | Yes | Update quantity |
| `/api/cart/remove/{id}` | DELETE | Yes | Remove item |
| `/api/cart/clear` | DELETE | Yes | Clear cart |
| `/api/payment/create-intent` | POST | Yes | Create Stripe intent |
| `/api/payment/verify/{id}` | GET | Yes | Verify payment |
| `/api/orders` | GET | Yes | Get user orders |
| `/api/orders` | POST | Yes | Create order |
| `/api/orders/{id}/cancel` | POST | Yes | Cancel order |
| `/api/claims` | GET | Yes | Get user claims |
| `/api/claims` | POST | Yes | Create claim |
| `/api/claims/order/{id}` | GET | Yes | Get claims for order |

---

## ✅ Test Checklist

### Authentication
- [ ] Register with valid data
- [ ] Register validation errors shown
- [ ] OTP verification works
- [ ] Login with valid credentials
- [ ] Login error for wrong password
- [ ] Forgot password flow
- [ ] Logout clears session

### Products
- [ ] Products load on app start
- [ ] Pull to refresh works
- [ ] Search filters products
- [ ] Sort options work (price, rating, name)
- [ ] Category filter works
- [ ] Product detail shows all info

### Cart
- [ ] Add product to cart
- [ ] Update quantity (+/-)
- [ ] Remove item
- [ ] Clear cart
- [ ] Cart persists after app restart
- [ ] Cart total calculates correctly

### Checkout
- [ ] Form validation works
- [ ] Map picker selects location
- [ ] GPS coordinates saved to order
- [ ] Cash on delivery creates order
- [ ] Stripe payment works
- [ ] Order confirmation shown

### Orders
- [ ] Shows only current user's orders
- [ ] Order details display correctly
- [ ] GPS coordinates display (if provided)
- [ ] Cancel pending order works
- [ ] Cannot cancel completed order
- [ ] Order items show correctly

### AI Assistant
- [ ] Welcome message shows
- [ ] Responds to car questions
- [ ] Product recommendations appear
- [ ] Quick replies work
- [ ] Clear chat works
- [ ] Fallback works without API key

### Claims
- [ ] Access from Profile
- [ ] Create new claim
- [ ] View claim status

### Profile
- [ ] Profile info displays
- [ ] Edit profile works
- [ ] Change password works
- [ ] Logout works

---

## 🐛 Known Issues to Watch For

1. **GPS coordinates**: Ensure map picker returns valid lat/lng
2. **Stripe test mode**: Use test cards only
3. **AI rate limits**: Groq free tier has 30 req/min limit
4. **Network errors**: Should show user-friendly messages
5. **Token expiration**: App should handle gracefully

---

## 📝 Test Data

### Stripe Test Cards:
| Card Number | Result |
|-------------|--------|
| `4242 4242 4242 4242` | Success |
| `4000 0000 0000 0002` | Declined |
| `4000 0000 0000 9995` | Insufficient funds |

### Sample Product Categories:
- Brakes
- Engine
- Ignition
- Electrical

### Sample Products:
- Brake Pads ($49.99)
- Oil Filter ($12.99)
- Spark Plug ($8.50)
- Car Battery ($120.00)

---

**Happy Testing! 🚀**

