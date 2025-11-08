-- =====================================================
-- FIX ADMIN USER - Delete and Recreate
-- =====================================================
-- This will delete any existing admin user and create a fresh one
-- with a verified BCrypt hash
-- =====================================================

-- Step 1: Delete existing admin user (if any)
delete from users
 where username = 'admin'
    or email = 'admin@carparts.com';

-- Step 2: Create fresh admin user with verified hash
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
           '$2a$10$N9qo8uLOickgx2ZMRZoMye7FRNJheRYICjr3Cujna2TlN0e4h.tPu',
           'System Administrator',
           'Admin Office',
           '+1234567890',
           3,
           now(),
           now() );

-- Step 3: Verify the user was created
select u.id,
       u.username,
       u.email,
       length(u.password) as password_length,
       substring(
          u.password,
          1,
          10
       ) as password_start,
       u.full_name,
       r.name as role,
       u.created_at
  from users u
  join roles r
on u.role_id = r.id
 where u.username = 'admin';

-- Step 4: Test login query (this is what Spring does)
select u.id,
       u.username,
       u.email,
       u.password,
       r.name as role_name
  from users u
  join roles r
on u.role_id = r.id
 where u.username = 'admin'
    or u.email = 'admin';

-- Expected output:
-- password_length should be 60
-- password_start should be '$2a$10$N9q'
-- role should be 'ADMIN'