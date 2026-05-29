# BOOKING SYSTEM API DOCUMENTATION
## REST API Design - Parent & Teacher Booking System

---

## BASE URL
http://localhost:8080/api


## AUTHENTICATION
*Not implemented yet - assume all endpoints are public for now*

---

## 1. PARENT ENDPOINTS

### 1.1 Get All Available Offerings
**GET** `/parents/{parentId}/offerings`

Returns all offerings that parent can book (excludes already booked ones).

**Path Parameters:**
| Name | Type | Description |
|------|------|-------------|
| parentId | Long | ID of the parent |

**Response:** `200 OK`
```json
[
  {
    "id": 10,
    "name": "Math Grade 5 - Summer Batch",
    "courseId": 1,
    "courseName": "Math Grade 5",
    "teacherId": 3,
    "teacherName": "John Smith",
    "sessions": [
      {
        "id": 45,
        "startTimeLocal": "2026-06-07 14:00:00 IST",
        "endTimeLocal": "2026-06-07 15:00:00 IST"
      },
      {
        "id": 46,
        "startTimeLocal": "2026-06-14 14:00:00 IST",
        "endTimeLocal": "2026-06-14 15:00:00 IST"
      }
    ],
    "totalSessions": 2,
    "createdAt": "2026-05-20T10:00:00Z"
  }
]
```

**Errors:**
    404 Not Found - Parent not found

### 1.2 Book an Offering

**POST** /parents/{parentId}/bookings

Books an entire offering (all sessions) for a parent.

Path Parameters:
| Name | Type | Description |
|------|---------|--------------|
| parentId | Long | ID of the parent|


**Response:** 201 Created
```json

{
  "bookingId": 125,
  "parentId": 5,
  "offeringId": 10,
  "offeringName": "Math Grade 5 - Summer Batch",
  "status": "CONFIRMED",
  "bookedAt": "2026-05-29T08:30:00Z",
  "bookedAtLocal": "2026-05-29 14:00:00 IST",
  "sessions": [
    {
      "sessionId": 45,
      "startTimeLocal": "2026-06-07 14:00:00 IST",
      "endTimeLocal": "2026-06-07 15:00:00 IST"
    },
    {
      "sessionId": 46,
      "startTimeLocal": "2026-06-14 14:00:00 IST",
      "endTimeLocal": "2026-06-14 15:00:00 IST"
    }
  ]
}```

**Errors:**
    400 Bad Request - Parent already booked this offering
    400 Bad Request - Time conflict with existing booking
    400 Bad Request - Offering has no sessions
    404 Not Found - Parent or offering not found
    409 Conflict - Booking conflict (concurrent booking)

## 1.3 Get Parent's Bookings

**GET** `/parents/{parentId}/bookings`

Returns all bookings for a parent with session details.

**Path Parameters:**

| Name     | Type | Description      |
| -------- | ---- | ---------------- |
| parentId | Long | ID of the parent |

**Query Parameters (optional):**

| Name     | Type    | Description                                                   |
| -------- | ------- | ------------------------------------------------------------- |
| upcoming | Boolean | If true, returns only future sessions (default: false)        |
| status   | String  | Filter by status: `"CONFIRMED"`, `"CANCELLED"`, `"COMPLETED"` |

**Response:** `200 OK`

```json
[
  {
    "bookingId": 125,
    "offeringName": "Math Grade 5 - Summer Batch",
    "courseName": "Math Grade 5",
    "teacherName": "John Smith",
    "status": "CONFIRMED",
    "bookedAtLocal": "2026-05-29 14:00:00 IST",
    "sessions": [
      {
        "sessionId": 45,
        "startTimeLocal": "2026-06-07 14:00:00 IST",
        "endTimeLocal": "2026-06-07 15:00:00 IST",
        "status": "UPCOMING"
      },
      {
        "sessionId": 46,
        "startTimeLocal": "2026-06-14 14:00:00 IST",
        "endTimeLocal": "2026-06-14 15:00:00 IST",
        "status": "UPCOMING"
      }
    ]
  }
]
```

**Errors:**
404 Not Found - Parent not found

---

## 1.4 Cancel Booking

**DELETE** `/parents/{parentId}/bookings/{bookingId}`

Cancels an existing booking.

**Path Parameters:**

| Name      | Type | Description                 |
| --------- | ---- | --------------------------- |
| parentId  | Long | ID of the parent            |
| bookingId | Long | ID of the booking to cancel |

**Response:** `200 OK`

```json
{
  "bookingId": 125,
  "status": "CANCELLED",
  "message": "Booking cancelled successfully",
  "cancelledAt": "2026-05-29T09:15:00Z",
  "cancelledAtLocal": "2026-05-29 14:45:00 IST"
}
```

**Errors:**
400 Bad Request - Cannot cancel booking (e.g., session already started)
404 Not Found - Parent or booking not found
403 Forbidden - Booking doesn't belong to this parent

---

## 1.5 Get Parent Profile

**GET** `/parents/{parentId}`

Returns parent profile information.

**Path Parameters:**

| Name     | Type | Description      |
| -------- | ---- | ---------------- |
| parentId | Long | ID of the parent |

**Response:** `200 OK`

```json
{
  "id": 5,
  "name": "Sarah Johnson",
  "timezone": "Asia/Kolkata",
  "email": "sarah@example.com",
  "phone": "+91 9876543210",
  "totalBookings": 3,
  "activeBookings": 2,
  "memberSince": "2026-01-15T00:00:00Z"
}
```

**Errors:**
404 Not Found - Parent not found

---

# 2. TEACHER ENDPOINTS

## 2.1 Create Offering

**POST** `/teachers/{teacherId}/offerings`

Creates a new course offering.

**Path Parameters:**

| Name      | Type | Description       |
| --------- | ---- | ----------------- |
| teacherId | Long | ID of the teacher |

**Request Body:**

```json
{
  "name": "Math Grade 5 - Summer Batch",
  "courseId": 1,
  "description": "Intensive summer math course for Grade 5"
}
```

**Response:** `201 Created`

```json
{
  "offeringId": 10,
  "name": "Math Grade 5 - Summer Batch",
  "courseId": 1,
  "courseName": "Math Grade 5",
  "teacherId": 3,
  "teacherName": "John Smith",
  "status": "DRAFT",
  "createdAt": "2026-05-29T08:30:00Z",
  "createdAtLocal": "2026-05-29 14:00:00 IST"
}
```

**Errors:**
404 Not Found - Teacher or course not found
400 Bad Request - Offering name already exists for this teacher

---

## 2.2 Add Sessions to Offering

**POST** `/offerings/{offeringId}/sessions`

Adds sessions to an existing offering.

**Path Parameters:**

| Name       | Type | Description        |
| ---------- | ---- | ------------------ |
| offeringId | Long | ID of the offering |

**Query Parameters:**

| Name     | Type   | Description                                 |
| -------- | ------ | ------------------------------------------- |
| timezone | String | Teacher's timezone (e.g., `"Asia/Kolkata"`) |

**Request Body:**

```json
[
  {
    "startTime": "2026-06-07T14:00:00",
    "endTime": "2026-06-07T15:00:00"
  },
  {
    "startTime": "2026-06-14T14:00:00",
    "endTime": "2026-06-14T15:00:00"
  },
  {
    "startTime": "2026-06-21T14:00:00",
    "endTime": "2026-06-21T15:00:00"
  }
]
```

**Response:** `201 Created`

```json
{
  "offeringId": 10,
  "offeringName": "Math Grade 5 - Summer Batch",
  "sessions": [
    {
      "sessionId": 45,
      "startTimeLocal": "2026-06-07 14:00:00 IST",
      "endTimeLocal": "2026-06-07 15:00:00 IST",
      "startTimeUTC": "2026-06-07T08:30:00Z",
      "endTimeUTC": "2026-06-07T09:30:00Z"
    },
    {
      "sessionId": 46,
      "startTimeLocal": "2026-06-14 14:00:00 IST",
      "endTimeLocal": "2026-06-14 15:00:00 IST",
      "startTimeUTC": "2026-06-14T08:30:00Z",
      "endTimeUTC": "2026-06-14T09:30:00Z"
    }
  ],
  "totalSessionsAdded": 3
}
```

**Errors:**
400 Bad Request - Session times overlap within offering
400 Bad Request - Session end time is before start time
404 Not Found - Offering not found

---

## 2.3 Get Teacher's Offerings

**GET** `/teachers/{teacherId}/offerings`

Returns all offerings created by a teacher.

**Path Parameters:**

| Name      | Type | Description       |
| --------- | ---- | ----------------- |
| teacherId | Long | ID of the teacher |

**Query Parameters (optional):**

| Name            | Type    | Description                                              |
| --------------- | ------- | -------------------------------------------------------- |
| includeSessions | Boolean | Include session details (default: true)                  |
| status          | String  | Filter by status: `"DRAFT"`, `"PUBLISHED"`, `"ARCHIVED"` |

**Response:** `200 OK`

```json
[
  {
    "offeringId": 10,
    "name": "Math Grade 5 - Summer Batch",
    "courseName": "Math Grade 5",
    "totalSessions": 3,
    "totalBookings": 5,
    "status": "PUBLISHED",
    "sessions": [
      {
        "sessionId": 45,
        "startTimeLocal": "2026-06-07 14:00:00 IST",
        "endTimeLocal": "2026-06-07 15:00:00 IST",
        "bookedCount": 5,
        "capacity": 10
      }
    ],
    "createdAt": "2026-05-20T10:00:00Z"
  }
]
```

**Errors:**
404 Not Found - Teacher not found

---

## 2.4 Update Session

**PUT** `/sessions/{sessionId}`

Updates a session's time.

**Path Parameters:**

| Name      | Type | Description       |
| --------- | ---- | ----------------- |
| sessionId | Long | ID of the session |

**Query Parameters:**

| Name     | Type   | Description        |
| -------- | ------ | ------------------ |
| timezone | String | Teacher's timezone |

**Request Body:**

```json
{
  "startTime": "2026-06-07T15:00:00",
  "endTime": "2026-06-07T16:00:00"
}
```

**Response:** `200 OK`

```json
{
  "sessionId": 45,
  "offeringId": 10,
  "oldStartTimeLocal": "2026-06-07 14:00:00 IST",
  "oldEndTimeLocal": "2026-06-07 15:00:00 IST",
  "newStartTimeLocal": "2026-06-07 15:00:00 IST",
  "newEndTimeLocal": "2026-06-07 16:00:00 IST",
  "affectedBookings": 5,
  "message": "Session updated. 5 parents notified of time change."
}
```

**Errors:**
400 Bad Request - New time conflicts with other sessions
400 Bad Request - Session already started (cannot update)
404 Not Found - Session not found

---

## 2.5 Delete Session

**DELETE** `/sessions/{sessionId}`

Deletes a session (only if no bookings exist).

**Path Parameters:**

| Name      | Type | Description       |
| --------- | ---- | ----------------- |
| sessionId | Long | ID of the session |

**Response:** `200 OK`

```json
{
  "sessionId": 45,
  "offeringId": 10,
  "message": "Session deleted successfully",
  "wasBooked": false
}
```

**Errors:**
400 Bad Request - Cannot delete session with existing bookings
404 Not Found - Session not found

---

## 2.6 Get Teacher Profile

**GET** `/teachers/{teacherId}`

Returns teacher profile information.

**Path Parameters:**

| Name      | Type | Description       |
| --------- | ---- | ----------------- |
| teacherId | Long | ID of the teacher |

**Response:** `200 OK`

```json
{
  "id": 3,
  "name": "John Smith",
  "timezone": "Asia/Kolkata",
  "email": "john.smith@school.com",
  "phone": "+91 9876543211",
  "totalOfferings": 5,
  "activeOfferings": 3,
  "totalStudents": 45,
  "memberSince": "2025-01-10T00:00:00Z"
}
```

**Errors:**
404 Not Found - Teacher not found

---

# 3. TEST DATA ENDPOINTS

## 3.1 Initialize Test Data

**POST** `/test-data/init`

Populates database with sample teachers, courses, offerings, and parents.

**Request Body (optional):**

```json
{
  "resetExisting": true,
  "dataSize": "MEDIUM"
}
```

**Response:** `201 Created`

```json
{
  "message": "Test data initialized successfully",
  "dataCreated": {
    "teachers": 3,
    "courses": 5,
    "offerings": 8,
    "sessions": 24,
    "parents": 10,
    "bookings": 12
  },
  "timestamp": "2026-05-29T08:30:00Z"
}
```

---

## 3.2 Clear All Test Data

**DELETE** `/test-data/clear`

Removes all test data from database.

**Response:** `200 OK`

```json
{
  "message": "All test data cleared successfully",
  "recordsDeleted": 62,
  "timestamp": "2026-05-29T09:00:00Z"
}
```

---

## 3.3 Get System Status

**GET** `/test-data/status`

Returns current database statistics.

**Response:** `200 OK`

```json
{
  "status": "READY",
  "counts": {
    "teachers": 3,
    "courses": 5,
    "offerings": 8,
    "sessions": 24,
    "parents": 10,
    "bookings": 12
  },
  "database": "MySQL 8.4",
  "flywayMigrations": 6,
  "lastMigration": "2026-05-28T23:11:42Z"
}
```

---

# 4. COMMON RESPONSE FORMATS

## Success Response (`200 OK`)

```json
{
  "success": true,
  "data": {},
  "timestamp": "2026-05-29T08:30:00Z"
}
```

## Error Response (`4xx/5xx`)

```json
{
  "success": false,
  "error": {
    "code": "BOOKING_001",
    "message": "Time conflict with existing booking",
    "details": "Session on 2026-06-07 14:00:00 conflicts with existing booking",
    "timestamp": "2026-05-29T08:30:00Z"
  }
}
```

---

# 5. HTTP STATUS CODES

| Status             | Meaning          | When to Use                               |
| ------------------ | ---------------- | ----------------------------------------- |
| 200 OK             | Success          | GET, PUT, DELETE successful               |
| 201 Created        | Resource created | POST successful                           |
| 400 Bad Request    | Client error     | Invalid input, validation failed          |
| 403 Forbidden      | Not authorized   | Parent trying to cancel another's booking |
| 404 Not Found      | Resource missing | Parent/teacher/offering not found         |
| 409 Conflict       | State conflict   | Concurrent booking, duplicate booking     |
| 500 Internal Error | Server error     | Unexpected exception                      |

---

# 6. IMPLEMENTATION CHECKLIST

## Priority 1 (Core functionality)

* `GET /parents/{parentId}/offerings` - View available offerings
* `POST /parents/{parentId}/bookings` - Book offering
* `GET /parents/{parentId}/bookings` - View bookings
* `POST /teachers/{teacherId}/offerings` - Create offering
* `POST /offerings/{offeringId}/sessions` - Add sessions
* `GET /teachers/{teacherId}/offerings` - View offerings

## Priority 2 (Enhancements)

* `DELETE /parents/{parentId}/bookings/{bookingId}` - Cancel booking
* `PUT /sessions/{sessionId}` - Update session
* `GET /parents/{parentId}` - Parent profile
* `GET /teachers/{teacherId}` - Teacher profile

## Priority 3 (Testing)

* `POST /test-data/init` - Seed database
* `DELETE /test-data/clear` - Clear test data
* `GET /test-data/status` - System status


