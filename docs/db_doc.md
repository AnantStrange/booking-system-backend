# Database Documentation

## Schema Overview

The database consists of 6 tables representing a booking system for course offerings. All timestamps are stored in UTC.

### Entity Relationship Diagram

image_here

---
```


# Table Definitions

## teachers

Stores teacher information including timezone for session scheduling.

| Column     | Type         | Constraints                 | Description                              |
| ---------- | ------------ | --------------------------- | ---------------------------------------- |
| id         | BIGINT       | PRIMARY KEY, AUTO_INCREMENT | Unique teacher identifier                |
| name       | VARCHAR(100) | NOT NULL                    | Teacher's full name                      |
| timezone   | VARCHAR(50)  | NOT NULL                    | IANA timezone (e.g., "America/New_York") |
| created_at | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP   | Record creation timestamp (UTC)          |

### Example Data

```sql 
INSERT INTO teachers (name, timezone) VALUES 
('John Doe', 'America/New_York'),
('Jane Smith', 'Asia/Kolkata');
```

---

## courses

Stores course catalog information.

| Column      | Type         | Constraints                 | Description                     |
| ----------- | ------------ | --------------------------- | ------------------------------- |
| id          | BIGINT       | PRIMARY KEY, AUTO_INCREMENT | Unique course identifier        |
| name        | VARCHAR(200) | NOT NULL                    | Course name                     |
| description | TEXT         | NULLABLE                    | Course description/details      |
| created_at  | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP   | Record creation timestamp (UTC) |

### Example Data

```sql 
INSERT INTO courses (name, description) VALUES 
('Mathematics Grade 5', 'Advanced mathematics including algebra and geometry'),
('Science Grade 5', 'Physics and chemistry basics');
```

---

## offerings

Connects courses with teachers. Represents a specific instance of a course taught by a teacher.

| Column     | Type         | Constraints                          | Description                               |
| ---------- | ------------ | ------------------------------------ | ----------------------------------------- |
| id         | BIGINT       | PRIMARY KEY, AUTO_INCREMENT          | Unique offering identifier                |
| name       | VARCHAR(200) | NOT NULL                             | Offering name (e.g., "Summer Batch 2026") |
| course_id  | BIGINT       | NOT NULL, FOREIGN KEY → courses(id)  | Reference to course                       |
| teacher_id | BIGINT       | NOT NULL, FOREIGN KEY → teachers(id) | Reference to teacher                      |
| created_at | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP            | Record creation timestamp (UTC)           |

### Foreign Key Constraints

* course_id references courses(id)
* teacher_id references teachers(id)

### Example Data

```sql 
INSERT INTO offerings (name, course_id, teacher_id) VALUES 
('Summer Batch 2026', 1, 1),
('Evening Summer Batch', 1, 1);
```

---

## sessions

Stores individual session dates and times for each offering. Times are stored in UTC.

| Column      | Type      | Constraints                                             | Description                     |
| ----------- | --------- | ------------------------------------------------------- | ------------------------------- |
| id          | BIGINT    | PRIMARY KEY, AUTO_INCREMENT                             | Unique session identifier       |
| offering_id | BIGINT    | NOT NULL, FOREIGN KEY → offerings(id) ON DELETE CASCADE | Reference to offering           |
| start_time  | TIMESTAMP | NOT NULL                                                | Session start time (UTC)        |
| end_time    | TIMESTAMP | NOT NULL                                                | Session end time (UTC)          |
| created_at  | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP                               | Record creation timestamp (UTC) |

### Foreign Key Constraints

* offering_id references offerings(id) with ON DELETE CASCADE

### Validation Rules

* end_time must be after start_time
* Sessions for the same offering cannot overlap

### Example Data

```sql 
INSERT INTO sessions (offering_id, start_time, end_time) VALUES 
(1, '2026-06-07 10:00:00', '2026-06-07 12:00:00'),
(1, '2026-06-14 10:00:00', '2026-06-14 12:00:00');
```

---

## parents

Stores parent/student information including timezone for display conversion.

| Column     | Type         | Constraints                 | Description                                      |
| ---------- | ------------ | --------------------------- | ------------------------------------------------ |
| id         | BIGINT       | PRIMARY KEY, AUTO_INCREMENT | Unique parent identifier                         |
| name       | VARCHAR(100) | NOT NULL                    | Parent's full name                               |
| timezone   | VARCHAR(50)  | NOT NULL                    | IANA timezone for display (e.g., "Asia/Kolkata") |
| created_at | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP   | Record creation timestamp (UTC)                  |

### Example Data

```sql 
INSERT INTO parents (name, timezone) VALUES 
('Alice Brown', 'Asia/Kolkata'),
('Bob Wilson', 'America/New_York');
```

---

## bookings

Junction table tracking which parent booked which offering.

| Column      | Type      | Constraints                           | Description               |
| ----------- | --------- | ------------------------------------- | ------------------------- |
| id          | BIGINT    | PRIMARY KEY, AUTO_INCREMENT           | Unique booking identifier |
| parent_id   | BIGINT    | NOT NULL, FOREIGN KEY → parents(id)   | Reference to parent       |
| offering_id | BIGINT    | NOT NULL, FOREIGN KEY → offerings(id) | Reference to offering     |
| booked_at   | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP             | Booking timestamp (UTC)   |

### Foreign Key Constraints

* parent_id references parents(id)
* offering_id references offerings(id)

### Unique Constraints

* UNIQUE (parent_id, offering_id) - Prevents duplicate bookings

### Example Data

```sql 
INSERT INTO bookings (parent_id, offering_id) VALUES 
(1, 1),
(2, 2);
```

---

# Relationships Summary

| Relationship         | Type        | Description                            |
| -------------------- | ----------- | -------------------------------------- |
| teachers → offerings | One-to-Many | A teacher can have multiple offerings  |
| courses → offerings  | One-to-Many | A course can have multiple offerings   |
| offerings → sessions | One-to-Many | An offering has multiple sessions      |
| parents → bookings   | One-to-Many | A parent can have multiple bookings    |
| offerings → bookings | One-to-Many | An offering can have multiple bookings |

---

# Indexes

| Table     | Index                    | Type        | Purpose                                       |
| --------- | ------------------------ | ----------- | --------------------------------------------- |
| bookings  | (parent_id, offering_id) | UNIQUE      | Prevent duplicate bookings                    |
| sessions  | offering_id              | FOREIGN KEY | Cascade delete sessions when offering deleted |
| offerings | course_id                | FOREIGN KEY | Enforce referential integrity                 |
| offerings | teacher_id               | FOREIGN KEY | Enforce referential integrity                 |

---

# Flyway Migrations

Migrations are versioned and run automatically on application startup.

## Migration Files

| File                                      | Tables Created |
| ----------------------------------------- | -------------- |
| V20260528_001__create_teachers_table.sql  | teachers       |
| V20260528_002__create_courses_table.sql   | courses        |
| V20260528_003__create_offerings_table.sql | offerings      |
| V20260528_004__create_sessions_table.sql  | sessions       |
| V20260528_005__create_parents_table.sql   | parents        |
| V20260528_006__create_bookings_table.sql  | bookings       |

## Migration Order

```text 
teachers → courses → offerings → sessions → parents → bookings
```

### Why this order

* teachers and courses have no dependencies
* offerings depends on teachers and courses
* sessions depends on offerings
* parents has no dependencies
* bookings depends on parents and offerings

---

# Sample Query: Get Offerings for Parent with Timezone

```sql 
-- Get offerings a parent has booked (with UTC times)
SELECT 
    o.id AS offering_id,
    o.name AS offering_name,
    c.name AS course_name,
    t.name AS teacher_name,
    s.start_time,
    s.end_time
FROM bookings b
JOIN offerings o ON b.offering_id = o.id
JOIN courses c ON o.course_id = c.id
JOIN teachers t ON o.teacher_id = t.id
JOIN sessions s ON o.id = s.offering_id
WHERE b.parent_id = 1;
```

---

# Sample Query: Check for Time Conflicts

```sql 
-- Check if parent already has a session overlapping with new session
SELECT COUNT(*)
FROM bookings b
JOIN sessions s ON b.offering_id = s.offering_id
WHERE b.parent_id = 1
  AND s.start_time < '2026-06-14 12:00:00'
  AND s.end_time > '2026-06-14 10:00:00';
```

---

# Data Integrity Rules

| Rule                                  | Enforcement             |
| ------------------------------------- | ----------------------- |
| No duplicate parent-offering bookings | UNIQUE constraint       |
| No orphaned sessions                  | ON DELETE CASCADE       |
| No orphaned bookings                  | FOREIGN KEY constraints |
| All timestamps in UTC                 | Application logic       |
| Sessions must have valid time range   | Application validation  |

---

# Backup and Restore

## Backup

```bash 
podman exec mysql-booking mysqldump -uroot -proot booking_system > backup.sql
```

## Restore

```bash 
cat backup.sql | podman exec -i mysql-booking mysql -uroot -proot booking_system
```

