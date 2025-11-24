-- Complete database analysis - ALL 27 tables
SELECT 
    'admins' as table_name, 
    COUNT(*) as row_count 
FROM admins
UNION ALL
SELECT 'brands', COUNT(*) FROM brands
UNION ALL
SELECT 'cart_items', COUNT(*) FROM cart_items
UNION ALL
SELECT 'carts', COUNT(*) FROM carts
UNION ALL
SELECT 'categories', COUNT(*) FROM categories
UNION ALL
SELECT 'conversations', COUNT(*) FROM conversations
UNION ALL
SELECT 'deliveries', COUNT(*) FROM deliveries
UNION ALL
SELECT 'messages', COUNT(*) FROM messages
UNION ALL
SELECT 'order_items', COUNT(*) FROM order_items
UNION ALL
SELECT 'orders', COUNT(*) FROM orders
UNION ALL
SELECT 'payments', COUNT(*) FROM payments
UNION ALL
SELECT 'product_images', COUNT(*) FROM product_images
UNION ALL
SELECT 'products', COUNT(*) FROM products
UNION ALL
SELECT 'purchase_order_items', COUNT(*) FROM purchase_order_items
UNION ALL
SELECT 'purchase_orders', COUNT(*) FROM purchase_orders
UNION ALL
SELECT 'reclamations', COUNT(*) FROM reclamations
UNION ALL
SELECT 'recommendations', COUNT(*) FROM recommendations
UNION ALL
SELECT 'reorder_settings', COUNT(*) FROM reorder_settings
UNION ALL
SELECT 'reports', COUNT(*) FROM reports
UNION ALL
SELECT 'roles', COUNT(*) FROM roles
UNION ALL
SELECT 'stock_alerts', COUNT(*) FROM stock_alerts
UNION ALL
SELECT 'stock_movements', COUNT(*) FROM stock_movements
UNION ALL
SELECT 'super_admins', COUNT(*) FROM super_admins
UNION ALL
SELECT 'supplier_products', COUNT(*) FROM supplier_products
UNION ALL
SELECT 'suppliers', COUNT(*) FROM suppliers
UNION ALL
SELECT 'users', COUNT(*) FROM users
UNION ALL
SELECT 'vehicles', COUNT(*) FROM vehicles
ORDER BY table_name;
