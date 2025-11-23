-- First, let's see what we're working with
SELECT 
    p.id,
    p.name,
    p.brand as brand_varchar,
    b.name as brand_match,
    b.id as brand_id_to_set,
    p.category as category_varchar,
    c.name as category_match,
    c.id as category_id_to_set
FROM products p
LEFT JOIN brands b ON LOWER(TRIM(p.brand)) = LOWER(TRIM(b.name))
LEFT JOIN categories c ON LOWER(TRIM(p.category)) = LOWER(TRIM(c.name))
ORDER BY p.name;

-- Update brand_id based on brand varchar field
UPDATE products 
SET brand_id = (
    SELECT b.id 
    FROM brands b 
    WHERE LOWER(TRIM(b.name)) = LOWER(TRIM(products.brand))
    LIMIT 1
)
WHERE brand IS NOT NULL 
  AND brand != '';

-- Update category_id based on category varchar field
UPDATE products 
SET category_id = (
    SELECT c.id 
    FROM categories c 
    WHERE LOWER(TRIM(c.name)) = LOWER(TRIM(products.category))
    LIMIT 1
)
WHERE category IS NOT NULL 
  AND category != '';

-- Verify the updates
SELECT 
    COUNT(*) as total_products,
    COUNT(brand_id) as products_with_brand_fk,
    COUNT(category_id) as products_with_category_fk,
    COUNT(brand) as products_with_brand_varchar,
    COUNT(category) as products_with_category_varchar
FROM products;

-- Show sample results
SELECT 
    p.name,
    p.brand as brand_varchar,
    b.name as brand_fk_name,
    p.category as category_varchar,
    c.name as category_fk_name
FROM products p
LEFT JOIN brands b ON p.brand_id = b.id
LEFT JOIN categories c ON p.category_id = c.id
LIMIT 10;
