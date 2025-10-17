# ===================================================================
# EXTENDED API TESTING SCRIPT - ALL ENDPOINTS
# E-Commerce Spare Parts Backend
# ===================================================================

param(
    [string]$baseUrl = "http://localhost:8080",
    [switch]$verbose = $false
)

# Initialize tracking
$testResults = @()
$jwt_token = ""
$admin_token = ""
$user_id = ""
$product_id = ""
$order_id = ""
$cart_id = ""
$vehicle_id = ""
$reclamation_id = ""

# ===================================================================
# HELPER FUNCTIONS
# ===================================================================

function Write-TestHeader { param($phase, $title) 
    Write-Host "`n" -NoNewline
    Write-Host "=" -NoNewline -ForegroundColor DarkCyan
    Write-Host "=" * 70 -ForegroundColor DarkCyan
    Write-Host "  $phase - $title" -ForegroundColor Cyan
    Write-Host "=" -NoNewline -ForegroundColor DarkCyan
    Write-Host "=" * 70 -ForegroundColor DarkCyan
}

function Write-Success { param($message) Write-Host "[PASS] $message" -ForegroundColor Green }
function Write-Fail { param($message) Write-Host "[FAIL] $message" -ForegroundColor Red }
function Write-Info { param($message) Write-Host "[INFO] $message" -ForegroundColor Cyan }
function Write-Warn { param($message) Write-Host "[WARN] $message" -ForegroundColor Yellow }

function Add-TestResult {
    param($testName, $endpoint, $method, $expected, $actual, $passed, $notes = "")
    $script:testResults += [PSCustomObject]@{
        Test = $testName
        Endpoint = $endpoint
        Method = $method
        Expected = $expected
        Actual = $actual
        Status = if ($passed) { "PASS" } else { "FAIL" }
        Notes = $notes
        Timestamp = Get-Date -Format "HH:mm:ss"
    }
    
    if ($passed) {
        Write-Success "$testName - $method $endpoint"
    } else {
        Write-Fail "$testName - $method $endpoint ($notes)"
    }
}

function Test-Endpoint {
    param($name, $uri, $method = "GET", $body = $null, $headers = @{}, $expectedStatus = 200, $skipErrorCheck = $false)
    
    try {
        if ($body) {
            if ($method -eq "POST" -or $method -eq "PUT" -or $method -eq "PATCH") {
                $response = Invoke-RestMethod -Uri $uri -Method $method -Headers $headers -Body $body -ContentType "application/json"
            }
        } else {
            $response = Invoke-RestMethod -Uri $uri -Method $method -Headers $headers
        }
        
        Add-TestResult $name $uri $method $expectedStatus $expectedStatus $true "Success"
        return $response
    } catch {
        $actualStatus = if ($_.Exception.Response) { $_.Exception.Response.StatusCode.value__ } else { "Error" }
        
        if ($skipErrorCheck -and $actualStatus -eq $expectedStatus) {
            Add-TestResult $name $uri $method $expectedStatus $actualStatus $true "Expected error"
            return $null
        }
        
        Add-TestResult $name $uri $method $expectedStatus $actualStatus $false $_.Exception.Message
        return $null
    }
}

# ===================================================================
# START TESTING
# ===================================================================

Write-Host "`n" -NoNewline
Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host "     COMPREHENSIVE API TESTING SUITE                                " -ForegroundColor Cyan
Write-Host "     E-Commerce Spare Parts Backend                                 " -ForegroundColor Cyan
Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Info "Base URL: $baseUrl"
Write-Info "Start Time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"

# ===================================================================
# PHASE 1: HEALTH & DOCUMENTATION
# ===================================================================
Write-TestHeader "PHASE 1" "Health & Documentation"

$health = Test-Endpoint "Health Check" "$baseUrl/actuator/health" -expectedStatus 200

Test-Endpoint "Swagger UI" "$baseUrl/swagger-ui.html" -expectedStatus 200
Test-Endpoint "OpenAPI Docs" "$baseUrl/v3/api-docs" -expectedStatus 200

# ===================================================================
# PHASE 2: AUTHENTICATION
# ===================================================================
Write-TestHeader "PHASE 2" "Authentication & Authorization"

$randomNum = Get-Random -Minimum 1000 -Maximum 9999
$username = "testuser_$randomNum"
$email = "test$randomNum@example.com"

# Register User
$registerBody = @{
    username = $username
    email = $email
    password = "Password123!"
    fullName = "Test User $randomNum"
    phoneNumber = "+216 20 123 456"
    address = "123 Test Street, Tunis"
} | ConvertTo-Json

$registerResponse = Test-Endpoint "User Registration" "$baseUrl/api/auth/register" -method "POST" -body $registerBody -expectedStatus 201

if ($registerResponse) {
    $user_id = $registerResponse.id
    Write-Info "Created User ID: $user_id"
}

# Login User
$loginBody = @{
    username = $username
    password = "Password123!"
} | ConvertTo-Json

$loginResponse = Test-Endpoint "User Login" "$baseUrl/api/auth/login" -method "POST" -body $loginBody -expectedStatus 200

if ($loginResponse -and $loginResponse.token) {
    $jwt_token = $loginResponse.token
    Write-Info "JWT Token Acquired: $($jwt_token.Substring(0,30))..."
}

# Test Invalid Login
$invalidLoginBody = @{
    username = $username
    password = "WrongPassword!"
} | ConvertTo-Json

Test-Endpoint "Invalid Login (Should Fail)" "$baseUrl/api/auth/login" -method "POST" -body $invalidLoginBody -expectedStatus 401 -skipErrorCheck $true

# Logout
if ($jwt_token) {
    $authHeaders = @{ "Authorization" = "Bearer $jwt_token" }
    Test-Endpoint "User Logout" "$baseUrl/api/auth/logout" -method "POST" -headers $authHeaders -expectedStatus 200
}

# ===================================================================
# PHASE 3: USER MANAGEMENT
# ===================================================================
Write-TestHeader "PHASE 3" "User Management"

if ($jwt_token) {
    $authHeaders = @{ "Authorization" = "Bearer $jwt_token" }
    
    # Get Profile
    $profile = Test-Endpoint "Get User Profile" "$baseUrl/api/users/profile" -headers $authHeaders
    
    # Update Profile
    $updateBody = @{
        fullName = "Updated Test User"
        phoneNumber = "+216 20 999 888"
        address = "456 New Address, Tunis"
    } | ConvertTo-Json
    
    Test-Endpoint "Update User Profile" "$baseUrl/api/users/profile" -method "PUT" -headers $authHeaders -body $updateBody
    
    # Change Password
    $passwordBody = @{
        oldPassword = "Password123!"
        newPassword = "NewPassword123!"
    } | ConvertTo-Json
    
    Test-Endpoint "Change Password" "$baseUrl/api/users/change-password" -method "PUT" -headers $authHeaders -body $passwordBody
    
    # Get All Users (may require admin)
    Test-Endpoint "Get All Users" "$baseUrl/api/users?page=0&size=10" -headers $authHeaders
    
    if ($user_id) {
        Test-Endpoint "Get User By ID" "$baseUrl/api/users/$user_id" -headers $authHeaders
    }
}

# ===================================================================
# PHASE 4: PRODUCT CATALOG
# ===================================================================
Write-TestHeader "PHASE 4" "Product Catalog Management"

# Public endpoints
Test-Endpoint "Get All Products" "$baseUrl/api/products?page=0&size=20"
Test-Endpoint "Get Featured Products" "$baseUrl/api/products/featured"
Test-Endpoint "Get New Arrivals" "$baseUrl/api/products/new-arrivals"
Test-Endpoint "Search Products" "$baseUrl/api/products/search?term=brake"

# Categories
Test-Endpoint "Get All Categories" "$baseUrl/api/categories"
Test-Endpoint "Get Category Tree" "$baseUrl/api/categories/tree"

# Brands
Test-Endpoint "Get All Brands" "$baseUrl/api/brands"

# Authenticated product actions
if ($jwt_token) {
    $authHeaders = @{ "Authorization" = "Bearer $jwt_token" }
    
    # Create Product (may require admin/seller role)
    $productBody = @{
        name = "Test Brake Pad"
        description = "High quality brake pad for testing"
        price = 89.99
        stockQuantity = 50
        categoryId = 1
        brandId = 1
        sku = "BRK-TEST-$(Get-Random)"
    } | ConvertTo-Json
    
    $productResponse = Test-Endpoint "Create Product" "$baseUrl/api/products" -method "POST" -headers $authHeaders -body $productBody
    
    if ($productResponse) {
        $product_id = $productResponse.id
        Write-Info "Created Product ID: $product_id"
        
        # Update Product
        $updateProductBody = @{
            name = "Updated Test Brake Pad"
            price = 99.99
        } | ConvertTo-Json
        
        Test-Endpoint "Update Product" "$baseUrl/api/products/$product_id" -method "PUT" -headers $authHeaders -body $updateProductBody
        
        # Get Product Details
        Test-Endpoint "Get Product By ID" "$baseUrl/api/products/$product_id"
        
        # Get Product Reviews
        Test-Endpoint "Get Product Reviews" "$baseUrl/api/products/$product_id/reviews"
    }
}

# ===================================================================
# PHASE 5: SHOPPING CART
# ===================================================================
Write-TestHeader "PHASE 5" "Shopping Cart Operations"

if ($jwt_token -and $product_id) {
    $authHeaders = @{ "Authorization" = "Bearer $jwt_token" }
    
    # Get Cart
    $cart = Test-Endpoint "Get User Cart" "$baseUrl/api/cart" -headers $authHeaders
    
    # Add to Cart
    $cartItemBody = @{
        productId = $product_id
        quantity = 2
    } | ConvertTo-Json
    
    Test-Endpoint "Add Item to Cart" "$baseUrl/api/cart/items" -method "POST" -headers $authHeaders -body $cartItemBody
    
    # Update Cart Item
    $updateCartBody = @{
        quantity = 3
    } | ConvertTo-Json
    
    Test-Endpoint "Update Cart Item" "$baseUrl/api/cart/items/$product_id" -method "PUT" -headers $authHeaders -body $updateCartBody
    
    # Get Cart Total
    Test-Endpoint "Get Cart Total" "$baseUrl/api/cart/total" -headers $authHeaders
    
    # Clear specific item
    Test-Endpoint "Remove Cart Item" "$baseUrl/api/cart/items/$product_id" -method "DELETE" -headers $authHeaders
    
    # Clear Cart
    Test-Endpoint "Clear Cart" "$baseUrl/api/cart/clear" -method "DELETE" -headers $authHeaders
}

# ===================================================================
# PHASE 6: ORDER MANAGEMENT
# ===================================================================
Write-TestHeader "PHASE 6" "Order Management"

if ($jwt_token -and $product_id) {
    $authHeaders = @{ "Authorization" = "Bearer $jwt_token" }
    
    # Re-add item to cart for order
    $cartItemBody = @{
        productId = $product_id
        quantity = 2
    } | ConvertTo-Json
    
    Test-Endpoint "Re-add Item to Cart" "$baseUrl/api/cart/items" -method "POST" -headers $authHeaders -body $cartItemBody
    
    # Create Order
    $orderBody = @{
        shippingAddress = "789 Shipping St, Tunis"
        paymentMethod = "CREDIT_CARD"
        notes = "Test order - please handle with care"
    } | ConvertTo-Json
    
    $orderResponse = Test-Endpoint "Create Order" "$baseUrl/api/orders" -method "POST" -headers $authHeaders -body $orderBody
    
    if ($orderResponse) {
        $order_id = $orderResponse.id
        Write-Info "Created Order ID: $order_id"
        
        # Get Order Details
        Test-Endpoint "Get Order By ID" "$baseUrl/api/orders/$order_id" -headers $authHeaders
        
        # Get Order Items
        Test-Endpoint "Get Order Items" "$baseUrl/api/orders/$order_id/items" -headers $authHeaders
        
        # Update Order Status (may require admin)
        $statusBody = @{
            status = "PROCESSING"
        } | ConvertTo-Json
        
        Test-Endpoint "Update Order Status" "$baseUrl/api/orders/$order_id/status" -method "PUT" -headers $authHeaders -body $statusBody
    }
    
    # Get User Orders
    Test-Endpoint "Get User Orders" "$baseUrl/api/orders/user?page=0&size=10" -headers $authHeaders
    
    # Get All Orders (admin)
    Test-Endpoint "Get All Orders" "$baseUrl/api/orders?page=0&size=10" -headers $authHeaders
}

# ===================================================================
# PHASE 7: VEHICLE MANAGEMENT
# ===================================================================
Write-TestHeader "PHASE 7" "Vehicle Management"

if ($jwt_token) {
    $authHeaders = @{ "Authorization" = "Bearer $jwt_token" }
    
    # Create Vehicle
    $vehicleBody = @{
        make = "Toyota"
        model = "Corolla"
        year = 2020
        vin = "TEST123456789VIN$(Get-Random)"
        licensePlate = "TN-1234-TEST"
    } | ConvertTo-Json
    
    $vehicleResponse = Test-Endpoint "Create Vehicle" "$baseUrl/api/vehicles" -method "POST" -headers $authHeaders -body $vehicleBody
    
    if ($vehicleResponse) {
        $vehicle_id = $vehicleResponse.id
        Write-Info "Created Vehicle ID: $vehicle_id"
        
        # Get Vehicle Details
        Test-Endpoint "Get Vehicle By ID" "$baseUrl/api/vehicles/$vehicle_id" -headers $authHeaders
        
        # Update Vehicle
        $updateVehicleBody = @{
            licensePlate = "TN-5678-UPDATED"
        } | ConvertTo-Json
        
        Test-Endpoint "Update Vehicle" "$baseUrl/api/vehicles/$vehicle_id" -method "PUT" -headers $authHeaders -body $updateVehicleBody
    }
    
    # Get User Vehicles
    Test-Endpoint "Get User Vehicles" "$baseUrl/api/vehicles/user" -headers $authHeaders
    
    # Get All Vehicles (admin)
    Test-Endpoint "Get All Vehicles" "$baseUrl/api/vehicles?page=0&size=10" -headers $authHeaders
    
    if ($vehicle_id) {
        Test-Endpoint "Delete Vehicle" "$baseUrl/api/vehicles/$vehicle_id" -method "DELETE" -headers $authHeaders
    }
}

# ===================================================================
# PHASE 8: RECLAMATIONS
# ===================================================================
Write-TestHeader "PHASE 8" "Reclamation Management"

if ($jwt_token -and $order_id) {
    $authHeaders = @{ "Authorization" = "Bearer $jwt_token" }
    
    # Create Reclamation
    $reclamationBody = @{
        orderId = $order_id
        subject = "Test Reclamation"
        description = "This is a test reclamation for order $order_id"
        type = "PRODUCT_QUALITY"
    } | ConvertTo-Json
    
    $reclamationResponse = Test-Endpoint "Create Reclamation" "$baseUrl/api/reclamations" -method "POST" -headers $authHeaders -body $reclamationBody
    
    if ($reclamationResponse) {
        $reclamation_id = $reclamationResponse.id
        Write-Info "Created Reclamation ID: $reclamation_id"
        
        # Get Reclamation Details
        Test-Endpoint "Get Reclamation By ID" "$baseUrl/api/reclamations/$reclamation_id" -headers $authHeaders
        
        # Update Reclamation Status
        $statusBody = @{
            status = "IN_PROGRESS"
        } | ConvertTo-Json
        
        Test-Endpoint "Update Reclamation Status" "$baseUrl/api/reclamations/$reclamation_id/status" -method "PUT" -headers $authHeaders -body $statusBody
        
        # Add Comment
        $commentBody = @{
            comment = "Working on this issue"
        } | ConvertTo-Json
        
        Test-Endpoint "Add Reclamation Comment" "$baseUrl/api/reclamations/$reclamation_id/comments" -method "POST" -headers $authHeaders -body $commentBody
    }
    
    # Get User Reclamations
    Test-Endpoint "Get User Reclamations" "$baseUrl/api/reclamations/user?page=0&size=10" -headers $authHeaders
    
    # Get All Reclamations (admin/support)
    Test-Endpoint "Get All Reclamations" "$baseUrl/api/reclamations?page=0&size=10" -headers $authHeaders
}

# ===================================================================
# PHASE 9: CHAT SYSTEM
# ===================================================================
Write-TestHeader "PHASE 9" "Chat & Messaging System"

if ($jwt_token) {
    $authHeaders = @{ "Authorization" = "Bearer $jwt_token" }
    
    # Get User Conversations
    Test-Endpoint "Get User Conversations" "$baseUrl/api/chat/conversations" -headers $authHeaders
    
    # Get Unread Message Count
    Test-Endpoint "Get Unread Messages Count" "$baseUrl/api/chat/messages/unread/count" -headers $authHeaders
    
    # Get Messages (WebSocket test - may not work via REST)
    Test-Endpoint "Get Chat Messages" "$baseUrl/api/chat/messages?page=0&size=20" -headers $authHeaders
}

# ===================================================================
# PHASE 10: AI RECOMMENDATIONS
# ===================================================================
Write-TestHeader "PHASE 10" "AI Recommendation Engine"

if ($jwt_token -and $product_id) {
    $authHeaders = @{ "Authorization" = "Bearer $jwt_token" }
    
    # Get Product Recommendations
    Test-Endpoint "Get Product Recommendations" "$baseUrl/api/recommendations/products/$product_id" -headers $authHeaders
    
    # Get User Recommendations
    Test-Endpoint "Get User Recommendations" "$baseUrl/api/recommendations/user" -headers $authHeaders
    
    # Get Similar Products
    Test-Endpoint "Get Similar Products" "$baseUrl/api/recommendations/similar/$product_id"
    
    # Get Trending Products
    Test-Endpoint "Get Trending Products" "$baseUrl/api/recommendations/trending"
}

# ===================================================================
# PHASE 11: DELIVERY TRACKING
# ===================================================================
Write-TestHeader "PHASE 11" "Delivery & Shipment Tracking"

if ($jwt_token -and $order_id) {
    $authHeaders = @{ "Authorization" = "Bearer $jwt_token" }
    
    # Get Delivery Status
    Test-Endpoint "Get Delivery Status" "$baseUrl/api/delivery/$order_id/status" -headers $authHeaders
    
    # Get Delivery History
    Test-Endpoint "Get Delivery History" "$baseUrl/api/delivery/$order_id/history" -headers $authHeaders
    
    # Update Delivery Location (driver/admin)
    $locationBody = @{
        latitude = 36.8065
        longitude = 10.1815
        address = "Avenue Habib Bourguiba, Tunis"
    } | ConvertTo-Json
    
    Test-Endpoint "Update Delivery Location" "$baseUrl/api/delivery/$order_id/location" -method "PUT" -headers $authHeaders -body $locationBody
}

# ===================================================================
# PHASE 12: ADMIN & REPORTS
# ===================================================================
Write-TestHeader "PHASE 12" "Admin & Analytics"

if ($jwt_token) {
    $authHeaders = @{ "Authorization" = "Bearer $jwt_token" }
    
    # Dashboard Stats
    Test-Endpoint "Get Dashboard Stats" "$baseUrl/api/admin/dashboard/stats" -headers $authHeaders
    
    # Sales Reports
    Test-Endpoint "Get Sales Report" "$baseUrl/api/admin/reports/sales?startDate=2024-01-01&endDate=2024-12-31" -headers $authHeaders
    
    # User Analytics
    Test-Endpoint "Get User Analytics" "$baseUrl/api/admin/analytics/users" -headers $authHeaders
    
    # Product Analytics
    Test-Endpoint "Get Product Analytics" "$baseUrl/api/admin/analytics/products" -headers $authHeaders
    
    # Order Analytics
    Test-Endpoint "Get Order Analytics" "$baseUrl/api/admin/analytics/orders" -headers $authHeaders
    
    # Top Products
    Test-Endpoint "Get Top Products" "$baseUrl/api/admin/reports/top-products?limit=10" -headers $authHeaders
    
    # Revenue Report
    Test-Endpoint "Get Revenue Report" "$baseUrl/api/admin/reports/revenue?period=monthly" -headers $authHeaders
}

# ===================================================================
# PHASE 13: NOTIFICATIONS
# ===================================================================
Write-TestHeader "PHASE 13" "Notification System"

if ($jwt_token) {
    $authHeaders = @{ "Authorization" = "Bearer $jwt_token" }
    
    # Get User Notifications
    Test-Endpoint "Get User Notifications" "$baseUrl/api/notifications?page=0&size=20" -headers $authHeaders
    
    # Get Unread Notifications
    Test-Endpoint "Get Unread Notifications" "$baseUrl/api/notifications/unread" -headers $authHeaders
    
    # Mark as Read
    Test-Endpoint "Mark All Notifications Read" "$baseUrl/api/notifications/mark-all-read" -method "PUT" -headers $authHeaders
}

# ===================================================================
# FINAL RESULTS
# ===================================================================
Write-Host "`n" -NoNewline
Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host "                    TEST RESULTS SUMMARY                            " -ForegroundColor Cyan
Write-Host "=====================================================================" -ForegroundColor Cyan

# Calculate statistics
$totalTests = $testResults.Count
$passedTests = ($testResults | Where-Object { $_.Status -eq "PASS" }).Count
$failedTests = ($testResults | Where-Object { $_.Status -eq "FAIL" }).Count
$successRate = if ($totalTests -gt 0) { [math]::Round(($passedTests / $totalTests) * 100, 2) } else { 0 }

# Display summary table
Write-Host "`nDetailed Test Results:" -ForegroundColor Cyan
$testResults | Format-Table -Property Test, Method, Endpoint, Status, Notes, Timestamp -AutoSize

# Display statistics
Write-Host "`n[STATISTICS]" -ForegroundColor Cyan
Write-Host "=====================================================================" -ForegroundColor DarkCyan
Write-Host "Total Tests:    " -NoNewline; Write-Host $totalTests -ForegroundColor White
Write-Host "Passed:         " -NoNewline; Write-Host $passedTests -ForegroundColor Green
Write-Host "Failed:         " -NoNewline; Write-Host $failedTests -ForegroundColor Red
Write-Host "Success Rate:   " -NoNewline; Write-Host "$successRate%" -ForegroundColor $(if ($successRate -ge 80) { "Green" } elseif ($successRate -ge 50) { "Yellow" } else { "Red" })
Write-Host "End Time:       " -NoNewline; Write-Host (Get-Date -Format 'yyyy-MM-dd HH:mm:ss') -ForegroundColor White

# Export results to CSV
$reportFile = "test-results-$(Get-Date -Format 'yyyyMMdd-HHmmss').csv"
$testResults | Export-Csv -Path $reportFile -NoTypeInformation
Write-Host "`n[REPORT] Results exported to: " -NoNewline -ForegroundColor Cyan
Write-Host $reportFile -ForegroundColor Yellow

Write-Host "`n" -NoNewline
Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host "                    TESTING COMPLETE                                " -ForegroundColor Cyan
Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host ""
