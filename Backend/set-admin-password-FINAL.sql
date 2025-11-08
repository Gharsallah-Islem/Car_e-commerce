-- =====================================================
-- UPDATE ADMIN PASSWORD WITH KNOWN WORKING HASH
-- =====================================================
-- Using a verified BCrypt hash from bcrypt-generator.com
-- Password will be: Password123
-- =====================================================

update users
   set password = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
       updated_at = now()
 where username = 'admin';

-- Verify the update
select u.username,
       u.email,
       length(u.password) as pwd_length,
       substring(
          u.password,
          1,
          20
       ) as pwd_start,
       r.name as role
  from users u
  join roles r
on u.role_id = r.id
 where u.username = 'admin';

-- =====================================================
-- LOGIN CREDENTIALS:
-- Email: admin@carparts.com
-- Password: Password123
-- =====================================================
-- This hash was generated with BCrypt round 10
-- Hash: $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
-- Password: Password123
-- =====================================================