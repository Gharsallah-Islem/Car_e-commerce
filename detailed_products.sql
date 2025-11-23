-- Get detailed product information
SELECT 
    p.id,
    p.name,
    p.price,
    p.stock,
    b.name as brand_name,
    c.name as category_name,
    p.model,
    p.year,
    p.compatibility
FROM products p
LEFT JOIN brands b ON p.brand_id = b.id
LEFT JOIN categories c ON p.category_id = c.id
ORDER BY p.created_at DESC;

-- Get all brands
SELECT * FROM brands ORDER BY name;

-- Get all categories
SELECT * FROM categories ORDER BY name;

-- Check for any NULL brand_id or category_id in products
SELECT 
    COUNT(*) as total_products,
    COUNT(brand_id) as products_with_brand,
    COUNT(category_id) as products_with_category
FROM products;
