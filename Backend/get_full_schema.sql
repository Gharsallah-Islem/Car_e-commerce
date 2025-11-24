-- Get all schema information for key tables
SELECT table_name, column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name IN ('suppliers', 'purchase_orders', 'stock_movements', 'reorder_settings', 'products')
ORDER BY table_name, ordinal_position;
