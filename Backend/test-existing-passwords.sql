-- =====================================================
-- TEST: Use password from existing working user
-- =====================================================
-- Let's verify if other users can login by checking their setup
--
-- Step 1: View existing user passwords to understand the format
SELECT 
    u.username,
    u.email,
    u.password,
    r.name as role
FROM users u
JOIN roles r ON u.role_id = r.id
WHERE u.username IN ('a9wanes', 'jomaa5971', 'islemgharsallah86')
LIMIT 3;
