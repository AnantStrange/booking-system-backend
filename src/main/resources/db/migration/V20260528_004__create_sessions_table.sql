-- Migration: create sessions table
-- Date: 2026-05-28 23:11:42

-- UP
-- TODO: Write your migration

CREATE TABLE IF NOT EXISTS sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    offering_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (offering_id) REFERENCES offerings(id) ON DELETE CASCADE
);

-- DOWN (optional)
-- TODO: Write rollback if needed

