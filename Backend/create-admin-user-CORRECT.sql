-- =====================================================
-- CREATE ADMIN USER - CORRECT VERSION
-- =====================================================
-- This script creates an admin user in the USERS table
-- (NOT in the admins table, since authentication uses users table)
--
-- Database: ecommercespareparts
-- Table: users (with role_id foreign key to roles table)
-- Password: admin123 (BCrypt hashed)
-- =====================================================

-- First, verify roles exist (these should already be in your database)
-- If not, uncomment the following INSERT statements:

/*
INSERT INTO roles (id, name) VALUES 
    (1, 'CLIENT'),
    (2, 'SUPPORT'),
    (3, 'ADMIN'),
    (4, 'SUPER_ADMIN')
ON CONFLICT (id) DO NOTHING;
*/

-- =====================================================
-- OPTION 1: Create ADMIN user (recommended)
-- =====================================================
INSERT INTO users (
    id, 
    username, 
    email, 
    password, 
    full_name, 
    address, 
    phone, 
    role_id,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'admin',
    'admin@carparts.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7FRNJheRYICjr3Cujna2TlN0e4h.tPu', -- Password: admin123
    'System Administrator',
    'Admin Office',
    '+1234567890',
    3, -- ADMIN role ID
    NOW(),
    NOW()
)
ON CONFLICT (username) DO NOTHING;

-- =====================================================
-- OPTION 2: Create SUPER_ADMIN user (highest privileges)
-- =====================================================
-- Uncomment if you need a super admin instead:
/*
INSERT INTO users (
    id, 
    username, 
    email, 
    password, 
    full_name, 
    address, 
    phone, 
    role_id,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'superadmin',
    'superadmin@carparts.com',
    '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRps.9cGLcZEiGDMVr5yUY1Q8flaa', -- Password: super123
    'Super Administrator',
    'Admin Office',
    '+1234567891',
    4, -- SUPER_ADMIN role ID
    NOW(),
    NOW()
)
ON CONFLICT (username) DO NOTHING;
*/

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================

-- Check if the admin user was created successfully
SELECT 
    u.id,
    u.username,
    u.email,
    u.full_name,
    r.name as role,
    u.created_at
FROM users u
JOIN roles r ON u.role_id = r.id
WHERE u.username IN ('admin', 'superadmin')
ORDER BY u.created_at DESC;

-- Count admin users
SELECT 
    r.name as role,
    COUNT(u.id) as user_count
FROM users u
JOIN roles r ON u.role_id = r.id
WHERE r.name IN ('ADMIN', 'SUPER_ADMIN')
GROUP BY r.name;

-- =====================================================
-- IMPORTANT NOTES:
-- =====================================================
-- 1. LOGIN CREDENTIALS:
--    Username: admin
--    Email: admin@carparts.com
--    Password: admin123
--
-- 2. The admin user is created in the USERS table (not admins table)
--    because your AuthController authenticates against the users table
--
-- 3. The separate 'admins' and 'super_admins' tables exist in your
--    database but are NOT used for authentication. They may be legacy
--    tables or used for other purposes.
--
-- 4. After running this script, you can login at:
--    POST http://localhost:8080/api/auth/login
--    Body: {"email": "admin@carparts.com", "password": "admin123"}
--
-- 5. Once logged in, you can access the admin dashboard at:
--    http://localhost:4200/admin
--
-- 6. PASSWORD HASHES (BCrypt with 10 rounds):
--    admin123  -> $2a$10$N9qo8uLOickgx2ZMRZoMye7FRNJheRYICjr3Cujna2TlN0e4h.tPu
--    super123  -> $2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRps.9cGLcZEiGDMVr5yUY1Q8flaa
--
-- 7. To change the password, use an online BCrypt generator with
--    10 rounds and replace the password hash in the INSERT statement
-- =====================================================
