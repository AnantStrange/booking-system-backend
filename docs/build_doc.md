# Build Documentation

## Prerequisites

* Java 21
* Maven 3.8+
* Podman or Docker (both are interchangable)
* Git

---

## Quick Start (One Command)

```bash
./mvnw clean package && podman-compose up -d --build

# Check status
podman ps
```


---

## Step by Step Setup

### 1. Clone Repository

```bash
git clone https://github.com/AnantStrange/booking-system-backend
cd booking-system-backend
```

### 2. Build JAR

```bash
./mvnw clean package
```

This creates the `target/*.jar` file.

### 3. Start Containers

```bash
podman-compose up -d --build

# Or with Docker:
docker-compose up -d --build
```

### 4. Wait for MySQL Initialization

MySQL could sometimes take longer to be ready, then app is initialized which leads to database
connection error. The app will retry connecting automatically (60 second timeout configured).

### 5. Verify Running Containers

```bash
podman ps
```

Expected output:

```
CONTAINER ID  IMAGE                    COMMAND     PORTS                    NAMES
xxx           mysql:8.4                mysqld      0.0.0.0:3306->3306/tcp   mysql-booking
xxx           undoschool_app:latest    java        0.0.0.0:8080->8080/tcp   booking-app
```

### 6. Test API

```bash
# Health check
curl http://localhost:8080/api/courses

# Create a teacher
curl -X POST http://localhost:8080/api/teachers \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","timezone":"America/New_York"}'
```

---

## Files Overview

### Dockerfile

```dockerfile
FROM docker.io/eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### docker-compose.yml

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.4
    container_name: mysql-booking
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: booking_system
      MYSQL_USER: anant
      MYSQL_PASSWORD: anant
    ports:
      - "3306:3306"
    networks:
      - booking-network

  app:
    build: .
    container_name: booking-app
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/booking_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
      SPRING_DATASOURCE_USERNAME: anant
      SPRING_DATASOURCE_PASSWORD: anant
    ports:
      - "8080:8080"
    depends_on:
      - mysql
    networks:
      - booking-network

networks:
  booking-network:
    driver: bridge
```

### application.properties (required for connection timeout)

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/booking_system
spring.datasource.username=anant
spring.datasource.password=anant

# HikariCP timeout (critical for container startup)
spring.datasource.hikari.connection-timeout=60000

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Flyway
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
```

---

## Troubleshooting

### App Can't Connect to MySQL

**Error:** Communications link failure
**Fix:** Increase connection timeout in `application.properties`:

```properties
spring.datasource.hikari.connection-timeout=60000
```

Then rebuild:

```bash
./mvnw clean package
podman-compose up -d --build
```

### MySQL Not Ready

**Error:** Access denied for user
**Fix:** Wait 30 seconds after starting containers, then restart app:

```bash
podman-compose restart booking-app
```

### Port Already in Use

**Error:** port already allocated
**Fix:** Stop existing containers or change ports:

```bash
podman-compose down

# Or kill process using port 8080
lsof -i :8080
kill -9 <PID>
```

### Flyway Migration Fails

**Error:** Flyway migration error
**Fix:** Clean database and restart:

```bash
podman exec -it mysql-booking mysql -uroot -proot -e "DROP DATABASE booking_system; CREATE DATABASE booking_system;"
podman-compose restart app
```

### Container Image Not Found

**Error:** short-name did not resolve
**Fix:** Use full registry path in Dockerfile:

```dockerfile
FROM docker.io/eclipse-temurin:21-jre-alpine
```

---

## Useful Commands

| Command                                               | Purpose                        |
| ----------------------------------------------------- | ------------------------------ |
| `podman-compose up -d`                                | Start containers in background |
| `podman-compose down`                                 | Stop and remove containers     |
| `podman-compose logs -f`                              | View live logs                 |
| `podman-compose logs app`                             | View app logs only             |
| `podman-compose logs mysql`                           | View MySQL logs only           |
| `podman-compose restart app`                          | Restart app container          |
| `podman exec -it mysql-booking bash`                  | Enter MySQL container          |
| `podman exec -it mysql-booking mysql -uanant -panant` | MySQL CLI                      |

---

## Demo Checklist

Before demo, verify:

* `podman ps` shows both containers running
* `curl http://localhost:8080/api/courses` returns JSON (not error)
* Postman collection imports successfully
* Can create teacher, course, offering, sessions
* Can create parent and book offering
* Timezone conversion works (different timezones show correct local times)

---

## Cleanup

```bash
# Stop containers
podman-compose down

# Remove all containers and images
podman system prune -a

# Remove volumes
podman volume prune
```

---

## Important Notes for Demo

* If at start API returns empty array `[]`, that's normal, you need to fill db with data via
  **POST** requests first.
* Use Postman collection to create test data first, then test GET endpoints

---

## Support

If containers fail to start:

```bash
# Check logs
podman-compose logs

# Full rebuild
podman-compose down
podman rmi undoschool_app
./mvnw clean package
podman-compose up -d --build
```

For MySQL connection issues, verify MySQL is ready:

```bash
podman logs mysql-booking | grep "ready for connections"
```

---
* [API Documentation](./api_doc.md) - Complete API reference with examples
* [Code Documentation](./code_doc.md) - Architecture and implementation details
* [Build Documentation](./build_doc.md) - Container and deployment setup
* [Database Documentation](./db_docs.md) - Database schema and design decisions
* [Development Notes](./dev_notes.md) - Database schema and design decisions
