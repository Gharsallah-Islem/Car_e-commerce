# 🚗 CarParts E-Commerce Android App - Complete Testing Guide

## 📋 Project Architecture Overview

### Package Structure
```
com.example.carpartsecom/
├── MainActivity.kt                 # Main activity with navigation
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt         # Room database (v6)
│   │   ├── dao/
│   │   │   ├── CartDao.kt         # Cart operations
│   │   │   ├── ClaimDao.kt        # Claims operations
│   │   │   ├── OrderDao.kt        # Orders operations
│   │   │   ├── OrderItemDao.kt    # Order items operations
│   │   │   ├── ProductDao.kt      # Products operations
│   │   │   └── UserDao.kt         # User operations
│   │   └── entities/
│   │       ├── CartItemEntity.kt
│   │       ├── CartItemWithProduct.kt
│   │       ├── ClaimEntity.kt
│   │       ├── OrderEntity.kt
│   │       ├── OrderItemEntity.kt
│   │       ├── OtpCodeEntity.kt
│   │       ├── ProductEntity.kt
│   │       └── UserEntity.kt
│   ├── remote/
│   │   ├── RetrofitClient.kt      # Network client
│   │   ├── api/
│   │   │   ├── AuthService.kt     # POST /api/auth/*
│   │   │   ├── CartService.kt     # /api/cart/*
│   │   │   ├── ClaimService.kt    # /api/claims/*
│   │   │   ├── OrderService.kt    # /api/orders/*
│   │   │   ├── PaymentService.kt  # /api/payment/*
│   │   │   ├── ProductService.kt  # /api/products/*
│   │   │   └── UserService.kt     # /api/user/*
│   │   └── dto/
│   │       ├── AuthDto.kt
│   │       ├── CartDto.kt
│   │       ├── ClaimDto.kt
│   │       ├── OrderDto.kt
│   │       ├── PaymentDto.kt
│   │       └── ProductDto.kt
│   └── repository/
│       ├── AuthRepository.kt
│       ├── CartRepository.kt
│       ├── ClaimRepository.kt
│       ├── OrderRepository.kt
│       ├── PaymentRepository.kt
│       ├── ProductRepository.kt
│       └── UserRepository.kt
├── ui/
│   ├── adapter/
│   │   ├── CartAdapter.kt
│   │   ├── OrderAdapter.kt
│   │   ├── OrderItemAdapter.kt
│   │   └── ProductAdapter.kt
│   ├── fragment/
│   │   ├── CartFragment.kt
│   │   ├── CheckoutFragment.kt
│   │   ├── ClaimFragment.kt
│   │   ├── ForgotPasswordFragment.kt
│   │   ├── LoginFragment.kt
│   │   ├── MapPickerFragment.kt
│   │   ├── OrderDetailsFragment.kt
│   │   ├── OrderListFragment.kt
│   │   ├── OTPVerificationFragment.kt
│   │   ├── ProductDetailFragment.kt
│   │   ├── ProductListFragment.kt
│   │   ├── ProfileFragment.kt
│   │   └── RegisterFragment.kt
│   └── viewmodel/
│       ├── AuthViewModel.kt
│       ├── CartViewModel.kt
│       ├── CheckoutViewModel.kt
│       ├── ClaimViewModel.kt
│       ├── OrderViewModel.kt
│       ├── ProductViewModel.kt
│       ├── ProfileViewModel.kt
│       └── ViewModelFactory.kt
└── util/
    ├── Constants.kt               # BASE_URL, STRIPE_KEY
    ├── GoogleSignInHelper.kt      # Google OAuth
    ├── NetworkErrorHandler.kt
    ├── SingleOrListDeserializer.kt
    ├── StripePaymentHelper.kt     # Stripe SDK
    ├── TokenManager.kt            # JWT token storage
    └── ValidationUtils.kt         # Input validation
```

### Backend API Endpoints
| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/auth/register` | POST | No | Register new user |
| `/api/auth/verify-email` | POST | No | Verify OTP code |
| `/api/auth/login` | POST | No | Login with email/password |
| `/api/auth/google-signin` | POST | No | Login with Google ID token |
| `/api/auth/forgot-password` | POST | No | Request password reset |
| `/api/auth/reset-password` | POST | No | Reset password with OTP |
| `/api/user/profile` | GET | Yes | Get user profile |
| `/api/user/profile` | PUT | Yes | Update user profile |
| `/api/user/change-password` | PUT | Yes | Change password |
| `/api/products` | GET | No | Get all products |
| `/api/products/{id}` | GET | No | Get product by ID |
| `/api/products/search?query=` | GET | No | Search products |
| `/api/products/sort?by=` | GET | No | Sort products |
| `/api/products/category/{cat}` | GET | No | Filter by category |
| `/api/cart` | GET | Yes | Get cart items |
| `/api/cart/add` | POST | Yes | Add to cart |
| `/api/cart/update` | PUT | Yes | Update cart quantity |
| `/api/cart/remove/{id}` | DELETE | Yes | Remove from cart |
| `/api/cart/clear` | DELETE | Yes | Clear cart |
| `/api/payment/create-intent` | POST | Yes | Create Stripe payment intent |
| `/api/payment/verify/{id}` | GET | Yes | Verify payment status |
| `/api/orders` | GET | Yes | Get user orders |
| `/api/orders` | POST | Yes | Create order |
| `/api/orders/{id}/cancel` | POST | Yes | Cancel order |
| `/api/claims` | GET | Yes | Get user claims |
| `/api/claims` | POST | Yes | Create claim |
| `/api/claims/order/{id}` | GET | Yes | Get claims by order |

---

## 🧪 COMPREHENSIVE TEST FLOW

### Prerequisites
- [ ] Backend server running at `http://localhost:8080`
- [ ] Android emulator or physical device ready
- [ ] Internet connection available
- [ ] Test email account for receiving OTP codes

---

## 📱 TEST SECTION 1: APP LAUNCH & INITIAL STATE

### Test 1.1: Fresh Install Launch
**Steps:**
1. Uninstall any existing version of the app
2. Install fresh build
3. Launch the app

**Expected Results:**
- [ ] App launches without crash
- [ ] Login screen is displayed
- [ ] Bottom navigation is HIDDEN
- [ ] No data is present (fresh install)

### Test 1.2: App with Saved Token
**Steps:**
1. Login successfully (from Test 2.4)
2. Force close the app
3. Reopen the app

**Expected Results:**
- [ ] App opens directly to Products screen
- [ ] Bottom navigation is VISIBLE
- [ ] User remains logged in
- [ ] Cart data persists

---

## 🔐 TEST SECTION 2: AUTHENTICATION

### Test 2.1: Registration - Validation Errors
**Screen:** Register Fragment

| Test | Input | Expected Error |
|------|-------|----------------|
| 2.1.1 | First Name: empty | "First name is required" |
| 2.1.2 | First Name: "A" | "First name must be at least 2 characters" |
| 2.1.3 | First Name: "John123" | "First name can only contain letters" |
| 2.1.4 | Last Name: empty | "Last name is required" |
| 2.1.5 | Last Name: "B" | "Last name must be at least 2 characters" |
| 2.1.6 | Email: empty | "Email is required" |
| 2.1.7 | Email: "invalid" | "Invalid email format" |
| 2.1.8 | Email: "test@" | "Invalid email format" |
| 2.1.9 | Password: empty | "Password is required" |
| 2.1.10 | Password: "12345" | "Password must be at least 6 characters" |
| 2.1.11 | Confirm: empty | "Please confirm your password" |
| 2.1.12 | Confirm: mismatch | "Passwords do not match" |

### Test 2.2: Registration - Successful
**Steps:**
1. Navigate to Register screen
2. Enter valid data:
   - First Name: `John`
   - Last Name: `Doe`
   - Email: `your.real.email@example.com`
   - Password: `Test123`
   - Confirm Password: `Test123`
3. Tap "Create Account"

**Expected Results:**
- [ ] Button shows "Creating account..."
- [ ] Success message: "Account created! Check your email for OTP."
- [ ] Navigate to OTP Verification screen
- [ ] Email with 6-digit OTP received

### Test 2.3: OTP Verification
**Screen:** OTP Verification Fragment

| Test | Input | Expected Result |
|------|-------|-----------------|
| 2.3.1 | Empty code | "OTP code is required" |
| 2.3.2 | "abc123" | "OTP code must contain only digits" |
| 2.3.3 | "12345" | "OTP code must be 6 digits" |
| 2.3.4 | Wrong 6-digit code | "Verification failed" error |
| 2.3.5 | Correct code from email | Success, navigate to Login |

### Test 2.4: Login - Validation & Success
**Screen:** Login Fragment

| Test | Input | Expected Result |
|------|-------|-----------------|
| 2.4.1 | Email empty | "Email is required" |
| 2.4.2 | Invalid email format | "Invalid email format" |
| 2.4.3 | Password empty | "Password is required" |
| 2.4.4 | Password < 6 chars | "Password must be at least 6 characters" |
| 2.4.5 | Wrong password | "Login failed: 401" |
| 2.4.6 | Unverified account | "Login failed: 400" |
| 2.4.7 | Correct credentials | Success, navigate to Products |

**Successful Login Expected:**
- [ ] Button shows "Signing in..."
- [ ] "Welcome back!" snackbar
- [ ] Navigate to Products screen
- [ ] Bottom navigation VISIBLE

### Test 2.5: Google Sign-In (If Configured)
**Prerequisites:** Web Client ID configured in GoogleSignInHelper.kt

**Steps:**
1. On Login screen, tap "Continue with Google"
2. Select Google account
3. Grant permissions if prompted

**Expected Results:**
- [ ] Google account picker appears
- [ ] Button shows "Signing in..." then "Authenticating..."
- [ ] On success: Navigate to Products
- [ ] On failure: Error message displayed

### Test 2.6: Forgot Password Flow
**Screen:** Forgot Password Fragment

**Steps:**
1. From Login, tap "Forgot Password?"
2. Enter registered email
3. Tap "Send Reset Code"
4. Check email for OTP
5. Enter OTP code
6. Enter new password

| Test | Input | Expected Result |
|------|-------|-----------------|
| 2.6.1 | Empty email | "Email is required" |
| 2.6.2 | Invalid email | "Invalid email format" |
| 2.6.3 | Valid email | "Reset code sent to your email!" |
| 2.6.4 | OTP: "12345" | "Code must be 6 digits" |
| 2.6.5 | OTP: "abcdef" | "Code must contain only digits" |
| 2.6.6 | Password: "abc" | "Password must be at least 6 characters" |
| 2.6.7 | Password: "abcdef" | "Password must contain at least one number" |
| 2.6.8 | Password: "123456" | "Password must contain at least one letter" |
| 2.6.9 | Password: "Test123" | Success, navigate to Login |

### Test 2.7: Logout
**Screen:** Profile Fragment

**Steps:**
1. Navigate to Profile tab
2. Tap "Logout" button
3. Confirm in dialog

**Expected Results:**
- [ ] Confirmation dialog appears
- [ ] On confirm: "Logged out successfully" snackbar
- [ ] Navigate to Login screen (NOT Products)
- [ ] Bottom navigation HIDDEN
- [ ] Token cleared
- [ ] Local data cleared

---

## 🛍️ TEST SECTION 3: PRODUCTS

### Test 3.1: Product List Loading
**Screen:** Product List Fragment

**Steps:**
1. Login successfully
2. Observe Products screen

**Expected Results:**
- [ ] Products load automatically
- [ ] Each product shows: image, name, price, rating stars, category badge
- [ ] Pull-to-refresh works

### Test 3.2: Product Search
**Steps:**
1. Tap search icon in toolbar
2. Enter search query

| Test | Query | Expected Result |
|------|-------|-----------------|
| 3.2.1 | "Brake" | Shows products with "Brake" in name/description |
| 3.2.2 | "xyz123" | Empty list or "No products found" |
| 3.2.3 | Clear search | All products shown |

### Test 3.3: Product Sorting
**Steps:**
1. Tap sort/filter button
2. Select sort option

| Test | Sort Option | Expected Result |
|------|-------------|-----------------|
| 3.3.1 | Price: Low to High | Products ordered by price ascending |
| 3.3.2 | Price: High to Low | Products ordered by price descending |
| 3.3.3 | Rating | Products ordered by rating descending |
| 3.3.4 | Name A-Z | Products ordered alphabetically |

### Test 3.4: Product Category Filter
**Steps:**
1. Tap category filter
2. Select category

| Test | Category | Expected Result |
|------|----------|-----------------|
| 3.4.1 | "Brakes" | Only brake products shown |
| 3.4.2 | "Engine" | Only engine products shown |
| 3.4.3 | "All" | All products shown |

### Test 3.5: Product Details
**Steps:**
1. Tap on any product in the list

**Expected Results:**
- [ ] Navigate to Product Detail screen
- [ ] Large product image displayed
- [ ] Product name shown
- [ ] Price displayed correctly (e.g., "$49.99")
- [ ] Full description shown
- [ ] Stock quantity shown
- [ ] Rating displayed
- [ ] "Add to Cart" button visible

### Test 3.6: Add to Cart from Details
**Steps:**
1. On Product Detail screen
2. Tap "Add to Cart"

**Expected Results:**
- [ ] Success message shown
- [ ] Can navigate to Cart tab and see item

---

## 🛒 TEST SECTION 4: SHOPPING CART

### Test 4.1: Add to Cart
**Screen:** Product List/Detail

| Test | Action | Expected Result |
|------|--------|-----------------|
| 4.1.1 | Add product (not in cart) | "Added to cart" message |
| 4.1.2 | Add same product again | Quantity increments |
| 4.1.3 | Add different product | Both products in cart |

### Test 4.2: View Cart
**Screen:** Cart Fragment

**Expected Display:**
- [ ] Product image
- [ ] Product name
- [ ] Unit price
- [ ] Quantity with +/- buttons
- [ ] Item subtotal (price × quantity)
- [ ] Cart total at bottom
- [ ] "Checkout" button

### Test 4.3: Update Cart Quantity
**Steps:**
1. Navigate to Cart tab
2. Use +/- buttons

| Test | Action | Expected Result |
|------|--------|-----------------|
| 4.3.1 | Tap "+" | Quantity increases by 1 |
| 4.3.2 | Tap "-" | Quantity decreases by 1 |
| 4.3.3 | Set quantity to 0 | Item removed from cart |
| 4.3.4 | Multiple updates | Total recalculates correctly |

### Test 4.4: Remove Cart Item
**Steps:**
1. Swipe left on cart item (or tap remove button)

**Expected Results:**
- [ ] Item removed from cart
- [ ] Total updated
- [ ] If last item: "Cart is empty" message

### Test 4.5: Clear Cart
**Steps:**
1. Tap "Clear Cart" button
2. Confirm action

**Expected Results:**
- [ ] All items removed
- [ ] Cart shows empty state
- [ ] "Checkout" button disabled or hidden

### Test 4.6: Cart Persistence
**Steps:**
1. Add items to cart
2. Close app completely
3. Reopen app

**Expected Results:**
- [ ] Cart items still present
- [ ] Quantities preserved
- [ ] Totals correct

---

## 💳 TEST SECTION 5: CHECKOUT & PAYMENT

### Test 5.1: Navigate to Checkout
**Steps:**
1. Add items to cart
2. Tap "Checkout" button

**Expected Results:**
- [ ] Navigate to Checkout screen
- [ ] Order summary shown
- [ ] Delivery form visible
- [ ] Payment options visible

### Test 5.2: Delivery Information Validation
**Screen:** Checkout Fragment

| Test | Field | Input | Expected Error |
|------|-------|-------|----------------|
| 5.2.1 | Address | empty | "Address is required" |
| 5.2.2 | Address | "abc" | "Please enter a valid address" |
| 5.2.3 | Phone | empty | "Phone number is required" |
| 5.2.4 | Phone | "123" | "Phone must have at least 8 digits" |
| 5.2.5 | Phone | "abc123" | "Phone can only contain digits..." |
| 5.2.6 | Latitude | empty | "Latitude is required" |
| 5.2.7 | Latitude | "abc" | "Invalid number" |
| 5.2.8 | Latitude | "100" | "Latitude must be between -90 and 90" |
| 5.2.9 | Longitude | empty | "Longitude is required" |
| 5.2.10 | Longitude | "200" | "Longitude must be between -180 and 180" |

### Test 5.3: Map Location Picker
**Steps:**
1. Tap "Select on Map" button
2. Map opens (OpenStreetMap)
3. Tap on map to select location
4. Tap confirm button

**Expected Results:**
- [ ] Map displays correctly
- [ ] Can pan and zoom
- [ ] Tap places marker
- [ ] On confirm: Latitude field auto-populated
- [ ] On confirm: Longitude field auto-populated

### Test 5.4: Cash on Delivery Order
**Steps:**
1. Fill valid delivery info:
   - Address: `123 Main Street, City`
   - Phone: `+1234567890`
   - Lat/Lng: Select on map or enter manually
   - Notes: `Leave at door` (optional)
2. Select "Cash on Delivery"
3. Tap "Place Order"

**Expected Results:**
- [ ] Button shows "Placing Order..."
- [ ] Order created successfully
- [ ] Navigate to order confirmation or Orders screen
- [ ] Cart cleared
- [ ] Order visible in Orders tab

### Test 5.5: Card Payment (Stripe)
**Steps:**
1. Fill valid delivery info
2. Select "Pay with Card"
3. Tap "Place Order"
4. Stripe payment sheet appears
5. Enter test card: `4242 4242 4242 4242`
   - Expiry: Any future date (e.g., `12/26`)
   - CVC: Any 3 digits (e.g., `123`)
   - ZIP: Any 5 digits (e.g., `12345`)
6. Complete payment

**Expected Results:**
- [ ] Payment intent created
- [ ] Stripe payment sheet displayed
- [ ] Card validation works
- [ ] On success: Order created
- [ ] Payment status: "requires_payment_method" → "succeeded"
- [ ] Cart cleared
- [ ] Order shows payment intent ID

### Test 5.6: Empty Cart Checkout (Error)
**Steps:**
1. Clear cart
2. Try to access checkout

**Expected Results:**
- [ ] Cannot proceed to checkout
- [ ] Error message or checkout button disabled

---

## 📦 TEST SECTION 6: ORDERS

### Test 6.1: View Orders List
**Screen:** Order List Fragment

**Steps:**
1. Navigate to Orders tab

**Expected Results:**
- [ ] Only YOUR orders displayed (user-specific)
- [ ] Orders sorted by date (newest first)
- [ ] Each order shows: ID, date, status badge, total
- [ ] Status colors: PENDING (yellow), COMPLETED (green), CANCELLED (red)

### Test 6.2: Order Details
**Steps:**
1. Tap on any order in the list

**Expected Results:**
- [ ] Navigate to Order Details screen
- [ ] Order ID displayed
- [ ] Status badge with correct color
- [ ] Date formatted nicely
- [ ] Total amount shown
- [ ] **Delivery Information:**
  - [ ] Address displayed
  - [ ] GPS coordinates shown
  - [ ] Contact phone shown
  - [ ] Delivery notes shown (if any)
- [ ] **Payment Information:**
  - [ ] Payment method (Cash/Card)
  - [ ] Transaction ID (for card payments)
- [ ] **Order Items:**
  - [ ] Product names shown
  - [ ] Quantities shown
  - [ ] Prices shown

### Test 6.3: Cancel Pending Order
**Steps:**
1. Find an order with status "PENDING"
2. Tap "Cancel Order" button
3. Confirm cancellation

**Expected Results:**
- [ ] Button shows "Cancelling..."
- [ ] Order status changes to "CANCELLED"
- [ ] "Order cancelled successfully" message
- [ ] Return to orders list

### Test 6.4: Cancel Completed Order (Error)
**Steps:**
1. Find an order with status "COMPLETED"
2. Try to cancel

**Expected Results:**
- [ ] Cancel button NOT shown for completed orders
- [ ] OR error message if attempted

### Test 6.5: Multi-User Order Isolation
**Steps:**
1. Login as User A
2. Create some orders
3. Note order IDs
4. Logout
5. Login as User B (or create new account)
6. View Orders tab

**Expected Results:**
- [ ] User B does NOT see User A's orders
- [ ] Only User B's orders displayed (or empty if new user)

---

## 📝 TEST SECTION 7: CLAIMS

### Test 7.1: Submit Claim - Validation
**Screen:** Claim Fragment

| Test | Field | Input | Expected Error |
|------|-------|-------|----------------|
| 7.1.1 | Order ID | empty | "Order ID is required" |
| 7.1.2 | Order ID | "0" | "Please enter a valid order ID" |
| 7.1.3 | Order ID | "-1" | "Please enter a valid order ID" |
| 7.1.4 | Subject | empty | "Subject is required" |
| 7.1.5 | Subject | "abc" | "Subject must be at least 5 characters" |
| 7.1.6 | Description | empty | "Description is required" |
| 7.1.7 | Description | "short" | "Please provide more details (at least 20 characters)" |

### Test 7.2: Submit Claim - Success
**Steps:**
1. Navigate to Claims tab
2. Enter valid data:
   - Order ID: A valid order ID from your orders
   - Subject: `Defective Product Received`
   - Description: `The brake pads I received appear to be cracked and damaged. Please help resolve this issue.`
3. Tap "Submit Claim"

**Expected Results:**
- [ ] Button shows "Submitting..."
- [ ] "Claim submitted successfully" message
- [ ] Form fields cleared
- [ ] Claim appears in claims list below

### Test 7.3: View Claims List
**Expected Results:**
- [ ] All user's claims displayed
- [ ] Each claim shows: ID, subject, status
- [ ] Status shown (PENDING, RESOLVED, etc.)

---

## 👤 TEST SECTION 8: PROFILE

### Test 8.1: View Profile
**Screen:** Profile Fragment

**Steps:**
1. Navigate to Profile tab

**Expected Results:**
- [ ] Profile loads successfully
- [ ] Avatar/initials displayed
- [ ] Full name shown
- [ ] Email displayed
- [ ] Verified badge (if verified)
- [ ] Phone number (if set)
- [ ] Member since date

### Test 8.2: Update Profile - Validation
| Test | Field | Input | Expected Error |
|------|-------|-------|----------------|
| 8.2.1 | First Name | empty | "First name is required" |
| 8.2.2 | First Name | "A" | "First name must be at least 2 characters" |
| 8.2.3 | First Name | "John123" | "First name can only contain letters" |
| 8.2.4 | Last Name | empty | "Last name is required" |
| 8.2.5 | Phone | "123" | "Phone must have at least 8 digits" |

### Test 8.3: Update Profile - Success
**Steps:**
1. Update fields:
   - First Name: `Jonathan`
   - Last Name: `Smith`
   - Phone: `+1987654321`
2. Tap "Update Profile"

**Expected Results:**
- [ ] Button shows "Updating..."
- [ ] "Profile updated successfully!" message
- [ ] Fields show updated values

### Test 8.4: Change Password - Validation
**Steps:**
1. Tap "Change Password"
2. Dialog appears

| Test | Input | Expected Error |
|------|-------|----------------|
| 8.4.1 | Current: empty | Error message |
| 8.4.2 | Current: wrong password | "Current password is incorrect" |
| 8.4.3 | New: "abc" | "Password must be at least 6 characters" |
| 8.4.4 | New: "abcdef" | "Must contain at least one number" |
| 8.4.5 | Confirm: mismatch | "Passwords don't match" |

### Test 8.5: Change Password - Success
**Steps:**
1. Enter current password correctly
2. Enter new password: `NewPass123`
3. Confirm new password: `NewPass123`
4. Tap "Change"

**Expected Results:**
- [ ] "Password changed successfully!" message
- [ ] Dialog closes
- [ ] Can logout and login with new password

---

## 🔄 TEST SECTION 9: DATA PERSISTENCE & OFFLINE

### Test 9.1: Token Persistence
**Steps:**
1. Login successfully
2. Force close app
3. Reopen app

**Expected:** Auto-login, Products screen shown

### Test 9.2: Cart Persistence
**Steps:**
1. Add items to cart
2. Force close app
3. Reopen app
4. Check cart

**Expected:** Cart items preserved with correct quantities

### Test 9.3: Products Cache
**Steps:**
1. Load products while online
2. Turn off network
3. Navigate away and back to Products

**Expected:** Cached products still shown

### Test 9.4: Network Error Handling
**Steps:**
1. Turn off network/WiFi
2. Try to login

**Expected:** Network error message, not crash

---

## ⚠️ TEST SECTION 10: ERROR HANDLING & EDGE CASES

### Test 10.1: Invalid Token
**Steps:**
1. Login successfully
2. Clear app data (Settings → Apps → CarParts → Clear Data)
3. Or manually invalidate token on backend
4. Try to access protected feature

**Expected:** Redirect to login screen

### Test 10.2: Backend Down
**Steps:**
1. Stop backend server
2. Try to login/load products

**Expected:** Connection error message, app doesn't crash

### Test 10.3: Session Timeout
**Steps:**
1. Login
2. Wait for token to expire (or manually expire it)
3. Try to access cart/orders

**Expected:** Redirect to login or refresh token

### Test 10.4: Concurrent Users
**Steps:**
1. Login on Device A
2. Login on Device B with same account
3. Perform actions on both

**Expected:** Both sessions work (or proper session management)

---

## 📊 TEST CHECKLIST SUMMARY

### Authentication (10 tests)
- [ ] 2.1 Registration validation errors
- [ ] 2.2 Registration success
- [ ] 2.3 OTP verification
- [ ] 2.4 Login validation & success
- [ ] 2.5 Google Sign-In
- [ ] 2.6 Forgot password flow
- [ ] 2.7 Logout → Goes to Login screen

### Products (6 tests)
- [ ] 3.1 Product list loading
- [ ] 3.2 Product search
- [ ] 3.3 Product sorting
- [ ] 3.4 Category filtering
- [ ] 3.5 Product details view
- [ ] 3.6 Add to cart from details

### Cart (6 tests)
- [ ] 4.1 Add to cart
- [ ] 4.2 View cart
- [ ] 4.3 Update quantity
- [ ] 4.4 Remove item
- [ ] 4.5 Clear cart
- [ ] 4.6 Cart persistence

### Checkout & Payment (6 tests)
- [ ] 5.1 Navigate to checkout
- [ ] 5.2 Delivery validation
- [ ] 5.3 Map location picker
- [ ] 5.4 Cash on delivery order
- [ ] 5.5 Card payment (Stripe)
- [ ] 5.6 Empty cart checkout error

### Orders (5 tests)
- [ ] 6.1 View orders list
- [ ] 6.2 Order details with items
- [ ] 6.3 Cancel pending order
- [ ] 6.4 Cannot cancel completed order
- [ ] 6.5 Multi-user order isolation

### Claims (3 tests)
- [ ] 7.1 Claim validation
- [ ] 7.2 Submit claim success
- [ ] 7.3 View claims list

### Profile (5 tests)
- [ ] 8.1 View profile
- [ ] 8.2 Update profile validation
- [ ] 8.3 Update profile success
- [ ] 8.4 Change password validation
- [ ] 8.5 Change password success

### Data & Error Handling (4 tests)
- [ ] 9.1 Token persistence
- [ ] 9.2 Cart persistence
- [ ] 9.3 Products cache
- [ ] 9.4 Network error handling

---

## 🔧 CONFIGURATION REFERENCE

### Constants.kt
```kotlin
BASE_URL = "http://10.0.2.2:8080/"  // Emulator
STRIPE_PUBLISHABLE_KEY = "pk_test_..."
```

### GoogleSignInHelper.kt
```kotlin
WEB_CLIENT_ID = "242829987510-pp4mi6fnq5mtos5utdrt0m99dvm1v66r.apps.googleusercontent.com"
```

### Stripe Test Cards
| Card Number | Description |
|-------------|-------------|
| 4242 4242 4242 4242 | Success |
| 4000 0000 0000 0002 | Declined |
| 4000 0000 0000 9995 | Insufficient funds |

---

## 📱 DEVICE TESTING MATRIX

| Device Type | API Level | Test Status |
|-------------|-----------|-------------|
| Emulator | API 26 | [ ] |
| Emulator | API 30 | [ ] |
| Emulator | API 34 | [ ] |
| Physical | Various | [ ] |

---

**Total Tests: 45+**
**Last Updated: January 6, 2026**

