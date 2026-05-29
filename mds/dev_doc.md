API_CODE.md - Implementation Reference

================================================================================
PARENT ENDPOINTS
================================================================================

1.1 GET /parents/{parentId}/offerings

Input:
- parentId (Long) from URL path

Service method needed:
OfferingRepository.findAvailableForParent(parentId) 
- Excludes offerings parent already booked
- Join with sessions to ensure offering has sessions

Response DTO to create:
AvailableOfferingDTO {
  private Long id;
  private String name;
  private Long courseId;
  private String courseName;
  private Long teacherId;
  private String teacherName;
  private List<SessionSummaryDTO> sessions;
  private Integer totalSessions;
  private Instant createdAt;
}

SessionSummaryDTO {
  private Long id;
  private String startTimeLocal;
  private String endTimeLocal;
}

Controller code:
@GetMapping("/parents/{parentId}/offerings")
public ResponseEntity<List<AvailableOfferingDTO>> getAvailableOfferings(@PathVariable Long parentId) {
    Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new EntityNotFoundException("Parent not found"));
    List<Offering> offerings = offeringRepository.findAllWithSessions();
    List<Booking> existingBookings = bookingRepository.findByParentId(parentId);
    Set<Long> bookedOfferingIds = existingBookings.stream().map(Booking::getOfferingId).collect(Collectors.toSet());
    List<Offering> available = offerings.stream().filter(o -> !bookedOfferingIds.contains(o.getId())).collect(Collectors.toList());
    List<AvailableOfferingDTO> result = convertToDTO(available, parent.getTimezone());
    return ResponseEntity.ok(result);
}

================================================================================

1.2 POST /parents/{parentId}/bookings

Input:
- parentId (Long) from URL path
- Request body: { "offeringId": 10 }

Service method (already exists in BookingService):
Booking bookOffering(Long parentId, Long offeringId)

Response DTO to create:
BookingConfirmationDTO {
  private Long bookingId;
  private Long parentId;
  private Long offeringId;
  private String offeringName;
  private String status;
  private Instant bookedAt;
  private String bookedAtLocal;
  private List<SessionBookingDTO> sessions;
}

SessionBookingDTO {
  private Long sessionId;
  private String startTimeLocal;
  private String endTimeLocal;
}

Controller code:
@PostMapping("/parents/{parentId}/bookings")
public ResponseEntity<BookingConfirmationDTO> createBooking(
    @PathVariable Long parentId,
    @RequestBody CreateBookingRequest request) {
    
    Booking booking = bookingService.bookOffering(parentId, request.getOfferingId());
    BookingConfirmationDTO response = buildConfirmationDTO(booking);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}

CreateBookingRequest DTO:
public class CreateBookingRequest {
    private Long offeringId;
    // getters and setters
}

Exceptions to handle:
- EntityNotFoundException (parent/offering not found) → 404
- IllegalStateException (duplicate booking, time conflict, no sessions) → 400

================================================================================

1.3 GET /parents/{parentId}/bookings

Input:
- parentId (Long) from URL path
- Optional query params: upcoming (Boolean), status (String)

Service method (already exists in BookingService):
List<Booking> getParentBookings(Long parentId)
- Need to modify to support status filtering

Response DTO to create:
ParentBookingDTO {
  private Long bookingId;
  private String offeringName;
  private String courseName;
  private String teacherName;
  private String status;
  private String bookedAtLocal;
  private List<ParentSessionDTO> sessions;
}

ParentSessionDTO {
  private Long sessionId;
  private String startTimeLocal;
  private String endTimeLocal;
  private String status; // UPCOMING, COMPLETED, CANCELLED
}

Controller code:
@GetMapping("/parents/{parentId}/bookings")
public ResponseEntity<List<ParentBookingDTO>> getParentBookings(
    @PathVariable Long parentId,
    @RequestParam(required = false) Boolean upcoming,
    @RequestParam(required = false) String status) {
    
    Parent parent = parentRepository.findById(parentId).orElseThrow(() -> new EntityNotFoundException("Parent not found"));
    List<Booking> bookings = bookingService.getParentBookings(parentId);
    
    // Filter by status if provided
    if (status != null) {
        bookings = bookings.stream().filter(b -> status.equals(b.getStatus())).collect(Collectors.toList());
    }
    
    // Filter upcoming sessions if requested
    if (upcoming != null && upcoming) {
        bookings = filterUpcomingBookings(bookings, parent.getTimezone());
    }
    
    List<ParentBookingDTO> result = convertToParentBookingDTO(bookings, parent.getTimezone());
    return ResponseEntity.ok(result);
}

================================================================================

1.4 DELETE /parents/{parentId}/bookings/{bookingId}

Input:
- parentId (Long) from URL path
- bookingId (Long) from URL path

Service method to create in BookingService:
public void cancelBooking(Long parentId, Long bookingId) {
    Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new EntityNotFoundException("Booking not found"));
    
    if (!booking.getParentId().equals(parentId)) {
        throw new IllegalStateException("Booking does not belong to this parent");
    }
    
    // Check if any session already started
    Offering offering = offeringRepository.findById(booking.getOfferingId()).orElseThrow();
    List<Session> sessions = sessionRepository.findByOfferingId(offering.getId());
    Instant now = timezoneService.nowUTC();
    
    boolean sessionStarted = sessions.stream().anyMatch(s -> s.getStartTime().isBefore(now));
    if (sessionStarted) {
        throw new IllegalStateException("Cannot cancel booking: a session has already started");
    }
    
    bookingRepository.delete(booking);
}

Response DTO:
CancellationResponseDTO {
  private Long bookingId;
  private String status;
  private String message;
  private Instant cancelledAt;
  private String cancelledAtLocal;
}

Controller code:
@DeleteMapping("/parents/{parentId}/bookings/{bookingId}")
public ResponseEntity<CancellationResponseDTO> cancelBooking(
    @PathVariable Long parentId,
    @PathVariable Long bookingId) {
    
    bookingService.cancelBooking(parentId, bookingId);
    CancellationResponseDTO response = buildCancellationResponse(bookingId);
    return ResponseEntity.ok(response);
}

Exceptions:
- EntityNotFoundException → 404
- IllegalStateException (wrong parent, session started) → 400 or 403

================================================================================

1.5 GET /parents/{parentId}

Input:
- parentId (Long) from URL path

Service method to create:
public ParentProfileDTO getParentProfile(Long parentId) {
    Parent parent = parentRepository.findById(parentId).orElseThrow();
    List<Booking> bookings = bookingRepository.findByParentId(parentId);
    long activeBookings = bookings.stream().filter(b -> "CONFIRMED".equals(b.getStatus())).count();
    
    ParentProfileDTO dto = new ParentProfileDTO();
    dto.setId(parent.getId());
    dto.setName(parent.getName());
    dto.setTimezone(parent.getTimezone());
    dto.setEmail(parent.getEmail());
    dto.setPhone(parent.getPhone());
    dto.setTotalBookings(bookings.size());
    dto.setActiveBookings((int) activeBookings);
    dto.setMemberSince(parent.getCreatedAt());
    return dto;
}

ParentProfileDTO {
  private Long id;
  private String name;
  private String timezone;
  private String email;
  private String phone;
  private Integer totalBookings;
  private Integer activeBookings;
  private Instant memberSince;
}

Controller code:
@GetMapping("/parents/{parentId}")
public ResponseEntity<ParentProfileDTO> getParentProfile(@PathVariable Long parentId) {
    ParentProfileDTO profile = parentService.getParentProfile(parentId);
    return ResponseEntity.ok(profile);
}

================================================================================
TEACHER ENDPOINTS
================================================================================

2.1 POST /teachers/{teacherId}/offerings

Input:
- teacherId (Long) from URL path
- Request body: { "name": "Math Summer Batch", "courseId": 1, "description": "..." }

Service method (exists in TeacherService):
Offering createOffering(Offering offering)
- Need to add validation for duplicate offering names per teacher

Response DTO:
TeacherOfferingResponseDTO {
  private Long offeringId;
  private String name;
  private Long courseId;
  private String courseName;
  private Long teacherId;
  private String teacherName;
  private String status;
  private Instant createdAt;
  private String createdAtLocal;
}

Controller code:
@PostMapping("/teachers/{teacherId}/offerings")
public ResponseEntity<TeacherOfferingResponseDTO> createOffering(
    @PathVariable Long teacherId,
    @RequestBody CreateOfferingRequest request) {
    
    Offering offering = new Offering();
    offering.setName(request.getName());
    offering.setCourseId(request.getCourseId());
    offering.setTeacherId(teacherId);
    offering.setStatus("DRAFT");
    
    Offering saved = teacherService.createOffering(offering);
    TeacherOfferingResponseDTO response = buildOfferingResponseDTO(saved);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}

CreateOfferingRequest DTO:
public class CreateOfferingRequest {
    private String name;
    private Long courseId;
    private String description;
    // getters/setters
}

================================================================================

2.2 POST /offerings/{offeringId}/sessions

Input:
- offeringId (Long) from URL path
- Query param: timezone (String) - teacher's timezone
- Request body: List of { "startTime": "2026-06-07T14:00:00", "endTime": "2026-06-07T15:00:00" }

Service method (exists in TeacherService):
List<Session> addSessions(Long offeringId, List<SessionRequest> sessionRequests, String teacherTimezone)

SessionRequest DTO (move from inner class to dto/):
public class SessionRequest {
    private String startTime;
    private String endTime;
    // getters/setters
}

Response DTO:
AddSessionsResponseDTO {
  private Long offeringId;
  private String offeringName;
  private List<AddedSessionDTO> sessions;
  private Integer totalSessionsAdded;
}

AddedSessionDTO {
  private Long sessionId;
  private String startTimeLocal;
  private String endTimeLocal;
  private Instant startTimeUTC;
  private Instant endTimeUTC;
}

Controller code:
@PostMapping("/offerings/{offeringId}/sessions")
public ResponseEntity<AddSessionsResponseDTO> addSessions(
    @PathVariable Long offeringId,
    @RequestParam String timezone,
    @RequestBody List<SessionRequest> sessionRequests) {
    
    List<Session> sessions = teacherService.addSessions(offeringId, sessionRequests, timezone);
    AddSessionsResponseDTO response = buildAddSessionsResponse(offeringId, sessions);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}

Validation to add:
- Check no overlapping sessions within same offering
- Check endTime > startTime

================================================================================

2.3 GET /teachers/{teacherId}/offerings

Input:
- teacherId (Long) from URL path
- Optional query params: includeSessions (Boolean default true), status (String)

Service method (exists in TeacherService):
List<Offering> getTeacherOfferings(Long teacherId)

Response DTO:
TeacherOfferingsListDTO {
  private Long offeringId;
  private String name;
  private String courseName;
  private Integer totalSessions;
  private Integer totalBookings;
  private String status;
  private List<TeacherSessionDTO> sessions;
  private Instant createdAt;
}

TeacherSessionDTO {
  private Long sessionId;
  private String startTimeLocal;
  private String endTimeLocal;
  private Integer bookedCount;
  private Integer capacity;
}

Controller code:
@GetMapping("/teachers/{teacherId}/offerings")
public ResponseEntity<List<TeacherOfferingsListDTO>> getTeacherOfferings(
    @PathVariable Long teacherId,
    @RequestParam(required = false, defaultValue = "true") Boolean includeSessions,
    @RequestParam(required = false) String status) {
    
    Teacher teacher = teacherRepository.findById(teacherId).orElseThrow(() -> new EntityNotFoundException("Teacher not found"));
    List<Offering> offerings = teacherService.getTeacherOfferings(teacherId);
    
    if (status != null) {
        offerings = offerings.stream().filter(o -> status.equals(o.getStatus())).collect(Collectors.toList());
    }
    
    List<TeacherOfferingsListDTO> result = convertToTeacherOfferingsDTO(offerings, teacher.getTimezone(), includeSessions);
    return ResponseEntity.ok(result);
}

================================================================================

2.4 PUT /sessions/{sessionId}

Input:
- sessionId (Long) from URL path
- Query param: timezone (String)
- Request body: { "startTime": "2026-06-07T15:00:00", "endTime": "2026-06-07T16:00:00" }

Service method to create in TeacherService:
public Session updateSession(Long sessionId, UpdateSessionRequest request, String teacherTimezone) {
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new EntityNotFoundException("Session not found"));
    
    Instant now = timezoneService.nowUTC();
    if (session.getStartTime().isBefore(now)) {
        throw new IllegalStateException("Cannot update session that has already started");
    }
    
    ZonedDateTime localStart = ZonedDateTime.parse(request.getStartTime());
    ZonedDateTime localEnd = ZonedDateTime.parse(request.getEndTime());
    Instant newStartUTC = timezoneService.toUTC(localStart, teacherTimezone);
    Instant newEndUTC = timezoneService.toUTC(localEnd, teacherTimezone);
    
    // Check for conflicts with other sessions in same offering
    List<Session> otherSessions = sessionRepository.findByOfferingId(session.getOfferingId());
    for (Session other : otherSessions) {
        if (!other.getId().equals(sessionId)) {
            if (hasTimeConflict(newStartUTC, newEndUTC, other.getStartTime(), other.getEndTime())) {
                throw new IllegalStateException("Time conflicts with existing session: " + other.getId());
            }
        }
    }
    
    session.setStartTime(newStartUTC);
    session.setEndTime(newEndUTC);
    return sessionRepository.save(session);
}

UpdateSessionRequest DTO:
public class UpdateSessionRequest {
    private String startTime;
    private String endTime;
    // getters/setters
}

Response DTO:
UpdateSessionResponseDTO {
  private Long sessionId;
  private Long offeringId;
  private String oldStartTimeLocal;
  private String oldEndTimeLocal;
  private String newStartTimeLocal;
  private String newEndTimeLocal;
  private Integer affectedBookings;
  private String message;
}

Controller code:
@PutMapping("/sessions/{sessionId}")
public ResponseEntity<UpdateSessionResponseDTO> updateSession(
    @PathVariable Long sessionId,
    @RequestParam String timezone,
    @RequestBody UpdateSessionRequest request) {
    
    Session updated = teacherService.updateSession(sessionId, request, timezone);
    UpdateSessionResponseDTO response = buildUpdateSessionResponse(updated, request, timezone);
    return ResponseEntity.ok(response);
}

================================================================================

2.5 DELETE /sessions/{sessionId}

Input:
- sessionId (Long) from URL path

Service method to create:
public void deleteSession(Long sessionId) {
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new EntityNotFoundException("Session not found"));
    
    // Check if any bookings exist for this session's offering
    List<Booking> bookings = bookingRepository.findByOfferingId(session.getOfferingId());
    if (!bookings.isEmpty()) {
        throw new IllegalStateException("Cannot delete session: offering has existing bookings");
    }
    
    sessionRepository.delete(session);
}

Note: Need to add findByOfferingId to BookingRepository:
List<Booking> findByOfferingId(Long offeringId);

Response DTO:
DeleteSessionResponseDTO {
  private Long sessionId;
  private Long offeringId;
  private String message;
  private Boolean wasBooked;
}

Controller code:
@DeleteMapping("/sessions/{sessionId}")
public ResponseEntity<DeleteSessionResponseDTO> deleteSession(@PathVariable Long sessionId) {
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new EntityNotFoundException("Session not found"));
    List<Booking> bookings = bookingRepository.findByOfferingId(session.getOfferingId());
    
    teacherService.deleteSession(sessionId);
    
    DeleteSessionResponseDTO response = new DeleteSessionResponseDTO();
    response.setSessionId(sessionId);
    response.setOfferingId(session.getOfferingId());
    response.setMessage("Session deleted successfully");
    response.setWasBooked(!bookings.isEmpty());
    return ResponseEntity.ok(response);
}

================================================================================

2.6 GET /teachers/{teacherId}

Input:
- teacherId (Long) from URL path

Service method to create:
public TeacherProfileDTO getTeacherProfile(Long teacherId) {
    Teacher teacher = teacherRepository.findById(teacherId).orElseThrow();
    List<Offering> offerings = offeringRepository.findByTeacherId(teacherId);
    int totalStudents = calculateTotalStudents(offerings); // Sum of unique parent IDs across bookings
    
    TeacherProfileDTO dto = new TeacherProfileDTO();
    dto.setId(teacher.getId());
    dto.setName(teacher.getName());
    dto.setTimezone(teacher.getTimezone());
    dto.setEmail(teacher.getEmail());
    dto.setPhone(teacher.getPhone());
    dto.setTotalOfferings(offerings.size());
    dto.setActiveOfferings(offerings.stream().filter(o -> "PUBLISHED".equals(o.getStatus())).count());
    dto.setTotalStudents(totalStudents);
    dto.setMemberSince(teacher.getCreatedAt());
    return dto;
}

TeacherProfileDTO {
  private Long id;
  private String name;
  private String timezone;
  private String email;
  private String phone;
  private Long totalOfferings;
  private Long activeOfferings;
  private Integer totalStudents;
  private Instant memberSince;
}

Controller code:
@GetMapping("/teachers/{teacherId}")
public ResponseEntity<TeacherProfileDTO> getTeacherProfile(@PathVariable Long teacherId) {
    TeacherProfileDTO profile = teacherService.getTeacherProfile(teacherId);
    return ResponseEntity.ok(profile);
}

================================================================================
TEST DATA ENDPOINTS
================================================================================

3.1 POST /test-data/init

Input (optional body):
{ "resetExisting": true, "dataSize": "MEDIUM" }

TestDataInitRequest DTO:
public class TestDataInitRequest {
    private Boolean resetExisting;
    private String dataSize; // SMALL, MEDIUM, LARGE
    // getters/setters
}

Service method to create in TestDataService:
public TestDataResult initTestData(TestDataInitRequest request)

TestDataResult DTO:
public class TestDataResult {
    private String message;
    private Map<String, Integer> dataCreated;
    private Instant timestamp;
    // getters/setters
}

Controller code:
@PostMapping("/test-data/init")
public ResponseEntity<TestDataResult> initTestData(@RequestBody(required = false) TestDataInitRequest request) {
    TestDataResult result = testDataService.initTestData(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
}

================================================================================

3.2 DELETE /test-data/clear

Input: none

Service method:
public ClearDataResult clearAllTestData()

ClearDataResult DTO:
public class ClearDataResult {
    private String message;
    private Integer recordsDeleted;
    private Instant timestamp;
    // getters/setters
}

Controller code:
@DeleteMapping("/test-data/clear")
public ResponseEntity<ClearDataResult> clearTestData() {
    ClearDataResult result = testDataService.clearAllTestData();
    return ResponseEntity.ok(result);
}

Order of deletion (foreign key constraints):
1. Delete bookings
2. Delete sessions
3. Delete offerings
4. Delete courses
5. Delete parents
6. Delete teachers

================================================================================

3.3 GET /test-data/status

Input: none

Service method:
public SystemStatusDTO getSystemStatus()

SystemStatusDTO DTO:
public class SystemStatusDTO {
    private String status;
    private Map<String, Long> counts;
    private String database;
    private Integer flywayMigrations;
    private String lastMigration;
    // getters/setters
}

Controller code:
@GetMapping("/test-data/status")
public ResponseEntity<SystemStatusDTO> getSystemStatus() {
    SystemStatusDTO status = testDataService.getSystemStatus();
    return ResponseEntity.ok(status);
}

================================================================================
ADDITIONAL REPOSITORY METHODS NEEDED
================================================================================

OfferingRepository:
List<Offering> findAllWithSessions();
List<Offering> findByTeacherId(Long teacherId);

BookingRepository:
List<Booking> findByParentId(Long parentId);
List<Booking> findByOfferingId(Long offeringId);
boolean existsByParentIdAndOfferingId(Long parentId, Long offeringId);

SessionRepository:
List<Session> findByOfferingId(Long offeringId);

ParentRepository:
Optional<Parent> findById(Long id);

TeacherRepository:
Optional<Teacher> findById(Long id);

================================================================================
ENTITY FIELDS TO ADD (if missing)
================================================================================

Entity classes may need additional fields:

Offering entity needs:
private String status; // DRAFT, PUBLISHED, ARCHIVED

Booking entity needs:
private String status; // CONFIRMED, CANCELLED, COMPLETED

Parent entity needs:
private String email;
private String phone;

Teacher entity needs:
private String email;
private String phone;

================================================================================
EXCEPTION HANDLING - Global Controller Advice
================================================================================

Create GlobalExceptionHandler class:

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException e) {
        ErrorResponse error = new ErrorResponse("NOT_FOUND", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalStateException e) {
        ErrorResponse error = new ErrorResponse("BAD_REQUEST", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception e) {
        ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

ErrorResponse DTO:
public class ErrorResponse {
    private String code;
    private String message;
    private Instant timestamp;
    // constructor, getters
}
