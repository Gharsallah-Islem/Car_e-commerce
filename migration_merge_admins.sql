-- 1. Ensure Roles exist
INSERT INTO roles (name) VALUES ('CLIENT') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('SUPPORT') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ADMIN') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('SUPER_ADMIN') ON CONFLICT (name) DO NOTHING;

-- 2. Drop separate tables (Data is discarded as per plan)
DROP TABLE IF EXISTS admins;
DROP TABLE IF EXISTS super_admins;

-- 3. Verify users table structure (already confirmed via schema dump, but good to be safe)
-- We rely on the existing users table which has role_id.

-- 4. Insert a default Super Admin user for immediate access
-- Password is 'password' (BCrypt hash: $2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG)
-- Role ID for SUPER_ADMIN needs to be fetched.
DO $$
DECLARE
    super_admin_role_id INTEGER;
BEGIN
    SELECT id INTO super_admin_role_id FROM roles WHERE name = 'SUPER_ADMIN';

    INSERT INTO users (
        id, 
        username, 
        email, 
        password, 
        full_name, 
        role_id, 
        is_email_verified, 
        created_at, 
        updated_at
    ) VALUES (
        '00000000-0000-0000-0000-000000000001', -- Fixed UUID for easy reference
        'superadmin', 
        'superadmin@example.com', 
        '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 
        'System Super Admin', 
        super_admin_role_id, 
        true, 
        NOW(), 
        NOW()
    ) ON CONFLICT (email) DO NOTHING;
END $$;
