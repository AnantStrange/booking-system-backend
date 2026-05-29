-- Migration: create parents table
-- Date: 2026-05-28 23:11:42

-- UP
-- TODO: Write your migration

CREATE TABLE IF NOT EXISTS parents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    timezone VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- DOWN (optional)
-- TODO: Write rollback if needed

