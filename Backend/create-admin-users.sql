-- ============================================================================
-- ADMIN USER SETUP SCRIPT
-- ============================================================================
-- This script creates admin users for the Car Parts E-commerce Application
-- Run this in pgAdmin or your PostgreSQL client
-- ============================================================================

-- 1. Ensure roles exist (should already be created by data.sql)
INSERT INTO roles (name) VALUES ('CLIENT') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('SUPPORT') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ADMIN') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('SUPER_ADMIN') ON CONFLICT (name) DO NOTHING;

-- 2. Create ADMIN user
-- Email: admin@carparts.com
-- Password: admin123
-- BCrypt hash for "admin123": $2a$10$N9qo8uLOickgx2ZMRZoMye7FRNJheRYICjr3Cujna2TlN0e4h.tPu
INSERT INTO users (
    id,
    username,
    email,
    password,
    full_name,
    phone,
    address,
    role_id,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'admin',
    'admin@carparts.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7FRNJheRYICjr3Cujna2TlN0e4h.tPu',
    'System Administrator',
    '+216 70 123 456',
    '123 Admin Street, Tunis, Tunisia',
    (SELECT id FROM roles WHERE name = 'ADMIN'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- 3. Create SUPER_ADMIN user
-- Email: superadmin@carparts.com
-- Password: super123
-- BCrypt hash for "super123": $2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRps.9cGLcZEiGDMVr5yUY1Q8flaa
INSERT INTO users (
    id,
    username,
    email,
    password,
    full_name,
    phone,
    address,
    role_id,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'superadmin',
    'superadmin@carparts.com',
    '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRps.9cGLcZEiGDMVr5yUY1Q8flaa',
    'Super Administrator',
    '+216 70 123 457',
    '124 Admin Street, Tunis, Tunisia',
    (SELECT id FROM roles WHERE name = 'SUPER_ADMIN'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- 4. Create SUPPORT user
-- Email: support@carparts.com
-- Password: support123
-- BCrypt hash for "support123": $2a$10$xz.JgHr.LNE.aMgZpPnTjOZcgRvL6UDnUVdm0xW3tJXKdZ9K4Xg5K
INSERT INTO users (
    id,
    username,
    email,
    password,
    full_name,
    phone,
    address,
    role_id,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'support',
    'support@carparts.com',
    '$2a$10$xz.JgHr.LNE.aMgZpPnTjOZcgRvL6UDnUVdm0xW3tJXKdZ9K4Xg5K',
    'Support Team',
    '+216 70 123 458',
    '125 Support Street, Tunis, Tunisia',
    (SELECT id FROM roles WHERE name = 'SUPPORT'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================

-- Verify all roles exist
SELECT * FROM roles ORDER BY id;

-- Verify admin users were created
SELECT 
    u.id,
    u.username,
    u.email,
    u.full_name,
    r.name as role,
    u.created_at
FROM users u
JOIN roles r ON u.role_id = r.id
WHERE r.name IN ('ADMIN', 'SUPER_ADMIN', 'SUPPORT')
ORDER BY r.name, u.username;

-- Count users by role
SELECT 
    r.name as role,
    COUNT(u.id) as user_count
FROM roles r
LEFT JOIN users u ON r.id = u.role_id
GROUP BY r.name
ORDER BY r.name;

-- ============================================================================
-- LOGIN CREDENTIALS
-- ============================================================================
/*
ADMIN USER:
-----------
Email: admin@carparts.com
Password: admin123
Access Level: ADMIN
URL: http://localhost:4200/admin

SUPER ADMIN USER:
-----------------
Email: superadmin@carparts.com
Password: super123
Access Level: SUPER_ADMIN
URL: http://localhost:4200/admin

SUPPORT USER:
-------------
Email: support@carparts.com
Password: support123
Access Level: SUPPORT
URL: http://localhost:4200/chat (for customer support)

NOTES:
------
1. All passwords are BCrypt hashed for security
2. Change passwords after first login
3. Admin and Super Admin can access the admin dashboard
4. Support can access chat/support features
5. Use ON CONFLICT DO NOTHING to avoid duplicate errors if users already exist
*/
