-- Add COORDINATOR role to roles table
INSERT INTO roles (name)
VALUES ('COORDINATOR')
ON DUPLICATE KEY UPDATE name=name;
