-- Check existing users and their password hashes
SELECT 
    u.id,
    u.username,
    u.email,
    LENGTH(u.password) as pwd_length,
    SUBSTRING(u.password, 1, 20) as pwd_sample,
    r.name as role
FROM users u
JOIN roles r ON u.role_id = r.id
LIMIT 10;
