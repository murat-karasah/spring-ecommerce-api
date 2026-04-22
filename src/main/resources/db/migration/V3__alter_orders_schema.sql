-- Rename total_amount → total to match entity field
ALTER TABLE orders RENAME COLUMN total_amount TO total;

-- Add updated_at (missing from initial schema)
ALTER TABLE orders ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT NOW();
