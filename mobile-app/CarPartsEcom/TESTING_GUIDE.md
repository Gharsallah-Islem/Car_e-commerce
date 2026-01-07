# 🧪 Car Parts E-Commerce App - Complete Testing Flow

## Prerequisites
- Android device/emulator running
- Backend server running at `http://localhost:8080` (or your configured URL)
- Internet connection for API calls

---

## 🔐 1. AUTHENTICATION TESTS

### 1.1 Registration Flow
1. **Open app** → Should see Login screen
2. **Tap "Create Account"** → Navigate to Register screen
3. **Test validation:**
   - Leave all fields empty → Tap "Create Account" → Should show errors
   - Enter invalid email (e.g., "test@") → Should show "Invalid email format"
   - Enter short password (< 6 chars) → Should show error
   - Enter mismatched passwords → Should show "Passwords do not match"
4. **Valid registration:**
   - First Name: `John`
   - Last Name: `Doe`
   - Email: `your.email@example.com` (use real email for OTP)
   - Password: `Test123`
   - Confirm Password: `Test123`
   - Tap "Create Account" → Should navigate to OTP screen
5. **Check email** for 6-digit OTP code

### 1.2 OTP Verification
1. **On OTP screen**, enter the 6-digit code from email
2. **Test validation:**
   - Enter letters → Should show "OTP code must contain only digits"
   - Enter 5 digits → Should show "OTP code must be 6 digits"
3. **Enter correct OTP** → Should show success, navigate to Login

### 1.3 Login Flow
1. **On Login screen:**
   - Enter registered email
   - Enter password
   - Tap "Sign In" → Should navigate to Products screen
2. **Test validation:**
   - Wrong email format → Error shown
   - Wrong password → "Login failed" message
   - Unverified account → Should show error

### 1.4 Google Sign-In (Requires Setup)
> **Note:** To test Google Sign-In, you need to:
> 1. Create project in [Google Cloud Console](https://console.cloud.google.com)
> 2. Enable Google Sign-In API
> 3. Create OAuth 2.0 credentials (Web Application type)
> 4. Copy the Web Client ID
> 5. Replace `YOUR_WEB_CLIENT_ID` in `GoogleSignInHelper.kt`
> 6. Configure your backend to verify Google tokens

1. Tap "Continue with Google"
2. Select Google account
3. Should authenticate and navigate to Products

### 1.5 Forgot Password
1. **From Login**, tap "Forgot Password?"
2. Enter registered email → Tap "Send Reset Code"
3. Check email for OTP
4. Enter OTP and new password
5. **Password validation:**
   - Must be 6+ characters
   - Must contain at least one letter
   - Must contain at least one number
6. Tap "Reset Password" → Should navigate to Login

### 1.6 Logout
1. Navigate to **Profile** tab
2. Tap "Logout"
3. Confirm in dialog
4. **✅ EXPECTED:** Should navigate to Login screen (not Products)

---

## 🛍️ 2. PRODUCT TESTS

### 2.1 Browse Products
1. After login, should see Products list
2. **Pull down** to refresh
3. Products should load with:
   - Image
   - Name
   - Price
   - Rating stars
   - Category badge

### 2.2 Search Products
1. Tap search icon
2. Enter "Brake" → Should filter to brake products
3. Clear search → All products shown

### 2.3 Sort Products
1. Tap sort/filter button
2. Test each sort option:
   - Price: Low to High
   - Price: High to Low
   - Rating
   - Name A-Z

### 2.4 Filter by Category
1. Tap category filter
2. Select "Brakes" → Only brake products shown
3. Select "All" → All products shown

### 2.5 Product Details
1. Tap on a product
2. Should see:
   - Large image
   - Full description
   - Price
   - Stock quantity
   - Add to Cart button

---

## 🛒 3. CART TESTS

### 3.1 Add to Cart
1. From Products, tap "Add to Cart" on a product
2. Should show confirmation
3. Tap same product again → Quantity should increment

### 3.2 View Cart
1. Navigate to **Cart** tab
2. Should see:
   - Product image
   - Product name
   - Price per item
   - Quantity controls (+/-)
   - Subtotal per item
   - Total at bottom

### 3.3 Update Quantity
1. Tap "+" → Quantity increases, total updates
2. Tap "-" → Quantity decreases
3. Set quantity to 0 → Item removed from cart

### 3.4 Remove Item
1. Swipe left on item (or tap remove)
2. Item should be removed
3. Total should update

### 3.5 Clear Cart
1. Tap "Clear Cart" button
2. Confirm → Cart should be empty

---

## 💳 4. CHECKOUT & PAYMENT TESTS

### 4.1 Proceed to Checkout
1. Add items to cart
2. Tap "Checkout" button
3. Should navigate to Checkout screen

### 4.2 Delivery Information Validation
1. **Address validation:**
   - Empty → "Address is required"
   - Less than 5 chars → "Please enter a valid address"
2. **Phone validation:**
   - Empty → "Phone number is required"
   - Less than 8 digits → Error
   - Invalid characters → Error
   - Valid: `+1234567890` or `123-456-7890`
3. **Location validation:**
   - Empty lat/lng → Errors shown
   - Invalid numbers → "Invalid number"
   - Lat outside -90 to 90 → Error
   - Lng outside -180 to 180 → Error

### 4.3 Select Location on Map
1. Tap "Select on Map" button
2. **Map should open** (OpenStreetMap)
3. Tap on map to select location
4. Tap confirm
5. **✅ EXPECTED:** Lat/Lng fields should auto-populate

### 4.4 Cash on Delivery
1. Select "Cash on Delivery" payment option
2. Fill in valid delivery info
3. Tap "Place Order"
4. **✅ EXPECTED:** Order created, navigate to order confirmation

### 4.5 Card Payment (Stripe)
1. Select "Pay with Card" option
2. Fill in valid delivery info
3. Tap "Place Order"
4. **Stripe payment sheet** should appear
5. Enter test card: `4242 4242 4242 4242`
   - Any future expiry (e.g., `12/26`)
   - Any CVC (e.g., `123`)
   - Any ZIP (e.g., `12345`)
6. Complete payment
7. **✅ EXPECTED:** Order created with payment confirmed

---

## 📦 5. ORDER TESTS

### 5.1 View Orders
1. Navigate to **Orders** tab
2. Should see list of YOUR orders only (not other users')
3. Each order shows:
   - Order ID
   - Date
   - Status badge
   - Total amount

### 5.2 Order Details
1. Tap on an order
2. Should see:
   - Order ID and status
   - Date placed
   - Total amount
   - **Delivery Information:**
     - Address
     - GPS coordinates
     - Contact phone
     - Delivery notes
   - **Payment Information:**
     - Payment method (Cash/Card)
     - Transaction ID (for card payments)
   - **Order Items:**
     - Product names
     - Quantities
     - Prices

### 5.3 Cancel Order
1. Find a **PENDING** order
2. Tap "Cancel Order"
3. Order status should change to CANCELLED
4. **Note:** Cannot cancel COMPLETED orders

---

## 📝 6. CLAIMS TESTS

### 6.1 Submit Claim
1. Navigate to **Claims** tab
2. Fill in:
   - Order ID: Valid order number
   - Subject: At least 5 characters
   - Description: At least 20 characters
3. **Test validation:**
   - Invalid Order ID (0 or negative) → Error
   - Short subject → Error
   - Short description → Error
4. Submit valid claim → Should show success

### 6.2 View Claims
1. Submitted claims appear in list
2. Shows: Claim ID, Subject, Status

---

## 👤 7. PROFILE TESTS

### 7.1 View Profile
1. Navigate to **Profile** tab
2. Should see:
   - Profile avatar
   - Name
   - Email
   - Verified badge
   - Member since date

### 7.2 Update Profile
1. **Edit fields:**
   - First Name
   - Last Name
   - Phone Number
2. **Validation:**
   - Names: Min 2 chars, letters only
   - Phone: 8-15 digits, valid format
3. Tap "Update Profile" → Should show success

### 7.3 Change Password
1. Tap "Change Password"
2. Enter current password
3. Enter new password (6+ chars, letter + number)
4. Confirm new password
5. Tap "Change" → Should show success
6. **Test:** Logout and login with new password

---

## 🔄 8. DATA PERSISTENCE TESTS

### 8.1 App Restart
1. Add items to cart
2. Close app completely
3. Reopen app
4. **✅ EXPECTED:** Cart items preserved

### 8.2 Login Persistence
1. Login successfully
2. Close app
3. Reopen app
4. **✅ EXPECTED:** Still logged in (Products screen shown)

### 8.3 Multi-User Data Isolation
1. Login as User A
2. Create orders
3. Logout → **Should go to Login screen**
4. Login as User B
5. **✅ EXPECTED:** User B sees only their orders, not User A's

---

## ⚠️ 9. ERROR HANDLING TESTS

### 9.1 Network Error
1. Turn off internet/WiFi
2. Try to login → Should show network error
3. Try to load products → Should show error with retry button

### 9.2 Invalid Token
1. Login successfully
2. Manually clear app data
3. Try to access protected feature
4. **✅ EXPECTED:** Redirect to login

### 9.3 Server Error
1. Stop backend server
2. Try operations
3. Should show appropriate error messages

---

## 📊 10. VALIDATION SUMMARY

| Screen | Field | Validation |
|--------|-------|------------|
| Register | First Name | Required, 2+ chars, letters only |
| Register | Last Name | Required, 2+ chars, letters only |
| Register | Email | Required, valid email format |
| Register | Password | Required, 6+ chars |
| Register | Confirm Password | Must match password |
| Login | Email | Required, valid format |
| Login | Password | Required, 6+ chars |
| OTP | Code | Required, exactly 6 digits |
| Forgot Password | New Password | 6+ chars, letter + number required |
| Checkout | Address | Required, 5+ chars |
| Checkout | Phone | Required, 8-15 digits |
| Checkout | Latitude | Required, -90 to 90 |
| Checkout | Longitude | Required, -180 to 180 |
| Profile | First Name | Required, 2+ chars, letters only |
| Profile | Last Name | Required, 2+ chars, letters only |
| Profile | Phone | Optional, but if provided: 8-15 digits |
| Claim | Order ID | Required, positive number |
| Claim | Subject | Required, 5-100 chars |
| Claim | Description | Required, 20-1000 chars |

---

## ✅ TEST CHECKLIST

- [ ] Registration with valid data
- [ ] Registration validation errors
- [ ] OTP verification
- [ ] Login with valid credentials
- [ ] Login validation errors
- [ ] Google Sign-In (if configured)
- [ ] Forgot password flow
- [ ] Browse products
- [ ] Search products
- [ ] Sort products
- [ ] Filter by category
- [ ] Add to cart
- [ ] Update cart quantity
- [ ] Remove from cart
- [ ] Clear cart
- [ ] Checkout with cash
- [ ] Checkout with card (Stripe)
- [ ] Select location on map
- [ ] View orders (user-specific)
- [ ] View order details with items
- [ ] Cancel pending order
- [ ] Submit claim
- [ ] View claims
- [ ] Update profile
- [ ] Change password
- [ ] Logout → Goes to Login screen
- [ ] Data persistence after restart
- [ ] Network error handling

---

## 🔧 Google Sign-In Setup (For Developers)

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Create a new project or select existing
3. Go to **APIs & Services** → **Credentials**
4. Click **Create Credentials** → **OAuth client ID**
5. Select **Web application** type
6. Add authorized origins (your backend URL)
7. Copy the **Client ID**
8. In Android app, update `GoogleSignInHelper.kt`:
   ```kotlin
   const val WEB_CLIENT_ID = "your-client-id.apps.googleusercontent.com"
   ```
9. In backend, configure Google OAuth verification with the same Client ID
10. Rebuild the app

---

**Happy Testing! 🚗🔧**

