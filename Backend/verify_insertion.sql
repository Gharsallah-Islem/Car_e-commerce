-- Verify the insertion was successful
SELECT 
    'suppliers' as table_name, 
    COUNT(*) as current_count
FROM suppliers
UNION ALL
SELECT 'purchase_orders', COUNT(*) FROM purchase_orders
UNION ALL
SELECT 'purchase_order_items', COUNT(*) FROM purchase_order_items
UNION ALL
SELECT 'stock_movements', COUNT(*) FROM stock_movements
UNION ALL
SELECT 'reorder_settings', COUNT(*) FROM reorder_settings
ORDER BY table_name;
