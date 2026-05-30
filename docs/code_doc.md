# Code Documentation

## Project Structure

```text id="6x8n1q"
image_project_strcut
```

---

# Architecture Layers

## Controller Layer

Handles HTTP requests, validates input, returns HTTP responses.

### Example - ParentController

```java id="cghn4v"
@RestController
@RequestMapping("/api/parents")
public class ParentController {
    
    @Autowired
    private ParentService parentService;
    
    @GetMapping("/{parentId}/offerings")
    public ResponseEntity<List<OfferingInfo>> getOfferings(@PathVariable Long parentId) {
        parentService.validateAndGetParent(parentId);
        List<OfferingInfo> offerings = parentService.getAvailableOfferings(parentId);
        return ResponseEntity.ok(offerings);
    }
}
```

---

## Service Layer

Contains business logic, transaction management, and orchestration.

### Example - BookingService with concurrency handling

```java id="0e4h8u"
@Service
public class BookingService {
    
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Booking bookOffering(Long parentId, Long offeringId) {
        // 1. Validate parent and offering exist
        // 2. Check for duplicate booking
        // 3. Check for time conflicts
        // 4. Save booking
    }
}
```

---

## Repository Layer

Spring Data JPA interfaces for database operations.

### Example

```java id="1gj0vw"
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByParentId(Long parentId);
    boolean existsByParentIdAndOfferingId(Long parentId, Long offeringId);
}
```

---

## Entity Layer

JPA entities mapping to database tables.

### Example

```java id="m2az8z"
@Entity
@Table(name = "teachers")
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String timezone;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
```

---

## DTO Layer

Data transfer objects for API requests and responses.

### Pattern

* `*Request.java` - Input DTOs (POST/PUT bodies)
* `*Info.java` - Output DTOs (GET responses)
* `*Response.java` - Creation responses

---

# Key Components

## TimezoneService

Handles all timezone conversions.

```java id="4h8vca"
@Service
public class TimezoneService {
    // Convert teacher's local time to UTC for storage
    public Instant toUTC(ZonedDateTime localTime, String timezone)
    
    // Convert UTC to parent's local time for display
    public ZonedDateTime toLocal(Instant utcTime, String timezone)
    
    // Format for API responses
    public String formatTime(Instant utcTime, String timezone)
    
    // Get formatted time for specific entity
    public String getLocalTime(Long id, String type, Instant utcTime)
}
```

---

## BookingService - Concurrency Handling

Uses SERIALIZABLE isolation level to prevent race conditions.

```java id="x2f3jr"
@Transactional(isolation = Isolation.SERIALIZABLE)
public Booking bookOffering(Long parentId, Long offeringId) {
    // All database operations in this transaction are isolated
    // Prevents:
    // - Two parents booking the same offering simultaneously
    // - Time conflicts from concurrent requests
}
```

---

## Conflict Detection Logic

```java id="6wl56x"
private boolean hasTimeConflict(Session newSession, Session existingSession) {
    Instant newStart = newSession.getStartTime();
    Instant newEnd = newSession.getEndTime();
    Instant existingStart = existingSession.getStartTime();
    Instant existingEnd = existingSession.getEndTime();
    
    return newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd);
}
```

---

# Design Patterns

## DTO Pattern

Separates API contract from database entities.

| Entity   | Purpose          | DTO            | Purpose      |
| -------- | ---------------- | -------------- | ------------ |
| Teacher  | Database mapping | TeacherProfile | API response |
| Offering | Database mapping | OfferingInfo   | API response |
| Booking  | Database mapping | BookingInfo    | API response |

---

## Repository Pattern

Spring Data JPA provides base CRUD operations.

---

## Dependency Injection

Using `@Autowired` for loose coupling.

```java id="b5sl9z"
@Autowired
private BookingService bookingService;
```

---

# Error Handling

## Global Exception Pattern

Using `ResponseStatusException` for HTTP status codes.

```java id="1xhkm4"
if (parentNotFound) {
    throw new ResponseStatusException(
        HttpStatus.NOT_FOUND, 
        "Parent not found: " + parentId
    );
}
```

---

## HTTP Status Mapping

| Exception                                     | Status | When                       |
| --------------------------------------------- | ------ | -------------------------- |
| ResponseStatusException(NOT_FOUND)            | 404    | Resource missing           |
| ResponseStatusException(CONFLICT)             | 409    | Duplicate or time conflict |
| ResponseStatusException(UNPROCESSABLE_ENTITY) | 422    | No sessions in offering    |

---

# Transaction Management

## Read Operations

```java id="tx2j9d"
@Transactional(readOnly = true)
public List<OfferingInfo> getOfferings() {
    // No write operations, better performance
}
```

---

## Write Operations

```java id="2m7v0q"
@Transactional(isolation = Isolation.SERIALIZABLE)
public Booking createBooking() {
    // Highest isolation level for booking
}
```

---

# Database Migrations (Flyway)

Migration files in `src/main/resources/db/migration/`:

```text id="phzpq0"
V20260528_001__create_teachers_table.sql
V20260528_002__create_courses_table.sql
V20260528_003__create_offerings_table.sql
V20260528_004__create_sessions_table.sql
V20260528_005__create_parents_table.sql
V20260528_006__create_bookings_table.sql
```

Flyway runs automatically on application startup.

---

# Configuration

## application.properties

```properties id="n4qaqr"
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/booking_system
spring.datasource.username=root
spring.datasource.password=root

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Flyway
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
```

---

# Testing Strategy

## Manual Testing with Postman

* Import collection from `/postman/collections/`
* Use dataset JSON files for parameterized runs
* Run in order: create → book → verify

---

## Expected Test Cases

| Test                  | Expected      |
| --------------------- | ------------- |
| Create teacher        | 201 Created   |
| Create course         | 201 Created   |
| Create offering       | 201 Created   |
| Add sessions          | 201 Created   |
| Parent books offering | 201 Created   |
| Duplicate booking     | 409 Conflict  |
| Time conflict         | 409 Conflict  |
| Invalid offering      | 404 Not Found |

---

# Performance Considerations

* SERIALIZABLE isolation - Ensures consistency but reduces concurrency
* UTC storage - No timezone conversion at database level
* Eager loading - Sessions loaded with offerings to prevent N+1 queries
* Indexes - Foreign keys automatically indexed by JPA

