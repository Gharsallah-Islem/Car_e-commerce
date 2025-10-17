# ===================================================================
# COMPREHENSIVE API TESTING SCRIPT
# E-Commerce Spare Parts Backend
# ===================================================================

$baseUrl = "http://localhost:8080"
$testResults = @()
$jwt_token = ""
$user_id = ""
$product_id = ""
$order_id = ""
$cart_id = ""

# Color output functions
function Write-Success { param($message) Write-Host "✅ $message" -ForegroundColor Green }
function Write-Error { param($message) Write-Host "❌ $message" -ForegroundColor Red }
function Write-Info { param($message) Write-Host "ℹ️  $message" -ForegroundColor Cyan }
function Write-Warning { param($message) Write-Host "⚠️  $message" -ForegroundColor Yellow }

# Test result tracking
function Add-TestResult {
    param($testName, $endpoint, $method, $expected, $actual, $passed, $notes = "")
    $testResults += [PSCustomObject]@{
        Test = $testName
        Endpoint = $endpoint
        Method = $method
        Expected = $expected
        Actual = $actual
        Status = if ($passed) { "✅ PASS" } else { "❌ FAIL" }
        Notes = $notes
    }
}

Write-Info "==================================================================="
Write-Info "Starting Comprehensive API Testing"
Write-Info "Base URL: $baseUrl"
Write-Info "==================================================================="

# ===================================================================
# PHASE 1: HEALTH & DOCUMENTATION TESTS
# ===================================================================
Write-Info "`n📋 PHASE 1: Health & Documentation Tests"

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method Get
    if ($response.status -eq "UP") {
        Write-Success "Health Check: Application is UP"
        Add-TestResult "Health Check" "/actuator/health" "GET" "200" "200" $true
    }
} catch {
    Write-Error "Health Check Failed: $_"
    Add-TestResult "Health Check" "/actuator/health" "GET" "200" "Error" $false
}

# ===================================================================
# PHASE 2: AUTHENTICATION TESTS
# ===================================================================
Write-Info "`n🔐 PHASE 2: Authentication Tests"

# Test 1: User Registration
Write-Info "Test: User Registration"
$registerBody = @{
    username = "testuser_$(Get-Random)"
    email = "test$(Get-Random)@example.com"
    password = "Password123!"
    fullName = "Test User"
    phoneNumber = "+216 20 123 456"
    address = "123 Test Street, Tunis"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" `
        -Method Post `
        -Body $registerBody `
        -ContentType "application/json"
    
    $user_id = $response.id
    Write-Success "User Registration: SUCCESS (User ID: $user_id)"
    Add-TestResult "User Registration" "/api/auth/register" "POST" "201" "201" $true "User created with ID: $user_id"
} catch {
    Write-Error "User Registration Failed: $_"
    Add-TestResult "User Registration" "/api/auth/register" "POST" "201" "Error" $false
}

# Test 2: User Login
Write-Info "Test: User Login"
$loginBody = @{
    username = $registerBody.username
    password = "Password123!"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" `
        -Method Post `
        -Body $loginBody `
        -ContentType "application/json"
    
    $jwt_token = $response.token
    Write-Success "User Login: SUCCESS"
    Write-Info "JWT Token: $($jwt_token.Substring(0, 20))..."
    Add-TestResult "User Login" "/api/auth/login" "POST" "200" "200" $true "Token received"
} catch {
    Write-Error "User Login Failed: $_"
    Add-TestResult "User Login" "/api/auth/login" "POST" "200" "Error" $false
}

# Test 3: Invalid Login
Write-Info "Test: Invalid Login (should fail)"
$invalidLoginBody = @{
    username = $registerBody.username
    password = "WrongPassword!"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" `
        -Method Post `
        -Body $invalidLoginBody `
        -ContentType "application/json"
    
    Write-Error "Invalid Login: Should have failed but succeeded"
    Add-TestResult "Invalid Login" "/api/auth/login" "POST" "401" "200" $false
} catch {
    if ($_.Exception.Response.StatusCode -eq 401) {
        Write-Success "Invalid Login: Correctly rejected (401)"
        Add-TestResult "Invalid Login" "/api/auth/login" "POST" "401" "401" $true
    }
}

# ===================================================================
# PHASE 3: USER MANAGEMENT TESTS
# ===================================================================
Write-Info "`n👤 PHASE 3: User Management Tests"

if ($jwt_token) {
    $headers = @{
        "Authorization" = "Bearer $jwt_token"
        "Content-Type" = "application/json"
    }

    # Test: Get User Profile
    Write-Info "Test: Get User Profile"
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/api/users/profile" `
            -Method Get `
            -Headers $headers
        
        Write-Success "Get User Profile: SUCCESS"
        Add-TestResult "Get User Profile" "/api/users/profile" "GET" "200" "200" $true
    } catch {
        Write-Error "Get User Profile Failed: $_"
        Add-TestResult "Get User Profile" "/api/users/profile" "GET" "200" "Error" $false
    }

    # Test: Update User Profile
    Write-Info "Test: Update User Profile"
    $updateBody = @{
        fullName = "Updated Test User"
        phoneNumber = "+216 20 999 888"
        address = "456 New Address, Tunis"
    } | ConvertTo-Json

    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/api/users/profile" `
            -Method Put `
            -Headers $headers `
            -Body $updateBody
        
        Write-Success "Update User Profile: SUCCESS"
        Add-TestResult "Update User Profile" "/api/users/profile" "PUT" "200" "200" $true
    } catch {
        Write-Error "Update User Profile Failed: $_"
        Add-TestResult "Update User Profile" "/api/users/profile" "PUT" "200" "Error" $false
    }
}

# ===================================================================
# PHASE 4: PRODUCT MANAGEMENT TESTS
# ===================================================================
Write-Info "`n🛒 PHASE 4: Product Management Tests"

# Test: Get All Products (Public)
Write-Info "Test: Get All Products"
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/products?page=0&size=10" `
        -Method Get
    
    Write-Success "Get All Products: SUCCESS (Found $($response.content.Count) products)"
    Add-TestResult "Get All Products" "/api/products" "GET" "200" "200" $true "Found $($response.content.Count) products"
} catch {
    Write-Error "Get All Products Failed: $_"
    Add-TestResult "Get All Products" "/api/products" "GET" "200" "Error" $false
}

# ===================================================================
# TEST RESULTS SUMMARY
# ===================================================================
Write-Info "`n📊 TEST RESULTS SUMMARY"
Write-Info "==================================================================="

$totalTests = $testResults.Count
$passedTests = ($testResults | Where-Object { $_.Status -eq "✅ PASS" }).Count
$failedTests = ($testResults | Where-Object { $_.Status -eq "❌ FAIL" }).Count
$successRate = if ($totalTests -gt 0) { [math]::Round(($passedTests / $totalTests) * 100, 2) } else { 0 }

$testResults | Format-Table -AutoSize

Write-Info "`nTOTAL TESTS: $totalTests"
Write-Success "PASSED: $passedTests"
Write-Error "FAILED: $failedTests"
Write-Info "SUCCESS RATE: $successRate%"

Write-Info "`n==================================================================="
Write-Info "Testing Complete!"
Write-Info "==================================================================="
