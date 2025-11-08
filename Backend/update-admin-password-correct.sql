-- Update admin password with correct BCrypt hash
UPDATE users 
SET password = '$2a$10$FJDYmfyz18Bh4e/yzZbrouHoL4lZT1In8pA9FnXRT1d.jVtnMDIfm',
    updated_at = NOW()
WHERE username = 'admin';

-- Verify the update
SELECT 
    username,
    email,
    LENGTH(password) as pwd_length,
    password,
    updated_at
FROM users 
WHERE username = 'admin';
