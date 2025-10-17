-- Manual schema fix for PostgreSQL database
-- Run this if you encounter "function lower(bytea) does not exist" error

-- Fix product description and compatibility columns
ALTER TABLE products ALTER COLUMN description TYPE TEXT USING description::TEXT;
ALTER TABLE products ALTER COLUMN compatibility TYPE TEXT USING compatibility::TEXT;

-- Verify the fix
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'products' 
  AND column_name IN ('description', 'compatibility');
