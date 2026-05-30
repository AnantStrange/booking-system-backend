# Booking System API

A Spring Boot application for managing course offerings, sessions, and parent bookings with timezone-aware scheduling and concurrent booking handling.

## Project Overview

Teachers create courses and offerings with sessions. Parents view available offerings in their local timezone and book entire offerings. The system prevents duplicate bookings and time conflicts across different offerings using SERIALIZABLE transaction isolation.

## Tech Stack

* Java 21
* Spring Boot 3.5.0
* Spring Data JPA
* MySQL 8.4
* Flyway (database migrations)
* Podman (containerization)
* Maven

## Quick Start

```bash
# Start MySQL container
podman run -d --name mysql-booking -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=booking_system mysql:8.4

# Run the application
./mvnw spring-boot:run
```

Application runs at:

```text
http://localhost:8080
```

## API Overview

| Method | Endpoint                                | Description                |
| ------ | --------------------------------------- | -------------------------- |
| POST   | `/api/teachers`                         | Create teacher             |
| POST   | `/api/teachers/{id}/courses`            | Create course              |
| POST   | `/api/teachers/{id}/offerings`          | Create offering            |
| POST   | `/api/teachers/offerings/{id}/sessions` | Add sessions               |
| POST   | `/api/parents`                          | Create parent              |
| POST   | `/api/parents/{id}/bookings`            | Book offering              |
| GET    | `/api/parents/{id}/offerings`           | View available offerings   |
| GET    | `/api/parents/{id}/bookings`            | View parent's bookings     |
| GET    | `/api/parents/{id}/sessions`            | View session times         |
| GET    | `/api/teachers/{id}`                    | Get teacher profile        |
| GET    | `/api/teachers/{id}/offerings`          | Get teacher's offerings    |
| GET    | `/api/courses`                          | List all courses           |
| GET    | `/api/courses/{id}`                     | Get course with offerings  |
| GET    | `/api/offerings/{id}`                   | Get offering with sessions |

For detailed API documentation with request/response examples, see [API Documentation][./mds/api_doc.md].

## Database Schema

* `teachers` - Instructor information with timezone
* `courses` - Course catalog
* `offerings` - Course instances with teacher assignment
* `sessions` - Individual session dates and times (UTC)
* `parents` - Parent/student information with timezone
* `bookings` - Parent offering enrollments

For complete schema with field definitions and relationships, see Database Schema.

## Key Design Decisions

### Timezone Handling

* All timestamps stored as UTC in database
* Teachers provide session times in their local timezone
* Parents view all times in their local timezone
* Timezone conversion handled by `TimezoneService`

### Concurrency Handling

* SERIALIZABLE transaction isolation for booking operations
* Unique constraint on `(parent_id, offering_id)` prevents duplicate bookings
* Conflict detection checks all existing bookings before creating new one

### Booking Rules

* Parents book entire offerings (all sessions together)
* Time conflict prevents booking overlapping sessions
* Duplicate booking returns `409 Conflict`

## Assumptions

* Teachers and parents have timezone field set during creation
* Session times are provided by teachers in their local timezone
* All session times are stored as UTC in database
* An offering must have at least one session before parents can book it
* One parent cannot book the same offering twice
* Cancellation is not implemented in current version

## Environment Configuration

Create `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/booking_system useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
server.port=8080
```

## Running with Podman

```bash
# Start MySQL
podman run -d --name mysql-booking -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=booking_system mysql:8.4

# Verify container is running
podman ps

# Stop when done
podman stop mysql-booking
```

## Testing with Postman

Import the Postman collection from `postman/collections/` folder.

See Build Documentation for detailed container setup and deployment.

## Documentation Links

* [API Documentation](./docs/api_doc.md) - Complete API reference with examples
* [Code Documentation](./docs/code_doc.md) - Architecture and implementation details
* [Build Documentation](./docs/build_doc.md) - Container and deployment setup
* [Database Documentation](./docs/db_docs.md) - Database schema and design decisions
* [Development Notes](./docs/dev_notes.md) - Database schema and design decisions

## License

Educational project for demonstration purposes.

