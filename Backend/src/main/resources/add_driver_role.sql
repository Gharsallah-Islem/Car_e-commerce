-- Add DRIVER role if not exists
INSERT INTO roles (id, name) 
SELECT (SELECT COALESCE(MAX(id), 0) + 1 FROM roles), 'DRIVER'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'DRIVER');
