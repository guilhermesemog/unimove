package com.guilhermesemog.unimove;

import com.guilhermesemog.unimove.dto.analytics.OperationalAnalyticsResponse;
import com.guilhermesemog.unimove.service.OperationalAnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OperationalAnalyticsIntegrationTest {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Sao_Paulo");

    @Autowired private JdbcTemplate jdbc;
    @Autowired private OperationalAnalyticsService service;

    @Test
    @Transactional
    void shouldCalculateMetricsAndRespectTheLocalDayBoundary() {
        clearOperationalDataForScenario();
        LocalDate day = LocalDate.now(BUSINESS_ZONE);
        Instant start = day.atStartOfDay(BUSINESS_ZONE).toInstant();
        Instant demandCreated = start.plusSeconds(8 * 3600L);
        Instant tripCreated = start.plusSeconds(10 * 3600L);
        Instant assignedAt = start.plusSeconds(11 * 3600L);

        UUID university = UUID.randomUUID();
        UUID stop = UUID.randomUUID();
        UUID vehicle = UUID.randomUUID();
        UUID recurringDemand = UUID.randomUUID();
        UUID manualDemand = UUID.randomUUID();
        UUID outsideDemand = UUID.randomUUID();
        UUID plan = UUID.randomUUID();
        jdbc.update("INSERT INTO universities(id,name,address) VALUES (?,?,?)", university, unique("Analytics University"), unique("Campus"));
        jdbc.update("INSERT INTO boarding_stops(id,local) VALUES (?,?)", stop, unique("Analytics Stop"));
        jdbc.update("INSERT INTO vehicles(id,plate,capacity) VALUES (?,?,4)", vehicle, unique("ANL").substring(0, 8));
        jdbc.update("""
                INSERT INTO recurrence_plans(id,name,destination_id,start_date,end_date,closing_time,departure_time,
                  arrival_time,return_departure_time,return_arrival_time,horizon_weeks,status,next_eligible_date)
                VALUES (?,?,?, ?,?, '07:00','08:00','09:00','17:00','18:00',8,'ACTIVE',?)
                """, plan, unique("Plan"), university, day, day.plusDays(30), day);

        insertDemand(recurringDemand, university, plan, day, demandCreated);
        insertDemand(manualDemand, university, null, day.plusDays(1), demandCreated.plusSeconds(60));
        insertDemand(outsideDemand, university, null, day.plusDays(2), start.minusSeconds(10));

        UUID approvedStudent = insertStudent(university);
        UUID pendingStudent = insertStudent(university);
        UUID cancelledStudent = insertStudent(university);
        UUID outsideStudent = insertStudent(university);
        insertBooking(approvedStudent, recurringDemand, university, stop, "APPROVED", demandCreated.plusSeconds(120), null);
        insertBooking(pendingStudent, recurringDemand, university, stop, "PENDING", demandCreated.plusSeconds(180), null);
        insertBooking(cancelledStudent, recurringDemand, university, stop, "CANCELLED", demandCreated.plusSeconds(240), demandCreated.plusSeconds(300));
        insertBooking(outsideStudent, outsideDemand, university, stop, "APPROVED", start.minusSeconds(1), null);

        jdbc.update("""
                INSERT INTO trips(id,interest_list_id,status,vehicle_id,created_at,updated_at,assigned_at,status_changed_at,version)
                VALUES (?,?,'SCHEDULED',?,?,?,?,?,0)
                """, UUID.randomUUID(), recurringDemand, vehicle, timestamp(tripCreated), timestamp(tripCreated),
                timestamp(assignedAt), timestamp(tripCreated));
        jdbc.update("""
                INSERT INTO audit_events(id,action,entity_type,entity_id,occurred_at,correlation_id,metadata)
                VALUES (?,'RECURRENCE_PLAN_GENERATED','RecurrencePlan',?,?,?,?)
                """, UUID.randomUUID(), plan, timestamp(demandCreated), UUID.randomUUID(), "{\"conflicts\":2}");

        OperationalAnalyticsResponse result = service.get(null, day, day);

        assertThat(result.period().timezone()).isEqualTo("America/Sao_Paulo");
        assertThat(result.period().complete()).isFalse();
        assertThat(result.summary().confirmedBookings().value()).isEqualTo(2.0);
        assertThat(result.summary().cancellationRate().value()).isEqualTo(33.33);
        assertThat(result.summary().capacityUtilization().value()).isEqualTo(50.0);
        assertThat(result.summary().planningLeadTime().value()).isEqualTo(2.0);
        assertThat(result.summary().assignmentLeadTime().value()).isEqualTo(1.0);
        assertThat(result.summary().tripsAtRisk().value()).isEqualTo(1.0);
        assertThat(result.trend()).singleElement().satisfies(point -> {
            assertThat(point.bookingsCreated()).isEqualTo(3);
            assertThat(point.bookingsCancelled()).isEqualTo(1);
            assertThat(point.tripsCreated()).isEqualTo(1);
        });
        assertThat(result.universities()).singleElement().satisfies(metric -> {
            assertThat(metric.bookings()).isEqualTo(3);
            assertThat(metric.trips()).isEqualTo(1);
        });
        assertThat(result.recurrence().activePlans()).isEqualTo(1);
        assertThat(result.recurrence().generatedOccurrences().value()).isEqualTo(1.0);
        assertThat(result.recurrence().coverage().value()).isEqualTo(50.0);
        assertThat(result.recurrence().conflictsAvoided().value()).isEqualTo(2.0);
        assertThat(result.exceptions().missingDriver()).isEqualTo(1);
        assertThat(result.exceptions().missingVehicle()).isZero();
        assertThat(result.exceptions().insufficientCapacity()).isZero();
    }

    private UUID insertStudent(UUID university) {
        UUID id = UUID.randomUUID();
        String token = UUID.randomUUID().toString().replace("-", "");
        jdbc.update("INSERT INTO users(id,cpf,password,first_name,last_name,phone,active,role) VALUES (?,?,?,?,?,?,true,'STUDENT')",
                id, token.substring(0, 11), "password", "Analytics", "Student", token.substring(11, 22));
        jdbc.update("INSERT INTO students(user_id,period,course,address,university_id) VALUES (?,1,'Course','Address',?)", id, university);
        return id;
    }

    private void clearOperationalDataForScenario() {
        jdbc.update("DELETE FROM audit_events");
        jdbc.update("DELETE FROM trip_students");
        jdbc.update("DELETE FROM trip_interest_lists");
        jdbc.update("DELETE FROM trip_boarding_points");
        jdbc.update("DELETE FROM trip_stop_points");
        jdbc.update("DELETE FROM trips");
        jdbc.update("DELETE FROM bookings");
        jdbc.update("DELETE FROM interest_lists");
        jdbc.update("DELETE FROM recurrence_plan_days");
        jdbc.update("DELETE FROM recurrence_plans");
    }

    private void insertDemand(UUID id, UUID university, UUID plan, LocalDate date, Instant createdAt) {
        jdbc.update("""
                INSERT INTO interest_lists(id,reference_date,closing_time,departure_time,arrival_time,return_departure_time,
                  return_arrival_time,destination_id,list_status,created_at,updated_at,status_changed_at,version,
                  recurrence_plan_id,occurrence_date)
                VALUES (?,?, '07:00','08:00','09:00','17:00','18:00',?,'OPEN',?,?,?,0,?,?)
                """, id, date, university, timestamp(createdAt), timestamp(createdAt), timestamp(createdAt), plan,
                plan == null ? null : date);
    }

    private void insertBooking(UUID student, UUID demand, UUID university, UUID stop, String status,
                               Instant createdAt, Instant cancelledAt) {
        jdbc.update("""
                INSERT INTO bookings(id,student_id,interest_list_id,booking_status,trip_type,destination_id,
                  boarding_location_id,created_at,updated_at,version,cancelled_at)
                VALUES (?,?,?,?,'ROUND_TRIP',?,?,?,?,0,?)
                """, UUID.randomUUID(), student, demand, status, university, stop, timestamp(createdAt),
                timestamp(createdAt), cancelledAt == null ? null : timestamp(cancelledAt));
    }

    private Timestamp timestamp(Instant instant) { return Timestamp.from(instant); }
    private String unique(String prefix) { return prefix + " " + UUID.randomUUID(); }
}
