package com.guilhermesemog.unimove;

import com.guilhermesemog.unimove.dto.analytics.OperationalAnalyticsResponse;
import com.guilhermesemog.unimove.dto.audit.AuditQuery;
import com.guilhermesemog.unimove.dto.booking.BookingCreate;
import com.guilhermesemog.unimove.dto.driver.DriverOperationResponse;
import com.guilhermesemog.unimove.dto.recurrence.RecurrenceGenerationResponse;
import com.guilhermesemog.unimove.dto.recurrence.RecurrencePlanRequest;
import com.guilhermesemog.unimove.dto.recurrence.RecurrencePlanResponse;
import com.guilhermesemog.unimove.dto.telemetry.TelemetryBatchRequest;
import com.guilhermesemog.unimove.dto.telemetry.TelemetryEventRequest;
import com.guilhermesemog.unimove.dto.telemetry.TelemetrySummaryResponse;
import com.guilhermesemog.unimove.dto.trip.TripAssignmentUpdate;
import com.guilhermesemog.unimove.dto.trip.TripCreate;
import com.guilhermesemog.unimove.dto.trip.TripResponse;
import com.guilhermesemog.unimove.model.*;
import com.guilhermesemog.unimove.model.enums.*;
import com.guilhermesemog.unimove.repository.*;
import com.guilhermesemog.unimove.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "unimove.phase-five.telemetry.enabled=true")
@AutoConfigureMockMvc
class PhaseFiveConsolidationIntegrationTest {
    @Autowired private RecurrencePlanService recurrenceService;
    @Autowired private BookingService bookingService;
    @Autowired private TripService tripService;
    @Autowired private OutboxClaimService outboxClaimService;
    @Autowired private OutboxEventProcessor outboxProcessor;
    @Autowired private AuditQueryService auditQueryService;
    @Autowired private OperationalAnalyticsService analyticsService;
    @Autowired private TelemetryService telemetryService;
    @Autowired private UserRepository userRepository;
    @Autowired private UniversityRepository universityRepository;
    @Autowired private BoardingStopRepository boardingStopRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private ConductorRepository conductorRepository;
    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private InterestListRepository interestListRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private TripRepository tripRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private OutboxEventRepository outboxRepository;
    @Autowired private Clock businessClock;
    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCompleteTheCriticalJourneyAcrossAllThreeRolesAndKeepFailuresIsolated() {
        User admin = userRepository.save(syntheticUser(Role.ADMIN, "Admin"));
        University university = universityRepository.save(new University(unique("Consolidation campus"), unique("Synthetic address")));
        BoardingStop stop = boardingStopRepository.save(new BoardingStop(unique("Synthetic stop")));
        Student student = studentRepository.save(new Student(syntheticUser(Role.STUDENT, "Student"), 3L,
                "Synthetic course", "Synthetic student address", university, stop));
        Conductor conductor = conductorRepository.save(new Conductor(syntheticUser(Role.CONDUCTOR, "Driver"),
                unique("LICENSE"), LocalDate.now(businessClock).plusYears(2)));
        Vehicle vehicle = vehicleRepository.save(new Vehicle(unique("TST").substring(0, 8), 20));

        authenticate(admin);
        LocalDate firstDate = LocalDate.now(businessClock).plusDays(2);
        interestListRepository.save(new InterestList(firstDate, LocalTime.of(23, 0), LocalTime.of(8, 0),
                LocalTime.of(9, 0), LocalTime.of(17, 0), LocalTime.of(18, 0), university));
        RecurrencePlanRequest request = new RecurrencePlanRequest(unique("Consolidation plan"), university.getId(),
                new LinkedHashSet<>(Arrays.asList(DayOfWeek.values())), firstDate, firstDate.plusDays(2),
                LocalTime.of(23, 0), LocalTime.of(8, 0), LocalTime.of(9, 0),
                LocalTime.of(17, 0), LocalTime.of(18, 0), 8);

        assertThat(recurrenceService.preview(request).conflictCount()).isEqualTo(1);
        RecurrencePlanResponse plan = recurrenceService.create(request);
        RecurrenceGenerationResponse firstGeneration = recurrenceService.generate(plan.id());
        RecurrenceGenerationResponse repeatedGeneration = recurrenceService.generate(plan.id());
        assertThat(firstGeneration.createdCount()).isEqualTo(2);
        assertThat(repeatedGeneration.createdCount()).isZero();
        assertThat(repeatedGeneration.skippedCount()).isEqualTo(2);

        InterestList generatedDemand = interestListRepository
                .findAllByDestination_IdAndReferenceDateBetween(university.getId(), firstDate, firstDate.plusDays(2))
                .stream().filter(demand -> demand.getRecurrencePlan() != null).findFirst().orElseThrow();

        authenticate(student.getUser());
        BookingCreate bookingRequest = new BookingCreate(generatedDemand.getId(), TripType.ROUND_TRIP,
                university.getId(), stop.getId());
        bookingService.create(currentAuthentication(), bookingRequest);
        Booking booking = bookingRepository.findByStudent_IdAndInterestList_Id(student.getId(), generatedDemand.getId())
                .orElseThrow();
        bookingService.deleteById(booking.getId());
        assertThat(bookingRepository.findById(booking.getId()).orElseThrow().getBookingStatus())
                .isEqualTo(BookingStatus.CANCELLED);
        bookingService.create(currentAuthentication(), bookingRequest);
        assertThat(bookingRepository.findById(booking.getId()).orElseThrow().getBookingStatus())
                .isEqualTo(BookingStatus.APPROVED);

        authenticate(admin);
        TripResponse trip = tripService.create(new TripCreate(generatedDemand.getId()));
        tripService.updateAssignment(trip.id(), new TripAssignmentUpdate(conductor.getId(), vehicle.getId()));

        authenticate(conductor.getUser());
        DriverOperationResponse operation = tripService.getDriverOperation(currentAuthentication(), trip.id());
        assertThat(operation.passengers()).singleElement()
                .satisfies(passenger -> assertThat(passenger.bookingId()).isEqualTo(booking.getId()));

        drainOutbox();
        assertThat(notificationRepository.countByRecipient_IdAndReadAtIsNull(conductor.getId())).isPositive();
        assertThat(notificationRepository.countByRecipient_IdAndReadAtIsNull(student.getId())).isPositive();

        authenticate(admin);
        assertThat(auditQueryService.search(new AuditQuery(null, null, Set.of(), null, null, null,
                Set.of(plan.id(), booking.getId(), trip.id()), null), 0, 100, "asc").getContent())
                .extracting(event -> event.entityId()).contains(plan.id(), booking.getId(), trip.id());
        OperationalAnalyticsResponse analytics = analyticsService.get(7, null, null);
        assertThat(analytics.summary().confirmedBookings().value()).isNotNull();
        assertThat(analytics.recurrence().activePlans()).isPositive();

        Instant now = businessClock.instant();
        telemetryService.ingest(student.getUser().getCpf(), Role.STUDENT, batch(
                event(TelemetryEventName.booking_started, now, Map.of("screen", "booking_checkout")),
                event(TelemetryEventName.booking_completed, now,
                        Map.of("screen", "booking_checkout", "result", "success", "durationMs", 900))));
        telemetryService.ingest(admin.getCpf(), Role.ADMIN, batch(
                event(TelemetryEventName.recurrence_published, now,
                        Map.of("screen", "recurrence_editor", "result", "success")),
                event(TelemetryEventName.assignment_completed, now,
                        Map.of("screen", "admin_operation", "result", "success", "durationMs", 400))));
        telemetryService.ingest(conductor.getUser().getCpf(), Role.CONDUCTOR, batch(
                event(TelemetryEventName.operation_opened, now,
                        Map.of("screen", "operation_detail", "durationMs", 300)),
                event(TelemetryEventName.manifest_viewed, now,
                        Map.of("screen", "operation_detail", "step", "manifest"))));
        TelemetrySummaryResponse telemetry = telemetryService.summary(7);
        assertThat(telemetry.bookingCompleted()).isPositive();
        assertThat(telemetry.assignmentsCompleted()).isPositive();
        assertThat(telemetry.manifestsViewed()).isPositive();
        assertSensitiveValueAbsent(student.getUser().getCpf());
        assertSensitiveValueAbsent(student.getUser().getPhone());
        assertSensitiveValueAbsent("synthetic-password");
        assertThat(jdbc.queryForList("""
                SELECT column_name FROM information_schema.columns
                WHERE table_schema = 'public' AND table_name = 'product_telemetry_events'
                """, String.class)).doesNotContain("user_id", "actor_id", "entity_id");

        OutboxEvent malformed = outboxRepository.save(new OutboxEvent(BusinessEventType.BOOKING_CREATED,
                "Booking", trip.id(), admin.getId(), Role.ADMIN.name(), UUID.randomUUID(), "{}",
                "consolidation-failure:" + UUID.randomUUID()));
        drainOutbox();
        assertThat(outboxRepository.findById(malformed.getId()).orElseThrow().getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(tripRepository.findById(trip.id())).isPresent();
    }

    @Test
    void shouldKeepAdministrativeSurfacesPrivate() throws Exception {
        userRepository.save(syntheticUser(Role.ADMIN, "ExistingAdmin"));
        mockMvc.perform(get("/admin/recurrence-plans").with(user("student").roles("STUDENT")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/audit-events").with(user("driver").roles("CONDUCTOR")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/analytics").with(user("student").roles("STUDENT")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/telemetry/summary").with(user("driver").roles("CONDUCTOR")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/conductors/list").with(user("student").roles("STUDENT")))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/auth/register").contentType(APPLICATION_JSON).content("""
                {"cpf":"59999999999","password":"password123","firstName":"Unauthorized",
                 "lastName":"Admin","phone":"59999999999"}
                """))
                .andExpect(status().isForbidden());
    }

    private void drainOutbox() {
        for (int pass = 0; pass < 10; pass++) {
            List<UUID> claimed = outboxClaimService.claim();
            if (claimed.isEmpty()) return;
            claimed.forEach(outboxProcessor::process);
        }
        throw new IllegalStateException("Outbox did not drain within the consolidation test limit");
    }

    private void assertSensitiveValueAbsent(String value) {
        String pattern = "%" + value.toLowerCase(Locale.ROOT) + "%";
        Long matches = jdbc.queryForObject("""
                SELECT
                  (SELECT count(*) FROM audit_events
                   WHERE lower(concat_ws(' ', previous_state, resulting_state, metadata)) LIKE ?)
                  +
                  (SELECT count(*) FROM outbox_events WHERE lower(payload) LIKE ?)
                  +
                  (SELECT count(*) FROM product_telemetry_events WHERE lower(properties::text) LIKE ?)
                """, Long.class, pattern, pattern, pattern);
        assertThat(matches).isZero();
    }

    private void authenticate(User user) {
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                user.getCpf(), "", AuthorityUtils.createAuthorityList("ROLE_" + user.getRole().name())));
    }

    private Authentication currentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private TelemetryBatchRequest batch(TelemetryEventRequest... events) {
        return new TelemetryBatchRequest(List.of(events));
    }

    private TelemetryEventRequest event(TelemetryEventName name, Instant now, Map<String, Object> properties) {
        return new TelemetryEventRequest(UUID.randomUUID(), name, UUID.randomUUID(), now, properties);
    }

    private User syntheticUser(Role role, String firstName) {
        String digits = Long.toString(ThreadLocalRandom.current().nextLong(10_000_000_000L, 100_000_000_000L));
        return new User(digits, "synthetic-password", firstName, "Synthetic", digits, true, role);
    }

    private String unique(String prefix) {
        return prefix + " " + UUID.randomUUID();
    }
}
