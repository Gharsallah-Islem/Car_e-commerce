# Database Schema Analysis & Data Insertion Plan

## Current Database Status

### Existing Data (as of analysis):
| Table | Count | Status |
|-------|-------|--------|
| users | 15 | ✅ Has data |
| products | 27 | ✅ Has data |
| categories | 17 | ✅ Has data |
| brands | 29 | ✅ Has data |
| orders | 14 | ✅ Has data |
| order_items | 20 | ✅ Has data |
| payments | 6 | ✅ Has data |
| **deliveries** | **6** | ✅ **HAS DATA!** |
| **suppliers** | **0** | ❌ **EMPTY** |
| **purchase_orders** | **0** | ❌ **EMPTY** |
| **stock_movements** | **0** | ❌ **EMPTY** |
| **reorder_settings** | **0** | ❌ **EMPTY** |

## Important Discovery!

**DELIVERIES TABLE IS NOT EMPTY!**

The deliveries table actually contains 6 records:
- TRK-2024-001: DELIVERED (John Smith)
- TRK-2024-002: DELIVERED (Sarah Johnson)
- TRK-2024-003: IN_TRANSIT (Michael Brown)
- TRK-2024-004: PROCESSING (David Wilson)
- TRK-2024-005: PENDING (Ahmed Ben Salah)
- TRK-2024-006: DELIVERED (Mohamed Trabelsi)

### Why the Delivery Page Shows No Data

**Possible Reasons:**
1. **Frontend API endpoint mismatch** - The frontend might be calling the wrong endpoint
2. **Authentication issue** - The user might not have the right role (ADMIN/SUPER_ADMIN required)
3. **CORS issue** - Cross-origin request might be blocked
4. **Pagination issue** - The frontend might be requesting a page that doesn't exist
5. **Date sorting issue** - The `createdAt` column might have NULL values causing sorting problems

**Next Steps for Delivery Page:**
1. Check browser console for API errors
2. Verify the user is logged in as ADMIN
3. Check network tab to see if API call is being made
4. Verify the endpoint: `GET /api/delivery?page=0&size=20&sort=createdAt`

## Database Schema Details

### Products Table Schema
```
id            | uuid
name          | varchar (NOT NULL)
price         | numeric (NOT NULL)
stock         | integer (NOT NULL)
brand         | varchar
category      | varchar
description   | text
image_url     | text
model         | varchar
compatibility | text
year          | integer
created_at    | timestamp
updated_at    | timestamp
brand_id      | bigint
category_id   | bigint
```

### Suppliers Table Schema
```
id             | uuid (PRIMARY KEY)
name           | varchar (NOT NULL)
company_name   | varchar
email          | varchar
phone          | varchar
address        | varchar
city           | varchar
postal_code    | varchar
country        | varchar
contact_person | varchar
tax_id         | varchar
payment_terms  | varchar
notes          | text
is_active      | boolean (NOT NULL)
rating         | double precision
website        | varchar
created_at     | timestamp (NOT NULL)
updated_at     | timestamp
```

### Purchase Orders Table Schema
```
id                     | uuid (PRIMARY KEY)
po_number              | varchar (NOT NULL, UNIQUE)
supplier_id            | uuid (NOT NULL, FOREIGN KEY)
order_date             | date (NOT NULL)
expected_delivery_date | date
actual_delivery_date   | date
status                 | varchar (NOT NULL)
total_amount           | numeric (NOT NULL)
tax_amount             | numeric
shipping_cost          | numeric
discount_amount        | numeric
grand_total            | numeric (NOT NULL)
notes                  | text
created_by             | varchar
approved_by            | varchar
received_by            | varchar
created_at             | timestamp (NOT NULL)
updated_at             | timestamp
```

### Stock Movements Table Schema
```
id             | uuid (PRIMARY KEY)
product_id     | uuid (NOT NULL, FOREIGN KEY)
movement_type  | varchar (NOT NULL)
quantity       | integer (NOT NULL)
previous_stock | integer (NOT NULL)
new_stock      | integer (NOT NULL)
reference_id   | uuid
reference_type | varchar
notes          | text
performed_by   | varchar
movement_date  | timestamp (NOT NULL)
```

### Reorder Settings Table Schema
```
id                    | uuid (PRIMARY KEY)
product_id            | uuid (NOT NULL, FOREIGN KEY)
reorder_point         | integer (NOT NULL)
reorder_quantity      | integer (NOT NULL)
minimum_stock         | integer
maximum_stock         | integer
lead_time_days        | integer
auto_reorder          | boolean (NOT NULL)
is_enabled            | boolean (NOT NULL)
preferred_supplier_id | uuid (FOREIGN KEY)
notes                 | text
last_alert_sent       | timestamp
last_reorder_date     | timestamp
created_at            | timestamp (NOT NULL)
updated_at            | timestamp
```

## Sample Existing Data

### Users (Sample)
```
704ec2e6-2227-471c-8339-b84dc1b2957f | mansourilamiaiset | mansourilamiaiset@gmail.com | lamia mansouri
61e534f1-9bb6-418f-9dda-38d361ae68dc | testuser_8723     | test8723@example.com        | Test User 8723
96d6587d-66f7-4548-ac53-80fa7ab7ecc2 | lasmer701         | lasmer701@gmail.com         | Lasmer
```

### Products (Sample)
```
a1111111-1111-1111-1111-111111111111 | Platinum Spark Plugs Set (4pcs) | 45.99  | 148
a1111111-1111-1111-1111-111111111112 | Engine Oil Filter               | 12.50  | 200
a1111111-1111-1111-1111-111111111113 | Engine Air Filter               | 18.75  | 174
a1111111-1111-1111-1111-111111111115 | Timing Belt Kit                 | 125.00 | 57
a2222222-2222-2222-2222-222222222221 | Ceramic Brake Pads (Front)      | 95.50  | 118
```

## Data Insertion Strategy

### Phase 1: Suppliers (10 records)
- Bosch Tunisia
- Mann Filter Tunisia
- Brembo Tunisia
- NGK Tunisia
- Valeo Tunisia
- Continental Tunisia
- Mahle Tunisia
- Denso Tunisia
- ZF Tunisia
- Schaeffler Tunisia

### Phase 2: Purchase Orders (15 records)
- Status distribution:
  - RECEIVED: 6 orders
  - APPROVED: 4 orders
  - PENDING: 3 orders
  - DRAFT: 1 order
  - CANCELLED: 1 order

### Phase 3: Stock Movements (12 records)
- Movement types:
  - PURCHASE: 5 movements (from received POs)
  - SALE: 5 movements (from orders)
  - ADJUSTMENT: 1 movement
  - DAMAGED: 1 movement
  - RETURN_FROM_CUSTOMER: 1 movement

### Phase 4: Reorder Settings (5 records)
- For high-demand products:
  - Spark Plugs (auto-reorder enabled)
  - Oil Filters (auto-reorder enabled)
  - Brake Pads (auto-reorder enabled)
  - Air Filters (auto-reorder enabled)
  - Timing Belts (manual reorder)

## SQL Execution Plan

1. **Execute**: `INSERT_INVENTORY_DATA.sql`
   - Inserts all suppliers
   - Inserts all purchase orders
   - Inserts all stock movements
   - Inserts all reorder settings

2. **Verify**: Run verification queries
   ```sql
   SELECT COUNT(*) FROM suppliers;        -- Should be 10
   SELECT COUNT(*) FROM purchase_orders;  -- Should be 15
   SELECT COUNT(*) FROM stock_movements;  -- Should be 12
   SELECT COUNT(*) FROM reorder_settings; -- Should be 5
   ```

3. **Test**: Refresh admin dashboard pages
   - Inventory page should show all data
   - Delivery page should show 6 deliveries (already exists!)

## Expected Results After Insertion

### Inventory Page - Suppliers Tab
- Should display 10 suppliers
- All marked as active
- Ratings between 4.4 and 4.9
- Contact information visible

### Inventory Page - Purchase Orders Tab
- Should display 15 purchase orders
- Filter by status should work
- Total amounts calculated correctly
- Supplier names displayed

### Inventory Page - Stock Movements Tab
- Should display 12 movements
- Different movement types visible
- Product names displayed
- Performed by information shown

### Inventory Page - Reorder Settings Tab
- Should display 5 settings
- Auto-reorder toggle functional
- Reorder points visible
- Supplier associations shown

### Delivery Page
- Should display 6 deliveries (already in database!)
- Tracking numbers visible
- Status badges colored correctly
- Driver information shown

## Troubleshooting Guide

### If Inventory Page Still Shows No Data:

1. **Check API Response**:
   - Open browser DevTools (F12)
   - Go to Network tab
   - Refresh page
   - Look for `/api/inventory/suppliers` request
   - Check response status and body

2. **Check Authentication**:
   - Verify user is logged in
   - Check user role is ADMIN or SUPER_ADMIN
   - Look for 403 Forbidden errors

3. **Check Database**:
   ```sql
   SELECT COUNT(*) FROM suppliers;
   SELECT * FROM suppliers LIMIT 1;
   ```

### If Delivery Page Still Shows No Data:

1. **Check API Response**:
   - Look for `/api/delivery` request in Network tab
   - Check response status (should be 200)
   - Check response body (should contain 6 deliveries)

2. **Check Frontend Code**:
   - Verify `DeliveryService.getAllDeliveries()` is being called
   - Check if `response.content` is being set correctly
   - Look for console errors

3. **Check Backend Logs**:
   - Look for any errors in Spring Boot console
   - Check if endpoint is being hit
   - Verify SQL query is executing

## Files Created

1. **INSERT_INVENTORY_DATA.sql** - Main insertion script
2. **DATABASE_ANALYSIS.md** - This document
3. **verify_before.sql** - Pre-insertion verification
4. **check_data.sql** - Data count checker

## Next Actions

1. ✅ Fix UUID format issue in SQL script
2. ⏳ Execute corrected SQL script
3. ⏳ Verify data insertion
4. ⏳ Test frontend pages
5. ⏳ Debug delivery page if still not showing data
