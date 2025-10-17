# 🔧 DELIVERY TABLE FIX - Step by Step Guide

## Problem Identified
Your database has `ondelivery_tracking_id` column, but your Java code expects `tracking_number`.

## Current Database Structure
```
- id (uuid)
- order_id (uuid)
- status (varchar)
- address (text)
- ondelivery_tracking_id (varchar) ❌ NEEDS TO BE RENAMED
- created_at (timestamp)
- updated_at (timestamp)
```

## Target Structure (What Java Code Expects)
```
- id (uuid)
- order_id (uuid)
- status (varchar)
- address (text)
- tracking_number (varchar) ✅ RENAMED
- delivery_notes (text) ✅ NEW
- estimated_delivery (timestamp) ✅ NEW
- actual_delivery (timestamp) ✅ NEW
- driver_name (varchar) ✅ NEW
- driver_phone (varchar) ✅ NEW
- pickup_time (timestamp) ✅ NEW
- current_location (text) ✅ NEW
- created_at (timestamp)
- updated_at (timestamp)
```

---

## 🎯 Solution: Run These SQL Commands

### **Step 1: Open pgAdmin and connect to your database**
- Database: `ecommercespareparts`
- User: `lasmer`

### **Step 2: Open Query Tool and paste this SQL**

```sql
-- ==============================================================
-- STEP 1: Rename ondelivery_tracking_id to tracking_number
-- ==============================================================
ALTER TABLE deliveries 
RENAME COLUMN ondelivery_tracking_id TO tracking_number;

-- ==============================================================
-- STEP 2: Add all missing columns
-- ==============================================================
ALTER TABLE deliveries ADD COLUMN delivery_notes TEXT;
ALTER TABLE deliveries ADD COLUMN estimated_delivery TIMESTAMP;
ALTER TABLE deliveries ADD COLUMN actual_delivery TIMESTAMP;
ALTER TABLE deliveries ADD COLUMN driver_name VARCHAR(255);
ALTER TABLE deliveries ADD COLUMN driver_phone VARCHAR(20);
ALTER TABLE deliveries ADD COLUMN pickup_time TIMESTAMP;
ALTER TABLE deliveries ADD COLUMN current_location TEXT;

-- ==============================================================
-- STEP 3: Generate tracking numbers for existing records
-- ==============================================================
UPDATE deliveries 
SET tracking_number = 'TRK' || EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT || SUBSTRING(gen_random_uuid()::TEXT, 1, 8)
WHERE tracking_number IS NULL OR tracking_number = '';

-- ==============================================================
-- STEP 4: Make tracking_number NOT NULL
-- ==============================================================
ALTER TABLE deliveries 
ALTER COLUMN tracking_number SET NOT NULL;

-- ==============================================================
-- STEP 5: Add unique constraint
-- ==============================================================
ALTER TABLE deliveries 
ADD CONSTRAINT unique_tracking_number UNIQUE (tracking_number);

-- ==============================================================
-- STEP 6: Create indexes for performance
-- ==============================================================
CREATE INDEX idx_deliveries_tracking_number ON deliveries(tracking_number);
CREATE INDEX idx_deliveries_status ON deliveries(status);
CREATE INDEX idx_deliveries_created_at ON deliveries(created_at);

-- ==============================================================
-- STEP 7: Verify the changes
-- ==============================================================
SELECT column_name, data_type, is_nullable, character_maximum_length
FROM information_schema.columns
WHERE table_name = 'deliveries'
ORDER BY ordinal_position;
```

### **Step 3: Execute the SQL**
- Click the **Execute/Play button** (F5) or click "Execute"
- You should see "Query returned successfully" messages

### **Step 4: Verify the Result**
After running the query, you should see this structure:

```
column_name          | data_type    | is_nullable
---------------------|--------------|------------
id                   | uuid         | NO
order_id             | uuid         | NO
status               | varchar      | NO
address              | text         | NO
tracking_number      | varchar      | NO  ✅
delivery_notes       | text         | YES ✅
estimated_delivery   | timestamp    | YES ✅
actual_delivery      | timestamp    | YES ✅
driver_name          | varchar      | YES ✅
driver_phone         | varchar      | YES ✅
pickup_time          | timestamp    | YES ✅
current_location     | text         | YES ✅
created_at           | timestamp    | YES
updated_at           | timestamp    | YES
```

---

## ✅ What I've Already Fixed in Your Java Code

I've updated your `Delivery.java` entity to include:
- ✅ `pickupTime` field mapped to `pickup_time`
- ✅ `currentLocation` field mapped to `current_location`
- ✅ Fixed column mapping: `address` instead of `delivery_address`

---

## 🚀 Next Steps After Running SQL

1. **Verify in pgAdmin:**
   ```sql
   SELECT * FROM deliveries;
   ```

2. **Build your Java project:**
   ```bash
   mvn clean compile
   ```

3. **Test the application:**
   - Create a test order
   - Check if delivery is created with tracking number
   - Verify all fields are saved correctly

---

## 🔍 If You Get Any Errors

### Error: "column already exists"
- **Solution:** That column was already added, skip that specific ADD COLUMN command

### Error: "relation unique_tracking_number already exists"
- **Solution:** The constraint already exists, you can skip it or drop first:
  ```sql
  ALTER TABLE deliveries DROP CONSTRAINT IF EXISTS unique_tracking_number;
  ALTER TABLE deliveries ADD CONSTRAINT unique_tracking_number UNIQUE (tracking_number);
  ```

### Error: "cannot execute ... in a read-only transaction"
- **Solution:** Make sure you're connected to the correct database and have write permissions

---

## 📝 Summary of Changes

### Database Changes:
- ✅ Renamed `ondelivery_tracking_id` → `tracking_number`
- ✅ Added `delivery_notes`
- ✅ Added `estimated_delivery`
- ✅ Added `actual_delivery`
- ✅ Added `driver_name`
- ✅ Added `driver_phone`
- ✅ Added `pickup_time`
- ✅ Added `current_location`
- ✅ Made `tracking_number` NOT NULL and UNIQUE
- ✅ Created indexes for performance

### Java Entity Changes:
- ✅ Added `pickupTime` field
- ✅ Added `currentLocation` field
- ✅ Fixed `deliveryAddress` mapping to `address` column

---

## ✅ Ready to Go!

After running the SQL commands above, your database will be perfectly aligned with your Java entity, and the mock delivery system will work flawlessly!

**Run the SQL now in pgAdmin, then test with `mvn clean compile`** 🚀
