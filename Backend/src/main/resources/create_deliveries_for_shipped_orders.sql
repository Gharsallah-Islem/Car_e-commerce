-- Create deliveries for existing SHIPPED orders that don't have a delivery record
-- Run this once to fix existing orders

INSERT INTO deliveries (id, tracking_number, status, order_id, address, created_at, updated_at, estimated_delivery)
SELECT 
    gen_random_uuid(),
    COALESCE(o.tracking_number, 'TRK-' || EXTRACT(EPOCH FROM NOW())::BIGINT || '-' || LEFT(o.id::text, 8)),
    'PROCESSING',
    o.id,
    o.delivery_address,
    NOW(),
    NOW(),
    NOW() + INTERVAL '3 days'
FROM orders o
WHERE o.status = 'SHIPPED' 
  AND NOT EXISTS (SELECT 1 FROM deliveries d WHERE d.order_id = o.id);

-- Show results
SELECT 
    o.id as order_id, 
    o.status as order_status, 
    d.tracking_number,
    d.status as delivery_status
FROM orders o
LEFT JOIN deliveries d ON d.order_id = o.id
WHERE o.status = 'SHIPPED';
