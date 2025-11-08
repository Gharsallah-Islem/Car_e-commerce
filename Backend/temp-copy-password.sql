-- =====================================================
-- COPY PASSWORD FROM EXISTING USER TO ADMIN
-- =====================================================
-- Since we can't generate a new hash easily, let's copy a working
-- password from an existing user and then you can change it later
-- =====================================================

-- First, let's see what password one of your existing users has
-- (We'll copy their hash temporarily so admin can login)

-- Copy the password from user 'a9wanes' to admin user
UPDATE users 
SET password = (SELECT password FROM users WHERE username = 'a9wanes' LIMIT 1),
    updated_at = NOW()
WHERE username = 'admin';

-- Verify the update
SELECT 
    u.username,
    u.email,
    LENGTH(u.password) as pwd_length,
    SUBSTRING(u.password, 1, 20) as pwd_sample,
    r.name as role
FROM users u
JOIN roles r ON u.role_id = r.id
WHERE u.username IN ('admin', 'a9wanes');

-- =====================================================
-- NOW YOU CAN LOGIN WITH:
-- Email: admin@carparts.com
-- Password: <same password as user a9wanes>
-- =====================================================
-- After logging in, you should change the password through
-- your application's UI or create a password change endpoint
-- =====================================================
