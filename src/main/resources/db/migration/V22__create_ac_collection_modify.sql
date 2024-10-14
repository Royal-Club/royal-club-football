-- Flyway script for modifying the 'ac_collections' table

-- 1. Add the new 'date' field as a DATE type (nullable initially for now)
ALTER TABLE ac_collections
    ADD COLUMN `date` DATE DEFAULT NULL;

-- 2. Update the 'date' field by copying the existing values from 'created_date'
UPDATE ac_collections
SET `date` = `created_date`
WHERE `created_date` IS NOT NULL;

-- 3. Make the 'date' field non-nullable after updating existing records
ALTER TABLE ac_collections
    MODIFY COLUMN `date` DATE NOT NULL;

-- 4. Delete the 'is_paid' field
ALTER TABLE ac_collections
DROP COLUMN `is_paid`;