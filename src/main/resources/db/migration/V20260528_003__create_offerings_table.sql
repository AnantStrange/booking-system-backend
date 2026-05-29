-- Migration: create offerings table
-- Date: 2026-05-28 23:11:42

-- UP
-- TODO: Write your migration

CREATE TABLE IF NOT EXISTS offerings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    course_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id),
    FOREIGN KEY (teacher_id) REFERENCES teachers(id)
);

-- DOWN (optional)
-- TODO: Write rollback if needed

