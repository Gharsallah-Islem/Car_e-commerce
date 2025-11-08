-- =====================================================
-- DEBUG ADMIN USER - Check what's in the database
-- =====================================================

-- 1. Check if admin user exists and what the data looks like
select u.id,
       u.username,
       u.email,
       u.password,
       u.full_name,
       r.id as role_id,
       r.name as role_name,
       u.created_at
  from users u
  left join roles r
on u.role_id = r.id
 where u.username = 'admin'
    or u.email = 'admin@carparts.com'
 order by u.created_at desc;

-- 2. Check all roles
select *
  from roles
 order by id;

-- 3. Check all users with ADMIN or SUPER_ADMIN role
select u.id,
       u.username,
       u.email,
       u.full_name,
       r.name as role
  from users u
  join roles r
on u.role_id = r.id
 where r.name in ( 'ADMIN',
                   'SUPER_ADMIN' )
 order by u.created_at desc;

-- 4. Count all users by role
select r.name as role,
       count(u.id) as user_count
  from roles r
  left join users u
on u.role_id = r.id
 group by r.id,
          r.name
 order by r.id;