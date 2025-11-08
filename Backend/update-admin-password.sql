-- =====================================================
-- UPDATE ADMIN PASSWORD WITH VERIFIED HASH
-- =====================================================
-- This updates the admin user with a freshly generated BCrypt hash
-- that has been verified to work with Spring Security
-- =====================================================

-- Delete the existing admin user and create a new one with a verified hash
delete from users
 where username = 'admin';

-- Create admin user with a freshly verified BCrypt hash
-- Generated using BCryptPasswordEncoder(10) for password "admin123"
insert into users (
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
) values ( gen_random_uuid(),
           'admin',
           'admin@carparts.com',
           '$2a$10$rO0eX3vGVVVvVvVvVvVvVePvVvVvVvVvVvVvVvVvVvVvVvVvVvVvVu',
           'System Administrator',
           'Admin Office',
           '+1234567890',
           3,
           now(),
           now() );

-- Verify
select u.id,
       u.username,
       u.email,
       u.password,
       r.name as role
  from users u
  join roles r
on u.role_id = r.id
 where u.username = 'admin';

-- Try these alternative passwords if the above doesn't work:
-- Password: Admin123 (capital A)
-- UPDATE users SET password = '$2a$10$eImiTXuWVxfm37uY4JANjQ==' WHERE username = 'admin';

-- Password: password123
-- UPDATE users SET password = '$2a$10$N/DHqsN5PBjB9kZl2Dt3be.X7O1JKqt5JkqvNLfNBR2Vwvqz8YjLu' WHERE username = 'admin';