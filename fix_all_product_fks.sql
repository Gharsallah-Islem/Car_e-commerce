-- Insert missing brands (parts manufacturers)
-- Products have: ACDelco, ATE, Bosal, Bosch, Brembo, Castrol, Denso, Gates, K&N, KYB, Magnaflow, Mann-Filter, Mishimoto, Mobil 1, Monroe, Moog, Motorcraft, Osram, Philips, Textar, TRW, Valeo, Wagner, Walker, Wix
-- Existing brands: Bosch, Brembo, Continental, Hella, Mann-Filter, NGK, Sachs, Valeo

INSERT INTO brands (name, description) VALUES
('ACDelco', 'American automotive parts manufacturer'),
('ATE', 'Brake systems manufacturer'),
('Bosal', 'Exhaust systems manufacturer'),
('Castrol', 'Motor oil and lubricants'),
('Denso', 'Japanese automotive parts manufacturer'),
('Gates', 'Belts and hoses manufacturer'),
('K&N', 'Air filters and intake systems'),
('KYB', 'Shock absorbers and suspension'),
('Magnaflow', 'Exhaust systems'),
('Mishimoto', 'Cooling systems'),
('Mobil 1', 'Synthetic motor oil'),
('Monroe', 'Shock absorbers'),
('Moog', 'Steering and suspension parts'),
('Motorcraft', 'Ford OEM parts'),
('Osram', 'Automotive lighting'),
('Philips', 'Automotive lighting'),
('Textar', 'Brake systems'),
('TRW', 'Automotive safety systems'),
('Wagner', 'Brake parts'),
('Walker', 'Exhaust systems'),
('Wix', 'Filters')
ON CONFLICT (name) DO NOTHING;

-- Insert missing categories
-- Products have: Brakes, Cooling, Electrical, Engine, Exhaust, Filters, Fluids, Lighting, Steering, Suspension
-- Existing categories (French): Carrosserie, Éclairage, Électrique, Filtration, Freinage, Moteur, Suspension, Transmission

INSERT INTO categories (name, description) VALUES
('Brakes', 'Brake systems and components'),
('Cooling', 'Cooling systems and radiators'),
('Electrical', 'Electrical components and systems'),
('Engine', 'Engine parts and components'),
('Exhaust', 'Exhaust systems and components'),
('Filters', 'Air, oil, and fuel filters'),
('Fluids', 'Motor oil, brake fluid, coolant'),
('Lighting', 'Headlights, tail lights, bulbs'),
('Steering', 'Steering systems and components'),
('Suspension', 'Suspension systems and shocks')
ON CONFLICT (name) DO NOTHING;

-- Now update products with brand_id
UPDATE products 
SET brand_id = (
    SELECT b.id 
    FROM brands b 
    WHERE LOWER(TRIM(b.name)) = LOWER(TRIM(products.brand))
    LIMIT 1
)
WHERE brand IS NOT NULL 
  AND brand != ''
  AND brand_id IS NULL;

-- Update products with category_id
UPDATE products 
SET category_id = (
    SELECT c.id 
    FROM categories c 
    WHERE LOWER(TRIM(c.name)) = LOWER(TRIM(products.category))
    LIMIT 1
)
WHERE category IS NOT NULL 
  AND category != ''
  AND category_id IS NULL;

-- Verify results
SELECT 
    COUNT(*) as total_products,
    COUNT(brand_id) as products_with_brand_fk,
    COUNT(category_id) as products_with_category_fk,
    COUNT(CASE WHEN brand_id IS NULL AND brand IS NOT NULL THEN 1 END) as still_missing_brand_fk,
    COUNT(CASE WHEN category_id IS NULL AND category IS NOT NULL THEN 1 END) as still_missing_category_fk
FROM products;

-- Show sample of updated products
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
