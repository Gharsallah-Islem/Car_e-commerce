-- Verify current data counts
SELECT 'suppliers' as table_name, COUNT(*) as count FROM suppliers
UNION ALL
SELECT 'purchase_orders', COUNT(*) FROM purchase_orders
UNION ALL
SELECT 'stock_movements', COUNT(*) FROM stock_movements
UNION ALL
SELECT 'reorder_settings', COUNT(*) FROM reorder_settings;
