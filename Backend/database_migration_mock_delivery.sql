-- ====================================================================
-- DELIVERY SYSTEM MIGRATION TO MOCK SYSTEM
-- Remove ONdelivery dependencies and add internal tracking fields
-- Database: ecommercespareparts
-- Date: October 17, 2025
-- ====================================================================

-- Connect to database
\c ecommercespareparts;

BEGIN;

-- ====================================================================
-- STEP 1: Remove ONdelivery external references
-- ====================================================================

ALTER TABLE deliveries 
DROP COLUMN IF EXISTS ondelivery_tracking_id;

COMMENT ON TABLE deliveries IS 'Internal delivery tracking system (mock implementation)';

-- ====================================================================
-- STEP 2: Add internal tracking fields (if not already present)
-- ====================================================================

-- Add pickup_time (when driver picked up the package)
ALTER TABLE deliveries
ADD COLUMN IF NOT EXISTS pickup_time TIMESTAMP;

-- Add current_location (textual description of current location)
ALTER TABLE deliveries
ADD COLUMN IF NOT EXISTS current_location TEXT;

-- Ensure tracking_number exists (should already be there)
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name='deliveries' AND column_name='tracking_number'
    ) THEN
        ALTER TABLE deliveries ADD COLUMN tracking_number VARCHAR(255);
    END IF;
END $$;

-- Ensure delivery_notes exists (should already be there)
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name='deliveries' AND column_name='delivery_notes'
    ) THEN
        ALTER TABLE deliveries ADD COLUMN delivery_notes TEXT;
    END IF;
END $$;

-- Ensure estimated_delivery exists (should already be there)
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name='deliveries' AND column_name='estimated_delivery'
    ) THEN
        ALTER TABLE deliveries ADD COLUMN estimated_delivery TIMESTAMP;
    END IF;
END $$;

-- Ensure actual_delivery exists (should already be there)
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name='deliveries' AND column_name='actual_delivery'
    ) THEN
        ALTER TABLE deliveries ADD COLUMN actual_delivery TIMESTAMP;
    END IF;
END $$;

-- Ensure driver_name exists (should already be there)
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name='deliveries' AND column_name='driver_name'
    ) THEN
        ALTER TABLE deliveries ADD COLUMN driver_name VARCHAR(255);
    END IF;
END $$;

-- Ensure driver_phone exists (should already be there)
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name='deliveries' AND column_name='driver_phone'
    ) THEN
        ALTER TABLE deliveries ADD COLUMN driver_phone VARCHAR(20);
    END IF;
END $$;

-- ====================================================================
-- STEP 3: Add constraints
-- ====================================================================

-- Make tracking_number NOT NULL and UNIQUE
ALTER TABLE deliveries 
ALTER COLUMN tracking_number SET NOT NULL;

-- Add unique constraint (drop first if exists to avoid errors)
ALTER TABLE deliveries 
DROP CONSTRAINT IF EXISTS unique_tracking_number;

ALTER TABLE deliveries
ADD CONSTRAINT unique_tracking_number UNIQUE (tracking_number);

-- ====================================================================
-- STEP 4: Create/Update indexes for performance
-- ====================================================================

-- Index on tracking_number for fast lookup
DROP INDEX IF EXISTS idx_deliveries_tracking_number;
CREATE INDEX idx_deliveries_tracking_number ON deliveries(tracking_number);

-- Index on status for filtering
DROP INDEX IF EXISTS idx_deliveries_status;
CREATE INDEX idx_deliveries_status ON deliveries(status);

-- Index on order_id (should already exist)
DROP INDEX IF EXISTS idx_deliveries_order_id;
CREATE INDEX idx_deliveries_order_id ON deliveries(order_id);

-- Index on created_at for date range queries
DROP INDEX IF EXISTS idx_deliveries_created_at;
CREATE INDEX idx_deliveries_created_at ON deliveries(created_at);

-- ====================================================================
-- STEP 5: Update existing data (if any)
-- ====================================================================

-- Update status values to new standard (if needed)
UPDATE deliveries SET status = 'PROCESSING' WHERE status = 'PENDING';

-- Generate tracking numbers for any existing deliveries without them
UPDATE deliveries 
SET tracking_number = 'TRK' || EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT || SUBSTRING(gen_random_uuid()::TEXT, 1, 8)
WHERE tracking_number IS NULL OR tracking_number = '';

-- Set estimated delivery for any existing deliveries
UPDATE deliveries 
SET estimated_delivery = created_at + INTERVAL '3 days'
WHERE estimated_delivery IS NULL;

-- ====================================================================
-- STEP 6: Add comments for documentation
-- ====================================================================

COMMENT ON COLUMN deliveries.tracking_number IS 'Internal tracking number generated by system';
COMMENT ON COLUMN deliveries.status IS 'PROCESSING, PICKED_UP, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, FAILED, CANCELLED';
COMMENT ON COLUMN deliveries.pickup_time IS 'Timestamp when driver picked up the package';
COMMENT ON COLUMN deliveries.current_location IS 'Current location description for tracking';
COMMENT ON COLUMN deliveries.driver_name IS 'Assigned driver name (mock data)';
COMMENT ON COLUMN deliveries.driver_phone IS 'Assigned driver phone (mock data)';
COMMENT ON COLUMN deliveries.estimated_delivery IS 'Estimated delivery timestamp';
COMMENT ON COLUMN deliveries.actual_delivery IS 'Actual delivery timestamp (when delivered)';

-- ====================================================================
-- STEP 7: Verify changes
-- ====================================================================

-- Display table structure
\d deliveries;

-- Show sample data (if any)
SELECT 
    id,
    tracking_number,
    status,
    driver_name,
    estimated_delivery,
    actual_delivery,
    created_at
FROM deliveries
LIMIT 5;

-- Show indexes
SELECT 
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'deliveries';

COMMIT;

-- ====================================================================
-- ROLLBACK (in case of errors - uncomment if needed)
-- ====================================================================
-- ROLLBACK;

-- ====================================================================
-- SUCCESS MESSAGE
-- ====================================================================
\echo '✅ Migration completed successfully!'
\echo '✅ ONdelivery references removed'
\echo '✅ Internal tracking system configured'
\echo '✅ Indexes created'
\echo ''
\echo 'Next steps:'
\echo '1. Update Java code (see DELIVERY_MOCK_SYSTEM.md)'
\echo '2. Run: mvn clean compile'
\echo '3. Test delivery endpoints'
\echo ''
