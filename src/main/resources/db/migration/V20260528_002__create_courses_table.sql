-- Migration: create courses table
-- Date: 2026-05-28 23:11:42

-- UP
CREATE TABLE IF NOT EXISTS courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- DOWN (optional)
-- DROP TABLE IF EXISTS courses;

