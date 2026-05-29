-- Migration: create bookings table
-- Date: 2026-05-28 23:11:42

-- UP
-- TODO: Write your migration

CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT NOT NULL,
    offering_id BIGINT NOT NULL,
    booked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES parents(id),
    FOREIGN KEY (offering_id) REFERENCES offerings(id),
    UNIQUE KEY unique_parent_offering (parent_id, offering_id)
);

-- DOWN (optional)
-- TODO: Write rollback if needed

