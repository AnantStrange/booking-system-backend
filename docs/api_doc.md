# API Documentation

## Base URL

```
http://localhost:8080/api
```

## Authentication

Not required. All endpoints are public.

---

# Teacher Endpoints

## POST /teachers

Create a new teacher.

### Request Body

```json 
{
  "name": "John Doe",
  "timezone": "America/New_York"
}
```

### Response (201 Created)

```json 
{
  "id": 1,
  "name": "John Doe",
  "timezone": "America/New_York",
  "createdAt": "2026-05-30 17:02:53 EDT"
}
```

---

## GET /teachers/{teacherId}

Get teacher profile.

### Response (200 OK)

```json 
{
  "id": 1,
  "name": "John Doe",
  "timezone": "America/New_York",
  "createdAt": "2026-05-30 17:02:53 EDT"
}
```

---

## POST /teachers/{teacherId}/courses

Create a course for a teacher.

### Request Body

```json 
{
  "name": "Mathematics Grade 5",
  "description": "Advanced mathematics including algebra"
}
```

### Response (201 Created)

```json 
{
  "id": 1,
  "name": "Mathematics Grade 5",
  "description": "Advanced mathematics including algebra",
  "createdAt": "2026-05-30T10:00:00Z",
  "offerings": []
}
```

---

## POST /teachers/{teacherId}/offerings

Create an offering for a teacher.

### Request Body

```json 
{
  "name": "Summer Batch 2026",
  "courseId": 1
}
```

### Response (201 Created)

```json 
{
  "id": 1,
  "name": "Summer Batch 2026",
  "courseId": 1,
  "teacherId": 1,
  "createdAt": "2026-05-30T10:00:00Z",
  "sessions": []
}
```

---

## GET /teachers/{teacherId}/offerings

Get all offerings for a teacher.

### Response (200 OK)

```json 
[
  {
    "id": 1,
    "name": "Summer Batch 2026",
    "courseId": 1,
    "teacherId": 1,
    "createdAt": "2026-05-30T10:00:00Z",
    "sessions": [
      {
        "id": 1,
        "startTimeLocal": "2026-06-07 10:00:00 EDT",
        "endTimeLocal": "2026-06-07 12:00:00 EDT"
      }
    ]
  }
]
```

---

## POST /teachers/offerings/{offeringId}/sessions

Add sessions to an offering.

### Query Parameters

* `timezone` - Teacher's timezone (e.g., America/New_York)

### Request Body

```json 
[
  {
    "startTime": "2026-06-07T10:00:00",
    "endTime": "2026-06-07T12:00:00"
  },
  {
    "startTime": "2026-06-14T10:00:00",
    "endTime": "2026-06-14T12:00:00"
  }
]
```

### Response (201 Created)

```json 
[
  {
    "id": 1,
    "startTimeLocal": "2026-06-07 10:00:00 EDT",
    "endTimeLocal": "2026-06-07 12:00:00 EDT",
    "createdAt": "2026-05-30T10:00:00Z"
  },
  {
    "id": 2,
    "startTimeLocal": "2026-06-14 10:00:00 EDT",
    "endTimeLocal": "2026-06-14 12:00:00 EDT",
    "createdAt": "2026-05-30T10:00:00Z"
  }
]
```

---

# Parent Endpoints

## POST /parents

Create a new parent.

### Request Body

```json 
{
  "name": "Alice Brown",
  "timezone": "Asia/Kolkata"
}
```

### Response (201 Created)

```json"
{
  "id": 1,
  "name": "Alice Brown",
  "timezone": "Asia/Kolkata",
  "createdAt": "2026-05-30 19:32:53 IST"
}
```

---

## GET /parents/{parentId}/offerings

Get all available offerings for a parent (excludes already booked offerings).

### Response (200 OK)

```json 
[
  {
    "id": 1,
    "name": "Summer Batch 2026",
    "courseId": 1,
    "teacherId": 1,
    "createdAt": "2026-05-30T10:00:00Z",
    "sessions": [
      {
        "id": 1,
        "startTimeLocal": "2026-06-07 19:30:00 IST",
        "endTimeLocal": "2026-06-07 21:30:00 IST"
      }
    ]
  }
]
```

---

## GET /parents/{parentId}/sessions

Get all sessions from parent's bookings.

### Response (200 OK)

```json 
[
  {
    "id": 1,
    "startTimeLocal": "2026-06-07 19:30:00 IST",
    "endTimeLocal": "2026-06-07 21:30:00 IST",
    "createdAt": "2026-05-30T10:00:00Z"
  }
]
```

---

## GET /parents/{parentId}/bookings

Get all bookings for a parent.

### Response (200 OK)

```json 
[
  {
    "bookingId": 1,
    "status": "CONFIRMED",
    "bookedAtLocal": "2026-05-30 19:35:00 IST",
    "offering": {
      "id": 1,
      "name": "Summer Batch 2026",
      "courseId": 1,
      "teacherId": 1,
      "createdAt": "2026-05-30T10:00:00Z",
      "sessions": [
        {
          "id": 1,
          "startTimeLocal": "2026-06-07 19:30:00 IST",
          "endTimeLocal": "2026-06-07 21:30:00 IST"
        }
      ]
    }
  }
]
```

---

## POST /parents/{parentId}/bookings

Book an offering for a parent.

### Query Parameters

* `offeringId` - ID of offering to book

### Response (201 Created)

```json 
{
  "bookingId": 1,
  "status": "CONFIRMED",
  "bookedAtLocal": "2026-05-30 19:35:00 IST",
  "offering": {
    "id": 1,
    "name": "Summer Batch 2026",
    "courseId": 1,
    "teacherId": 1,
    "sessions": [...]
  }
}
```

### Error Responses

* `404 Not Found` - Parent or offering not found
* `409 Conflict` - Parent already booked this offering
* `409 Conflict` - Time conflict with existing booking
* `422 Unprocessable Entity` - Offering has no sessions

---

# Course Endpoints

## GET /courses

Get all courses.

### Response (200 OK)

```json 
[
  {
    "id": 1,
    "name": "Mathematics Grade 5",
    "description": "Advanced mathematics including algebra",
    "createdAt": "2026-05-30T10:00:00Z",
    "offerings": []
  }
]
```

---

## GET /courses/{courseId}

Get a course with its offerings.

### Response (200 OK)

```json 
{
  "id": 1,
  "name": "Mathematics Grade 5",
  "description": "Advanced mathematics including algebra",
  "createdAt": "2026-05-30T10:00:00Z",
  "offerings": [
    {
      "id": 1,
      "name": "Summer Batch 2026",
      "courseId": 1,
      "teacherId": 1,
      "sessions": [...]
    }
  ]
}
```

---

# Offering Endpoints

## GET /offerings/{offeringId}

Get an offering with its sessions.

### Response (200 OK)

```json 
{
  "id": 1,
  "name": "Summer Batch 2026",
  "courseId": 1,
  "teacherId": 1,
  "createdAt": "2026-05-30T10:00:00Z",
  "sessions": [
    {
      "id": 1,
      "startTimeLocal": "2026-06-07 10:00:00 EDT",
      "endTimeLocal": "2026-06-07 12:00:00 EDT",
      "createdAt": "2026-05-30T10:00:00Z"
    }
  ]
}
```

---

# HTTP Status Codes

| Status                    | Meaning                    |
| ------------------------- | -------------------------- |
| 200 OK                    | Success (GET, PUT, DELETE) |
| 201 Created               | Resource created (POST)    |
| 400 Bad Request           | Invalid input              |
| 404 Not Found             | Resource not found         |
| 409 Conflict              | Duplicate or time conflict |
| 422 Unprocessable Entity  | No sessions in offering    |
| 500 Internal Server Error | Server error               |

---

# Timezone Handling

* All times stored as UTC in database
* Teachers provide session times in their local timezone
* Parents receive all times in their local timezone
* Timezone conversion uses `TimezoneService`

