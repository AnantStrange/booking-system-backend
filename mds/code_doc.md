# BOOKING SYSTEM - COMPLETE SCHEMA & ENTITIES REFERENCE
## System: Course → Offering → Sessions | Parents book Offerings

---

## 1. DATABASE SCHEMA (MySQL 8.4)

### TEACHERS
id BIGINT (PK)
name VARCHAR(100) NOT NULL
timezone VARCHAR(50) NOT NULL # e.g., "Asia/Kolkata"
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP


### COURSES
id BIGINT (PK)
name VARCHAR(200) NOT NULL
description TEXT
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
text


### OFFERINGS (connects Course + Teacher)

id BIGINT (PK)
name VARCHAR(200) NOT NULL # e.g., "Math Grade 5 - Summer Batch"
course_id BIGINT (FK → courses.id)
teacher_id BIGINT (FK → teachers.id)
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
text


### SESSIONS (all sessions for an offering)

id BIGINT (PK)
offering_id BIGINT (FK → offerings.id) ON DELETE CASCADE
start_time TIMESTAMP NOT NULL # Stored in UTC
end_time TIMESTAMP NOT NULL # Stored in UTC
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
text


### PARENTS

id BIGINT (PK)
name VARCHAR(100) NOT NULL
timezone VARCHAR(50) NOT NULL # e.g., "America/New_York"
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
text


### BOOKINGS (junction: which parent booked which offering)

id BIGINT (PK)
parent_id BIGINT (FK → parents.id)
offering_id BIGINT (FK → offerings.id)
booked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
UNIQUE (parent_id, offering_id) # Parent can't book same offering twice
text


---

## 2. ENTITY CLASSES (Java)

### Teacher
    Long id
    String name
    String timezone
    Instant createdAt

### Course
    Long id
    String name
    String description
    Instant createdAt

### Offering
    Long id
    String name
    Long courseId
    Long teacherId
    Instant createdAt
    List<Session> sessions (transient - not in DB)

### Session
    Long id
    Long offeringId
    Instant startTime
    Instant endTime
    Instant createdAt

### Parent
    Long id
    String name
    String timezone
    Instant createdAt

### Booking
    Long id
    Long parentId
    Long offeringId
    Instant bookedAt
    Offering offering (transient - for display)


---

## 3. RELATIONSHIP FLOW
COURSE (Math Grade 5)
↓ (1 to many)
OFFERING (Summer Batch 2026 - Teacher John)
↓ (1 to many)
SESSIONS (June 7, June 14, June 21)
↑
PARENT books OFFERING → gets ALL sessions automatically


**Key rule:** Parent books OFFERING, not individual sessions.

---

## 4. DTOs (Data Transfer Objects)

### INPUT DTOs (Client → Server)

**CreateBookingRequest**

    Long parentId
    Long offeringId

**SessionRequest**

    String startTime # ISO format: "2026-06-07T18:00:00"
    String endTime # ISO format: "2026-06-07T19:00:00"

### OUTPUT DTOs (Server → Client)

## OfferingDTO
    Long id
    String name
    Long courseId
    Long teacherId
    List<SessionDTO> sessions
    Instant createdAt

## SessionDTO
    Long id
    Long offeringId
    Instant startTime # UTC
    Instant endTime # UTC
    String startTimeLocal # Converted to user's timezone
    String endTimeLocal # Converted to user's timezone


## BookingResponseDTO (missing - needs to be created)

    Long bookingId
    String status
    String message
    List<SessionDTO> sessions

---

## 5. SERVICE LAYER (Key Methods)

### BookingService
    Booking bookOffering(Long parentId, Long offeringId)
    → Validates parent/offering exist
    → Checks duplicate booking
    → Detects time conflicts with existing bookings
    → Uses SERIALIZABLE isolation level

    List<Booking> getParentBookings(Long parentId)
    → Returns all bookings for parent
    → Loads sessions for each offering

### TeacherService

    Offering createOffering(Offering offering)
    → Validates teacher and course exist
    → Sets createdAt to UTC now

    List<Session> addSessions(Long offeringId, List<SessionRequest> requests, String teacherTimezone)
    → Converts local times to UTC for storage

    List<Offering> getTeacherOfferings(Long teacherId)
    → Returns offerings with their sessions

### TimezoneService

    Instant toUTC(ZonedDateTime localTime, String timezone)
    → Converts local time to UTC for storage

    ZonedDateTime toLocal(Instant utcTime, String timezone)
    → Converts UTC to local time for display

    String formatTime(Instant utcTime, String timezone)
    → Returns formatted string: "2026-05-29 15:30:00 IST"

    Instant nowUTC()
    → Returns current UTC time


---

## 6. REPOSITORIES (Spring Data JPA)
    TeacherRepository extends JpaRepository<Teacher, Long>
    CourseRepository extends JpaRepository<Course, Long>
    OfferingRepository extends JpaRepository<Offering, Long>
    SessionRepository extends JpaRepository<Session, Long>
    ParentRepository extends JpaRepository<Parent, Long>
    BookingRepository extends JpaRepository<Booking, Long>

## Custom queries in BookingRepository:
    List<Booking> findByParentId(Long parentId)
    boolean existsByParentIdAndOfferingId(Long parentId, Long offeringId)



---

## 7. BUSINESS RULES

1. **One booking per parent-offering** (UNIQUE constraint in DB)
2. **No overlapping sessions** - System checks all parent's existing sessions for time conflicts
3. **All times stored in UTC** - Converted to local for display using user's timezone
4. **Booking whole offerings** - Parent cannot pick individual sessions
5. **SERIALIZABLE isolation** - Prevents race conditions when booking

---

## 8. MISSING / NEEDS WORK

### Controllers (need implementation)
- ParentController endpoints
- TeacherController endpoints
- TestDataController for seeding data

### DTOs (need creation)
- BookingResponseDTO (currently returning Entity)
- ParentDashboardDTO (aggregated view)

### Missing endpoints
- Cancel booking
- Update/cancel sessions
- Offering status management

---

[api_doc.md](./api_doc.md)

