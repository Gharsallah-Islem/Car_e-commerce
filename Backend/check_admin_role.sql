-- Check admin user and role
SELECT u.id, u.username, u.email, u.full_name, u.role_id, r.name as role_name
FROM users u
LEFT JOIN roles r ON u.role_id = r.id
WHERE u.email = 'admin@carparts.com';

-- Check all roles
SELECT * FROM roles;
