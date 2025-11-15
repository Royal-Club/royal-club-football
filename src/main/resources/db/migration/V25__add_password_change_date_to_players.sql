-- Add last_password_change_date column to players table
ALTER TABLE players ADD COLUMN last_password_change_date DATETIME NULL;

-- Set password change date to 100 days ago for all existing players
-- This forces all players to reset their password on first login
UPDATE players SET last_password_change_date = DATE_SUB(NOW(), INTERVAL 100 DAY) WHERE last_password_change_date IS NULL;

