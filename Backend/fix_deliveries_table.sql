-- ====================================================================
-- FIX: Update deliveries table to match Java entity structure
-- Run this in pgAdmin
-- ====================================================================

-- Step 1: Rename the existing column
ALTER TABLE deliveries 
RENAME COLUMN ondelivery_tracking_id TO tracking_number;

-- Step 2: Add new columns
ALTER TABLE deliveries 
ADD COLUMN IF NOT EXISTS delivery_notes TEXT;

ALTER TABLE deliveries 
ADD COLUMN IF NOT EXISTS estimated_delivery TIMESTAMP;

ALTER TABLE deliveries 
ADD COLUMN IF NOT EXISTS actual_delivery TIMESTAMP;

ALTER TABLE deliveries 
ADD COLUMN IF NOT EXISTS driver_name VARCHAR(255);

ALTER TABLE deliveries 
ADD COLUMN IF NOT EXISTS driver_phone VARCHAR(20);

ALTER TABLE deliveries 
ADD COLUMN IF NOT EXISTS pickup_time TIMESTAMP;

ALTER TABLE deliveries 
ADD COLUMN IF NOT EXISTS current_location TEXT;

-- Step 3: Make tracking_number NOT NULL (set default value first if there are existing records)
UPDATE deliveries 
SET tracking_number = 'TRK' || EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT || SUBSTRING(gen_random_uuid()::TEXT, 1, 8)
WHERE tracking_number IS NULL OR tracking_number = '';

ALTER TABLE deliveries 
ALTER COLUMN tracking_number SET NOT NULL;

-- Step 4: Add unique constraint
ALTER TABLE deliveries 
ADD CONSTRAINT unique_tracking_number UNIQUE (tracking_number);

-- Step 5: Create indexes
CREATE INDEX IF NOT EXISTS idx_deliveries_tracking_number ON deliveries(tracking_number);
CREATE INDEX IF NOT EXISTS idx_deliveries_status ON deliveries(status);
CREATE INDEX IF NOT EXISTS idx_deliveries_created_at ON deliveries(created_at);

-- Step 6: Verify changes
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'deliveries'
ORDER BY ordinal_position;

-- Success message
SELECT '✅ Migration completed successfully!' as message;
