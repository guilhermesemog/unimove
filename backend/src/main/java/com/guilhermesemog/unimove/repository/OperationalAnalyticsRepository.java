package com.guilhermesemog.unimove.repository;

import com.guilhermesemog.unimove.dto.analytics.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Repository
public class OperationalAnalyticsRepository {
    private final JdbcClient jdbc;

    public OperationalAnalyticsRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public MetricSnapshot snapshot(Instant from, Instant to, LocalDate fromDate,
                                   LocalDate toExclusive, LocalDate today) {
        String sql = """
                WITH booking_metrics AS (
                    SELECT COUNT(*) FILTER (WHERE booking_status <> 'CANCELLED') AS confirmed,
                           COUNT(*) AS created,
                           COUNT(*) FILTER (WHERE booking_status = 'CANCELLED') AS cancelled
                    FROM bookings WHERE created_at >= :fromInstant AND created_at < :toInstant
                ), trip_metrics AS (
                    SELECT COALESCE(SUM((SELECT COUNT(*) FROM bookings b
                                         WHERE b.interest_list_id = t.interest_list_id
                                           AND b.booking_status <> 'CANCELLED')), 0) AS passengers,
                           COALESCE(SUM(v.capacity), 0) AS seats,
                           AVG(EXTRACT(EPOCH FROM (t.created_at - i.created_at)) / 3600.0) AS planning_hours
                    FROM trips t
                    JOIN interest_lists i ON i.id = t.interest_list_id
                    LEFT JOIN vehicles v ON v.id = t.vehicle_id
                    WHERE t.created_at >= :fromInstant AND t.created_at < :toInstant
                ), assignment_metrics AS (
                    SELECT AVG(EXTRACT(EPOCH FROM (assigned_at - created_at)) / 3600.0) AS assignment_hours
                    FROM trips WHERE assigned_at >= :fromInstant AND assigned_at < :toInstant
                ), risk_metrics AS (
                    SELECT COUNT(*) AS at_risk
                    FROM trips t JOIN interest_lists i ON i.id = t.interest_list_id
                    WHERE i.reference_date >= :effectiveFromDate AND i.reference_date < :toDate
                      AND (t.conductor_id IS NULL OR t.vehicle_id IS NULL)
                ), recurrence_metrics AS (
                    SELECT COUNT(*) AS demands,
                           COUNT(*) FILTER (WHERE recurrence_plan_id IS NOT NULL) AS recurring
                    FROM interest_lists
                    WHERE created_at >= :fromInstant AND created_at < :toInstant
                      AND reference_date >= :today
                ), generated AS (
                    SELECT COUNT(*) AS occurrences FROM interest_lists
                    WHERE recurrence_plan_id IS NOT NULL AND created_at >= :fromInstant AND created_at < :toInstant
                ), conflicts AS (
                    SELECT COALESCE(SUM(COALESCE((metadata::jsonb ->> 'conflicts')::bigint, 0)), 0) AS avoided
                    FROM audit_events
                    WHERE action = 'RECURRENCE_PLAN_GENERATED'
                      AND occurred_at >= :fromInstant AND occurred_at < :toInstant
                )
                SELECT bm.confirmed, bm.created, bm.cancelled,
                       tm.passengers, tm.seats, tm.planning_hours, am.assignment_hours,
                       rm.at_risk, rec.demands, rec.recurring, g.occurrences, c.avoided
                FROM booking_metrics bm CROSS JOIN trip_metrics tm CROSS JOIN assignment_metrics am
                CROSS JOIN risk_metrics rm CROSS JOIN recurrence_metrics rec CROSS JOIN generated g CROSS JOIN conflicts c
                """;
        return jdbc.sql(sql)
                .param("fromInstant", databaseTimestamp(from)).param("toInstant", databaseTimestamp(to))
                .param("effectiveFromDate", fromDate.isAfter(today) ? fromDate : today)
                .param("toDate", toExclusive).param("today", today)
                .query((rs, row) -> new MetricSnapshot(
                        rs.getLong("confirmed"), rs.getLong("created"), rs.getLong("cancelled"),
                        rs.getLong("passengers"), rs.getLong("seats"), nullableDouble(rs, "planning_hours"),
                        nullableDouble(rs, "assignment_hours"), rs.getLong("at_risk"), rs.getLong("demands"),
                        rs.getLong("recurring"), rs.getLong("occurrences"), rs.getLong("avoided")))
                .single();
    }

    public List<AnalyticsTrendPointResponse> trend(Instant from, Instant to, LocalDate fromDate,
                                                    LocalDate toDate, String timezone) {
        String sql = """
                WITH days AS (SELECT generate_series(CAST(:fromDate AS date), CAST(:toDate AS date), interval '1 day')::date AS day),
                booking_created AS (
                    SELECT (created_at AT TIME ZONE :timezone)::date AS day, COUNT(*) AS total
                    FROM bookings WHERE created_at >= :fromInstant AND created_at < :toInstant GROUP BY 1
                ), booking_cancelled AS (
                    SELECT (cancelled_at AT TIME ZONE :timezone)::date AS day, COUNT(*) AS total
                    FROM bookings WHERE cancelled_at >= :fromInstant AND cancelled_at < :toInstant GROUP BY 1
                ), trip_created AS (
                    SELECT (created_at AT TIME ZONE :timezone)::date AS day, COUNT(*) AS total
                    FROM trips WHERE created_at >= :fromInstant AND created_at < :toInstant GROUP BY 1
                )
                SELECT d.day, COALESCE(bc.total, 0), COALESCE(bx.total, 0), COALESCE(tc.total, 0)
                FROM days d LEFT JOIN booking_created bc ON bc.day = d.day
                LEFT JOIN booking_cancelled bx ON bx.day = d.day LEFT JOIN trip_created tc ON tc.day = d.day
                ORDER BY d.day
                """;
        return jdbc.sql(sql).param("fromDate", fromDate).param("toDate", toDate)
                .param("fromInstant", databaseTimestamp(from)).param("toInstant", databaseTimestamp(to)).param("timezone", timezone)
                .query((rs, row) -> new AnalyticsTrendPointResponse(rs.getObject(1, LocalDate.class),
                        rs.getLong(2), rs.getLong(3), rs.getLong(4))).list();
    }

    public List<UniversityAnalyticsResponse> universities(Instant from, Instant to) {
        String sql = """
                WITH booking_totals AS (
                    SELECT destination_id, COUNT(*) AS bookings FROM bookings
                    WHERE created_at >= :fromInstant AND created_at < :toInstant GROUP BY destination_id
                ), trip_totals AS (
                    SELECT i.destination_id, COUNT(*) AS trips FROM trips t
                    JOIN interest_lists i ON i.id = t.interest_list_id
                    WHERE t.created_at >= :fromInstant AND t.created_at < :toInstant GROUP BY i.destination_id
                )
                SELECT u.id, u.name, COALESCE(b.bookings, 0) AS bookings, COALESCE(t.trips, 0) AS trips
                FROM universities u LEFT JOIN booking_totals b ON b.destination_id = u.id
                LEFT JOIN trip_totals t ON t.destination_id = u.id
                WHERE b.bookings IS NOT NULL OR t.trips IS NOT NULL
                ORDER BY bookings DESC, trips DESC, u.name
                """;
        return jdbc.sql(sql).param("fromInstant", databaseTimestamp(from)).param("toInstant", databaseTimestamp(to))
                .query((rs, row) -> new UniversityAnalyticsResponse(rs.getObject("id", UUID.class),
                        rs.getString("name"), rs.getLong("bookings"), rs.getLong("trips"))).list();
    }

    public OperationalExceptionsResponse exceptions(LocalDate fromDate, LocalDate toExclusive, LocalDate today) {
        String sql = """
                SELECT COUNT(*) FILTER (WHERE t.conductor_id IS NULL) AS missing_driver,
                       COUNT(*) FILTER (WHERE t.vehicle_id IS NULL) AS missing_vehicle,
                       COUNT(*) FILTER (WHERE t.vehicle_id IS NOT NULL AND
                           (SELECT COUNT(*) FROM bookings b WHERE b.interest_list_id = t.interest_list_id
                            AND b.booking_status <> 'CANCELLED') > v.capacity) AS insufficient_capacity
                FROM trips t JOIN interest_lists i ON i.id = t.interest_list_id
                LEFT JOIN vehicles v ON v.id = t.vehicle_id
                WHERE i.reference_date >= :effectiveFromDate AND i.reference_date < :toDate
                """;
        return jdbc.sql(sql).param("effectiveFromDate", fromDate.isAfter(today) ? fromDate : today)
                .param("toDate", toExclusive).query((rs, row) -> new OperationalExceptionsResponse(
                        rs.getLong("missing_driver"), rs.getLong("missing_vehicle"),
                        rs.getLong("insufficient_capacity"))).single();
    }

    public long activeRecurrencePlans() {
        return jdbc.sql("SELECT COUNT(*) FROM recurrence_plans WHERE status = 'ACTIVE'")
                .query(Long.class).single();
    }

    public Instant metricsCompleteSince() {
        return jdbc.sql("""
                        SELECT metadata_value::timestamptz
                        FROM system_metadata WHERE metadata_key = 'operational_metrics_complete_since'
                        """)
                .query(OffsetDateTime.class).optional().map(OffsetDateTime::toInstant).orElse(null);
    }

    private static Double nullableDouble(java.sql.ResultSet resultSet, String column) throws java.sql.SQLException {
        double value = resultSet.getDouble(column);
        return resultSet.wasNull() ? null : value;
    }

    private static OffsetDateTime databaseTimestamp(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }

    public record MetricSnapshot(long confirmedBookings, long createdBookings, long cancelledBookings,
                                 long passengers, long seats, Double planningHours, Double assignmentHours,
                                 long tripsAtRisk, long demands, long recurringDemands,
                                 long generatedOccurrences, long conflictsAvoided) {
    }
}
