-- ====================================================================
-- Add email verification columns to users table
-- ====================================================================

-- Add is_email_verified column with default value true for existing users
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS is_email_verified BOOLEAN DEFAULT true NOT NULL;

-- Add email verification token column
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS email_verification_token VARCHAR(255);

-- Add email verification token expiry column
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS email_verification_token_expiry TIMESTAMP;

-- Add password reset token column
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS password_reset_token VARCHAR(255);

-- Add password reset token expiry column
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS password_reset_token_expiry TIMESTAMP;

-- Update all existing users to have is_email_verified = true (they already have accounts)
UPDATE users
SET is_email_verified = true;

-- Verify the update
SELECT 
    u.id,
    u.username,
    u.email,
    u.is_email_verified,
    r.name as role
FROM users u
JOIN roles r ON u.role_id = r.id
ORDER BY r.name, u.username;
