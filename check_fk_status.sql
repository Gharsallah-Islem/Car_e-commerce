-- Check current state of products with FK relationships
SELECT 
    COUNT(*) as total_products,
    COUNT(brand_id) as has_brand_fk,
    COUNT(category_id) as has_category_fk,
    COUNT(CASE WHEN brand_id IS NULL AND brand IS NOT NULL THEN 1 END) as missing_brand_fk,
    COUNT(CASE WHEN category_id IS NULL AND category IS NOT NULL THEN 1 END) as missing_category_fk
FROM products;

-- Show products that still don't have brand_id
SELECT 
    p.name,
    p.brand as brand_varchar,
    p.brand_id,
    b.name as matched_brand
FROM products p
LEFT JOIN brands b ON LOWER(TRIM(p.brand)) = LOWER(TRIM(b.name))
WHERE p.brand_id IS NULL AND p.brand IS NOT NULL;

-- Show products that still don't have category_id
SELECT 
    p.name,
    p.category as category_varchar,
    p.category_id,
    c.name as matched_category
FROM products p
LEFT JOIN categories c ON LOWER(TRIM(p.category)) = LOWER(TRIM(c.name))
WHERE p.category_id IS NULL AND p.category IS NOT NULL;

-- Show all brands for reference
SELECT id, name FROM brands ORDER BY name;

-- Show all categories for reference
SELECT id, name FROM categories ORDER BY name;
