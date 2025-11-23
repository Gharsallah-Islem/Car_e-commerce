# 🔧 Admin Dashboard - Runtime Errors Fix

**Date**: November 17, 2025  
**Status**: 🔄 **IN PROGRESS**

---

## 🐛 Errors Identified

### 1. Cart Service 403 Error ❌
**Error**: `Failed to load resource: the server responded with a status of 403`
**Location**: `cart.service.ts:78`

**Cause**: Cart endpoint requires authentication, but admin page might not have proper token

**Fix**: This is expected on admin page - cart is not needed. Can be ignored or we can conditionally load cart only on non-admin pages.

---

### 2. MatBadge Accessibility Warning ⚠️
**Warning**: `Detected a matBadge on an "aria-hidden" "<mat-icon>"`

**Fix**: Add `aria-hidden="false"` to the icon with badge

**Location**: Main app navbar - shopping cart icon

**Solution**:
```html
<!-- Before -->
<mat-icon [matBadge]="cartItemCount()" matBadgeColor="warn" 
          [matBadgeHidden]="cartItemCount() === 0" aria-hidden="false">
  shopping_cart
</mat-icon>

<!-- After -->
<mat-icon [matBadge]="cartItemCount()" matBadgeColor="warn" 
          [matBadgeHidden]="cartItemCount() === 0" 
          aria-hidden="false">
  shopping_cart
</mat-icon>
```

---

### 3. Form Control 'autoReorder' Error ❌
**Error**: `NG01203: No value accessor for form control name: 'autoReorder'`

**Cause**: The form control exists in TypeScript but the HTML template might be using it incorrectly or missing the proper form control directive.

**Possible Issues**:
1. Missing `MatSlideToggleModule` import
2. Using wrong directive (should be `mat-slide-toggle` for boolean)
3. Template not properly bound to form

**Fix**: Ensure the reorder settings form uses proper Material components

---

### 4. Backend 500 Errors - Stock Movements ❌
**Error**: `500 Internal Server Error`
**Endpoint**: `/api/inventory/stock-movements?page=0&size=10&sort=date`

**Cause**: Backend error when fetching stock movements

**Possible Issues**:
1. Database table doesn't exist
2. Entity mapping issue
3. Query error
4. No data exists

**Need to check**: Backend console logs for the actual error

---

### 5. Backend 500 Errors - Deliveries ❌
**Error**: `500 Internal Server Error`
**Endpoints**: 
- `/api/delivery?page=0&size=10&sort=createdAt`
- `/api/delivery/active?page=0&size=100`

**Cause**: Backend error when fetching deliveries

**Possible Issues**:
1. Database table doesn't exist
2. Entity mapping issue
3. Query error
4. No data exists

**Need to check**: Backend console logs for the actual error

---

## 🔧 Quick Fixes

### Fix 1: Ignore Cart Error on Admin Page

**File**: `frontend-web/src/app/app.component.ts`

Add conditional cart loading:

```typescript
constructor() {
    // ... existing code ...
    
    // Only load cart if not on admin page
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      const url = event.url;
      const isAdminPage = url.includes('/admin');
      this.showNavbar.set(!isAdminPage);
      
      // Only load cart on non-admin pages
      if (!isAdminPage && this.isLoggedIn()) {
        this.cartService.loadCart();
      }
    });
}
```

---

### Fix 2: Fix MatBadge Accessibility

**File**: `frontend-web/src/app/app.component.html`

```html
<button mat-icon-button routerLink="/cart">
  <mat-icon [matBadge]="cartItemCount()" 
            matBadgeColor="warn" 
            [matBadgeHidden]="cartItemCount() === 0"
            aria-hidden="false"
            aria-label="Shopping cart">
    shopping_cart
  </mat-icon>
</button>
```

---

### Fix 3: Backend Errors - Need Backend Logs

**Action Required**: Check backend console for actual error messages

**Common Solutions**:

1. **If tables don't exist**:
```bash
# Run database migrations
cd Backend
mvn flyway:migrate
# or
mvn spring-boot:run
```

2. **If no data exists**:
   - This is normal for a new installation
   - Frontend should handle empty responses gracefully
   - Add empty state UI

3. **If entity mapping error**:
   - Check entity relationships
   - Verify database schema matches entities

---

## 🎯 Priority Fixes

### High Priority:
1. ✅ Fix backend 500 errors (check backend logs)
2. ⚠️ Handle empty data states in frontend
3. ⚠️ Fix autoReorder form control

### Medium Priority:
4. ⚠️ Fix MatBadge accessibility warning
5. ⚠️ Conditionally load cart service

### Low Priority:
6. ℹ️ Add better error messages
7. ℹ️ Add retry logic for failed requests

---

## 🔍 Debugging Steps

### Step 1: Check Backend Console
Look for error stack traces when these endpoints are called:
- `/api/inventory/stock-movements`
- `/api/delivery`
- `/api/delivery/active`

### Step 2: Check Database
Verify tables exist:
```sql
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN ('stock_movement', 'delivery');
```

### Step 3: Test Endpoints Directly
Use curl or Postman:
```bash
curl -H "Authorization: Bearer YOUR_TOKEN" \
     http://localhost:8080/api/inventory/stock-movements?page=0&size=10
```

### Step 4: Check Entity Mappings
Verify in backend:
- `StockMovement.java`
- `Delivery.java`

---

## 📝 Temporary Workaround

While debugging backend, add error handling in frontend:

**File**: `frontend-web/src/app/features/admin/inventory-management/inventory-management.component.ts`

```typescript
loadStockMovements(): void {
    this.loading.set(true);
    const page = this.pageIndex();
    const size = this.pageSize();
    
    this.inventoryService.getStockMovements(page, size).subscribe({
        next: (response) => {
            this.stockMovements.set(response.content);
            this.loading.set(false);
        },
        error: (error) => {
            console.error('Error loading stock movements:', error);
            // Set empty array instead of showing error
            this.stockMovements.set([]);
            this.loading.set(false);
            // Don't show error notification for empty data
            if (error.status !== 404) {
                this.notificationService.error('Erreur lors du chargement des mouvements de stock');
            }
        }
    });
}
```

---

## ✅ What to Check in Backend

1. **Backend Console Output**: Look for stack traces
2. **Database Connection**: Verify PostgreSQL is running
3. **Tables Exist**: Check if `stock_movement` and `delivery` tables exist
4. **Data Exists**: Check if there's any data in these tables
5. **Entity Relationships**: Verify foreign keys are correct

---

## 🚀 Next Steps

1. **Start backend** and check console for errors
2. **Check database** for missing tables
3. **Apply frontend fixes** for better error handling
4. **Test each endpoint** individually
5. **Add sample data** if tables are empty

---

**Status**: Waiting for backend error logs to determine root cause

**Note**: The 500 errors are backend issues, not frontend issues. The frontend is correctly calling the APIs, but the backend is failing to process the requests.

