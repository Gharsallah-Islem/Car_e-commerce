-- ============================================================================
-- INVENTORY DATA INSERTION SCRIPT (CORRECTED)
-- ============================================================================
-- This script populates the inventory management tables with sample data
-- Uses gen_random_uuid() for UUID generation
-- ============================================================================

-- Enable UUID extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================================
-- 1. SUPPLIERS (10 records)
-- ============================================================================

INSERT INTO suppliers (id, name, company_name, email, phone, address, city, postal_code, country, contact_person, tax_id, payment_terms, notes, is_active, rating, website, created_at, updated_at)
VALUES
(gen_random_uuid(), 'Bosch Tunisia', 'Robert Bosch S.A.R.L', 'contact@bosch.tn', '+216 71 123 456', 'Zone Industrielle Charguia 2', 'Tunis', '2035', 'Tunisia', 'Ahmed Ben Ali', 'TN-1234567-A', 'Net 30', 'Premium automotive parts supplier - OEM quality', true, 4.8, 'www.bosch.tn', NOW() - INTERVAL '6 months', NOW()),
(gen_random_uuid(), 'Mann Filter Tunisia', 'Mann+Hummel Tunisia SARL', 'sales@mannfilter.tn', '+216 71 234 567', 'Rue de l''Industrie, Z.I. Mghira', 'Tunis', '2082', 'Tunisia', 'Fatma Trabelsi', 'TN-2345678-B', 'Net 45', 'Specialized in filters and air intake systems', true, 4.6, 'www.mannfilter.tn', NOW() - INTERVAL '5 months', NOW()),
(gen_random_uuid(), 'Brembo Tunisia', 'Brembo North Africa', 'info@brembo.tn', '+216 71 345 678', 'Avenue de la Liberté, Z.I. Ben Arous', 'Ben Arous', '2013', 'Tunisia', 'Mohamed Gharbi', 'TN-3456789-C', 'Net 30', 'High-performance brake systems and components', true, 4.9, 'www.brembo.com', NOW() - INTERVAL '8 months', NOW()),
(gen_random_uuid(), 'NGK Tunisia', 'NGK Spark Plugs Tunisia', 'sales@ngk.tn', '+216 71 456 789', 'Route de Bizerte Km 12', 'Ariana', '2080', 'Tunisia', 'Sami Jebali', 'TN-4567890-D', 'Net 30', 'Leading spark plug and ignition system manufacturer', true, 4.7, 'www.ngk.com', NOW() - INTERVAL '4 months', NOW()),
(gen_random_uuid(), 'Valeo Tunisia', 'Valeo Systèmes Thermiques', 'contact@valeo.tn', '+216 71 567 890', 'Z.I. Mghira 2', 'Tunis', '2082', 'Tunisia', 'Leila Mansouri', 'TN-5678901-E', 'Net 45', 'Thermal systems, lighting, and electrical components', true, 4.5, 'www.valeo.com', NOW() - INTERVAL '7 months', NOW()),
(gen_random_uuid(), 'Continental Tunisia', 'Continental Automotive Tunisia', 'info@continental.tn', '+216 71 678 901', 'Avenue Habib Bourguiba', 'Sousse', '4000', 'Tunisia', 'Karim Belaid', 'TN-6789012-F', 'Net 30', 'Tires, brake systems, and automotive electronics', true, 4.6, 'www.continental.com', NOW() - INTERVAL '3 months', NOW()),
(gen_random_uuid(), 'Mahle Tunisia', 'Mahle Filter Systems Tunisia', 'sales@mahle.tn', '+216 71 789 012', 'Z.I. Megrine', 'Ben Arous', '2014', 'Tunisia', 'Nadia Khelifi', 'TN-7890123-G', 'Net 45', 'Engine components and filtration systems', true, 4.4, 'www.mahle.com', NOW() - INTERVAL '6 months', NOW()),
(gen_random_uuid(), 'Denso Tunisia', 'Denso Manufacturing Tunisia', 'contact@denso.tn', '+216 71 890 123', 'Route de Sfax Km 5', 'Sfax', '3000', 'Tunisia', 'Youssef Hamdi', 'TN-8901234-H', 'Net 30', 'Advanced automotive technology and components', true, 4.7, 'www.denso.com', NOW() - INTERVAL '5 months', NOW()),
(gen_random_uuid(), 'ZF Tunisia', 'ZF Friedrichshafen Tunisia', 'info@zf.tn', '+216 71 901 234', 'Z.I. Charguia 1', 'Tunis', '2035', 'Tunisia', 'Rania Sassi', 'TN-9012345-I', 'Net 45', 'Transmission systems and chassis components', true, 4.5, 'www.zf.com', NOW() - INTERVAL '4 months', NOW()),
(gen_random_uuid(), 'Schaeffler Tunisia', 'Schaeffler Automotive Tunisia', 'sales@schaeffler.tn', '+216 71 012 345', 'Avenue de la République', 'Bizerte', '7000', 'Tunisia', 'Hichem Bouazizi', 'TN-0123456-J', 'Net 30', 'Bearings, timing systems, and engine components', true, 4.6, 'www.schaeffler.com', NOW() - INTERVAL '7 months', NOW());

-- ============================================================================
-- 2. PURCHASE ORDERS (15 records)
-- ============================================================================
-- Note: We'll use the first supplier for simplicity

DO $$
DECLARE
    supplier1_id uuid;
    supplier2_id uuid;
    supplier3_id uuid;
BEGIN
    -- Get first 3 supplier IDs
    SELECT id INTO supplier1_id FROM suppliers ORDER BY created_at LIMIT 1;
    SELECT id INTO supplier2_id FROM suppliers ORDER BY created_at LIMIT 1 OFFSET 1;
    SELECT id INTO supplier3_id FROM suppliers ORDER BY created_at LIMIT 1 OFFSET 2;

    -- Insert purchase orders
    INSERT INTO purchase_orders (id, po_number, supplier_id, order_date, expected_delivery_date, actual_delivery_date, status, total_amount, tax_amount, shipping_cost, discount_amount, grand_total, notes, created_by, approved_by, received_by, created_at, updated_at)
    VALUES
    (gen_random_uuid(), 'PO-2024-001', supplier1_id, '2024-09-01', '2024-09-15', '2024-09-14', 'RECEIVED', 4599.00, 873.81, 150.00, 0.00, 5622.81, 'Initial stock order for spark plugs - 100 units', 'admin@example.com', 'admin@example.com', 'warehouse@example.com', '2024-09-01 10:00:00', '2024-09-14 14:30:00'),
    (gen_random_uuid(), 'PO-2024-002', supplier2_id, '2024-09-05', '2024-09-20', '2024-09-19', 'RECEIVED', 2500.00, 475.00, 100.00, 125.00, 2950.00, 'Bulk order - Engine oil filters (200 units)', 'admin@example.com', 'admin@example.com', 'warehouse@example.com', '2024-09-05 09:00:00', '2024-09-19 11:00:00'),
    (gen_random_uuid(), 'PO-2024-003', supplier3_id, '2024-09-10', '2024-09-25', '2024-09-24', 'RECEIVED', 11460.00, 2177.40, 200.00, 573.00, 13264.40, 'Premium ceramic brake pads - 120 sets', 'admin@example.com', 'admin@example.com', 'warehouse@example.com', '2024-09-10 08:30:00', '2024-09-24 15:00:00'),
    (gen_random_uuid(), 'PO-2024-004', supplier1_id, '2024-10-15', '2024-11-01', NULL, 'APPROVED', 3281.25, 623.44, 120.00, 0.00, 4024.69, 'Engine air filters - 175 units', 'admin@example.com', 'admin@example.com', NULL, '2024-10-15 10:00:00', '2024-10-16 09:00:00'),
    (gen_random_uuid(), 'PO-2024-005', supplier2_id, '2024-11-01', '2024-11-20', NULL, 'PENDING', 7125.00, 1353.75, 180.00, 356.25, 8302.50, 'Timing belt kits - 57 units', 'admin@example.com', NULL, NULL, '2024-11-01 11:00:00', '2024-11-01 11:00:00'),
    (gen_random_uuid(), 'PO-2024-006', supplier3_id, '2024-09-20', '2024-10-05', '2024-10-04', 'RECEIVED', 8925.00, 1695.75, 175.00, 0.00, 10795.75, 'Brake discs (front) - 75 pairs', 'admin@example.com', 'admin@example.com', 'warehouse@example.com', '2024-09-20 14:00:00', '2024-10-04 16:00:00'),
    (gen_random_uuid(), 'PO-2024-007', supplier1_id, '2024-10-25', '2024-11-10', NULL, 'APPROVED', 5940.00, 1128.60, 150.00, 297.00, 6921.60, 'Fuel injectors - 45 units', 'admin@example.com', 'admin@example.com', NULL, '2024-10-25 09:30:00', '2024-10-26 10:00:00'),
    (gen_random_uuid(), 'PO-2024-008', supplier2_id, '2024-11-05', '2024-11-25', NULL, 'PENDING', 4200.00, 798.00, 130.00, 0.00, 5128.00, 'Synthetic engine oil 5W-30 - 100 bottles', 'admin@example.com', NULL, NULL, '2024-11-05 13:00:00', '2024-11-05 13:00:00'),
    (gen_random_uuid(), 'PO-2024-009', supplier3_id, '2024-10-01', '2024-10-15', '2024-10-14', 'RECEIVED', 3680.00, 699.20, 110.00, 184.00, 4305.20, 'Platinum spark plugs - 80 sets', 'admin@example.com', 'admin@example.com', 'warehouse@example.com', '2024-10-01 08:00:00', '2024-10-14 12:00:00'),
    (gen_random_uuid(), 'PO-2024-010', supplier1_id, '2024-11-08', '2024-11-28', NULL, 'APPROVED', 2850.00, 541.50, 95.00, 0.00, 3486.50, 'Automatic transmission filters - 60 units', 'admin@example.com', 'admin@example.com', NULL, '2024-11-08 10:30:00', '2024-11-09 09:00:00'),
    (gen_random_uuid(), 'PO-2024-011', supplier2_id, '2024-11-15', '2024-12-05', NULL, 'DRAFT', 6720.00, 1276.80, 160.00, 336.00, 7820.80, 'Wheel bearing assemblies - 80 units', 'admin@example.com', NULL, NULL, '2024-11-15 15:00:00', '2024-11-15 15:00:00'),
    (gen_random_uuid(), 'PO-2024-012', supplier3_id, '2024-10-10', '2024-10-25', NULL, 'CANCELLED', 1575.00, 299.25, 75.00, 0.00, 1949.25, 'Cancelled due to supplier stock issues', 'admin@example.com', NULL, NULL, '2024-10-10 11:00:00', '2024-10-12 14:00:00'),
    (gen_random_uuid(), 'PO-2024-013', supplier1_id, '2024-09-25', '2024-10-10', '2024-10-09', 'RECEIVED', 9450.00, 1795.50, 190.00, 472.50, 10963.00, 'Alternators - 30 units', 'admin@example.com', 'admin@example.com', 'warehouse@example.com', '2024-09-25 09:00:00', '2024-10-09 13:30:00'),
    (gen_random_uuid(), 'PO-2024-014', supplier2_id, '2024-11-10', '2024-11-30', NULL, 'PENDING', 15750.00, 2992.50, 250.00, 0.00, 18992.50, 'All-season tires 205/55R16 - 50 units', 'admin@example.com', NULL, NULL, '2024-11-10 14:00:00', '2024-11-10 14:00:00'),
    (gen_random_uuid(), 'PO-2024-015', supplier3_id, '2024-11-12', '2024-11-27', NULL, 'APPROVED', 12600.00, 2394.00, 210.00, 630.00, 14574.00, 'High-performance brake calipers - 40 sets', 'admin@example.com', 'admin@example.com', NULL, '2024-11-12 10:00:00', '2024-11-13 11:00:00');
END $$;

-- ============================================================================
-- 3. STOCK MOVEMENTS (12 records)
-- ============================================================================

DO $$
DECLARE
    product1_id uuid := 'a1111111-1111-1111-1111-111111111111'; -- Spark Plugs
    product2_id uuid := 'a1111111-1111-1111-1111-111111111112'; -- Oil Filter
    product3_id uuid := 'a2222222-2222-2222-2222-222222222221'; -- Brake Pads
    product4_id uuid := 'a1111111-1111-1111-1111-111111111113'; -- Air Filter
    product5_id uuid := 'a1111111-1111-1111-1111-111111111115'; -- Timing Belt
    po1_id uuid;
    po2_id uuid;
    po3_id uuid;
    order1_id uuid := 'e1111111-1111-1111-1111-111111111111';
    order2_id uuid := 'e1111111-1111-1111-1111-111111111112';
BEGIN
    -- Get some PO IDs
    SELECT id INTO po1_id FROM purchase_orders WHERE po_number = 'PO-2024-001';
    SELECT id INTO po2_id FROM purchase_orders WHERE po_number = 'PO-2024-002';
    SELECT id INTO po3_id FROM purchase_orders WHERE po_number = 'PO-2024-003';

    -- Insert stock movements
    INSERT INTO stock_movements (id, product_id, movement_type, quantity, previous_stock, new_stock, reference_id, reference_type, notes, performed_by, movement_date)
    VALUES
    (gen_random_uuid(), product1_id, 'PURCHASE', 100, 48, 148, po1_id, 'PURCHASE_ORDER', 'Received from PO-2024-001 - Bosch supplier', 'warehouse@example.com', '2024-09-14 14:30:00'),
    (gen_random_uuid(), product2_id, 'PURCHASE', 200, 0, 200, po2_id, 'PURCHASE_ORDER', 'Received from PO-2024-002 - Mann Filter supplier', 'warehouse@example.com', '2024-09-19 11:00:00'),
    (gen_random_uuid(), product3_id, 'PURCHASE', 120, 0, 120, po3_id, 'PURCHASE_ORDER', 'Received from PO-2024-003 - Brembo supplier', 'warehouse@example.com', '2024-09-24 15:00:00'),
    (gen_random_uuid(), product1_id, 'SALE', 2, 150, 148, order1_id, 'ORDER', 'Sold via order TRK-2024-001', 'system', '2024-10-04 10:00:00'),
    (gen_random_uuid(), product3_id, 'SALE', 2, 120, 118, order2_id, 'ORDER', 'Sold via order TRK-2024-002', 'system', '2024-10-14 11:00:00'),
    (gen_random_uuid(), product4_id, 'PURCHASE', 175, 0, 175, po1_id, 'PURCHASE_ORDER', 'Received from PO-2024-009', 'warehouse@example.com', '2024-10-14 12:00:00'),
    (gen_random_uuid(), product4_id, 'SALE', 1, 175, 174, order1_id, 'ORDER', 'Sold via order TRK-2024-003', 'system', '2024-10-29 09:00:00'),
    (gen_random_uuid(), product5_id, 'PURCHASE', 60, 0, 60, po1_id, 'PURCHASE_ORDER', 'Received from PO-2024-013', 'warehouse@example.com', '2024-10-09 13:30:00'),
    (gen_random_uuid(), product5_id, 'SALE', 3, 60, 57, order1_id, 'ORDER', 'Sold via order TRK-2024-004', 'system', '2024-11-01 10:30:00'),
    (gen_random_uuid(), product2_id, 'ADJUSTMENT', 5, 195, 200, NULL, 'MANUAL', 'Inventory count correction - found 5 additional units', 'admin@example.com', '2024-10-20 16:00:00'),
    (gen_random_uuid(), product3_id, 'DAMAGED', 2, 120, 118, NULL, 'MANUAL', 'Damaged during handling - written off', 'warehouse@example.com', '2024-10-15 14:00:00'),
    (gen_random_uuid(), product1_id, 'RETURN_FROM_CUSTOMER', 1, 147, 148, order1_id, 'ORDER', 'Customer return - wrong fitment', 'customer_service@example.com', '2024-10-12 11:00:00');
END $$;

-- ============================================================================
-- 4. REORDER SETTINGS (5 records)
-- ============================================================================

DO $$
DECLARE
    product1_id uuid := 'a1111111-1111-1111-1111-111111111111'; -- Spark Plugs
    product2_id uuid := 'a1111111-1111-1111-1111-111111111112'; -- Oil Filter
    product3_id uuid := 'a2222222-2222-2222-2222-222222222221'; -- Brake Pads
    product4_id uuid := 'a1111111-1111-1111-1111-111111111113'; -- Air Filter
    product5_id uuid := 'a1111111-1111-1111-1111-111111111115'; -- Timing Belt
    supplier1_id uuid;
    supplier2_id uuid;
    supplier3_id uuid;
BEGIN
    -- Get supplier IDs
    SELECT id INTO supplier1_id FROM suppliers ORDER BY created_at LIMIT 1;
    SELECT id INTO supplier2_id FROM suppliers ORDER BY created_at LIMIT 1 OFFSET 1;
    SELECT id INTO supplier3_id FROM suppliers ORDER BY created_at LIMIT 1 OFFSET 2;

    -- Insert reorder settings
    INSERT INTO reorder_settings (id, product_id, reorder_point, reorder_quantity, minimum_stock, maximum_stock, lead_time_days, auto_reorder, is_enabled, preferred_supplier_id, notes, last_alert_sent, last_reorder_date, created_at, updated_at)
    VALUES
    (gen_random_uuid(), product1_id, 50, 100, 30, 200, 14, true, true, supplier1_id, 'High-demand item - maintain adequate stock', NULL, '2024-09-01', NOW() - INTERVAL '2 months', NOW()),
    (gen_random_uuid(), product2_id, 75, 150, 50, 300, 10, true, true, supplier2_id, 'Fast-moving consumable - auto-reorder enabled', NULL, '2024-09-05', NOW() - INTERVAL '2 months', NOW()),
    (gen_random_uuid(), product3_id, 40, 80, 25, 150, 15, true, true, supplier3_id, 'Premium product - maintain stock for customer demand', NULL, '2024-09-10', NOW() - INTERVAL '2 months', NOW()),
    (gen_random_uuid(), product4_id, 60, 120, 40, 250, 12, true, true, supplier2_id, 'Regular maintenance item - high turnover', NULL, '2024-10-14', NOW() - INTERVAL '1 month', NOW()),
    (gen_random_uuid(), product5_id, 30, 60, 20, 100, 20, false, true, supplier1_id, 'Seasonal demand - manual reorder preferred', NULL, NULL, NOW() - INTERVAL '1 month', NOW());
END $$;

-- ============================================================================
-- VERIFICATION
-- ============================================================================

SELECT 'Suppliers' as table_name, COUNT(*) as count FROM suppliers
UNION ALL
SELECT 'Purchase Orders', COUNT(*) FROM purchase_orders
UNION ALL
SELECT 'Stock Movements', COUNT(*) FROM stock_movements
UNION ALL
SELECT 'Reorder Settings', COUNT(*) FROM reorder_settings;
