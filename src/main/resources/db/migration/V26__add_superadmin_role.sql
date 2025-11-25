-- Add SUPERADMIN role to roles table
INSERT INTO roles (name)
VALUES ('SUPERADMIN')
ON DUPLICATE KEY UPDATE name=name;

