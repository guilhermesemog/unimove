package com.guilhermesemog.unimove.repository;

import com.guilhermesemog.unimove.dto.telemetry.TelemetryBreakdownResponse;
import com.guilhermesemog.unimove.dto.telemetry.TelemetryEventRequest;
import com.guilhermesemog.unimove.model.enums.Role;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Repository
public class TelemetryRepository {
    private final JdbcClient jdbc;

    public TelemetryRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public int insertIfAbsent(TelemetryEventRequest event, Role role, String properties, Instant receivedAt) {
        return jdbc.sql("""
                        INSERT INTO product_telemetry_events
                          (id, event_name, actor_role, session_id, occurred_at, received_at, properties)
                        VALUES (:id, :eventName, :role, :sessionId, :occurredAt, :receivedAt, CAST(:properties AS jsonb))
                        ON CONFLICT (id) DO NOTHING
                        """)
                .param("id", event.id()).param("eventName", event.name().name()).param("role", role.name())
                .param("sessionId", event.sessionId()).param("occurredAt", timestamp(event.occurredAt()))
                .param("receivedAt", timestamp(receivedAt)).param("properties", properties)
                .update();
    }

    public TelemetryTotals totals(Instant from, Instant to) {
        return jdbc.sql("""
                        SELECT COUNT(DISTINCT session_id) FILTER (WHERE event_name = 'booking_started') AS booking_started,
                               COUNT(DISTINCT session_id) FILTER (WHERE event_name = 'booking_completed') AS booking_completed,
                               percentile_cont(0.5) WITHIN GROUP (ORDER BY (properties ->> 'durationMs')::double precision)
                                 FILTER (WHERE event_name = 'booking_completed' AND jsonb_exists(properties, 'durationMs')) AS booking_median,
                               COUNT(*) FILTER (WHERE event_name = 'recurrence_previewed') AS recurrence_previewed,
                               COUNT(*) FILTER (WHERE event_name = 'recurrence_published') AS recurrence_published,
                               COUNT(*) FILTER (WHERE event_name = 'trip_generated') AS trips_generated,
                               COUNT(*) FILTER (WHERE event_name = 'assignment_completed') AS assignments_completed,
                               COUNT(*) FILTER (WHERE event_name = 'operation_opened') AS operations_opened,
                               COUNT(*) FILTER (WHERE event_name = 'manifest_viewed') AS manifests_viewed,
                               COUNT(*) FILTER (WHERE event_name = 'operation_load_failed') AS operation_failures,
                               percentile_cont(0.5) WITHIN GROUP (ORDER BY (properties ->> 'durationMs')::double precision)
                                 FILTER (WHERE event_name = 'operation_opened' AND jsonb_exists(properties, 'durationMs')) AS operation_median
                        FROM product_telemetry_events
                        WHERE occurred_at >= :fromInstant AND occurred_at < :toInstant
                        """)
                .param("fromInstant", timestamp(from)).param("toInstant", timestamp(to))
                .query((rs, row) -> new TelemetryTotals(
                        rs.getLong("booking_started"), rs.getLong("booking_completed"), nullableDouble(rs, "booking_median"),
                        rs.getLong("recurrence_previewed"), rs.getLong("recurrence_published"), rs.getLong("trips_generated"),
                        rs.getLong("assignments_completed"), rs.getLong("operations_opened"), rs.getLong("manifests_viewed"),
                        rs.getLong("operation_failures"), nullableDouble(rs, "operation_median")))
                .single();
    }

    public List<TelemetryBreakdownResponse> breakdown(Instant from, Instant to, String eventName, String property) {
        if (!property.equals("step") && !property.equals("screen")) throw new IllegalArgumentException("Unsupported breakdown");
        String sql = """
                SELECT COALESCE(properties ->> '%s', 'unknown') AS key, COUNT(*) AS total
                FROM product_telemetry_events
                WHERE occurred_at >= :fromInstant AND occurred_at < :toInstant AND event_name = :eventName
                GROUP BY 1 ORDER BY total DESC, key
                """.formatted(property);
        return jdbc.sql(sql).param("fromInstant", timestamp(from)).param("toInstant", timestamp(to))
                .param("eventName", eventName)
                .query((rs, row) -> new TelemetryBreakdownResponse(rs.getString("key"), rs.getLong("total"))).list();
    }

    public int deleteReceivedBefore(Instant cutoff) {
        return jdbc.sql("DELETE FROM product_telemetry_events WHERE received_at < :cutoff")
                .param("cutoff", timestamp(cutoff)).update();
    }

    public long count() {
        return jdbc.sql("SELECT COUNT(*) FROM product_telemetry_events").query(Long.class).single();
    }

    private static OffsetDateTime timestamp(Instant instant) { return instant.atOffset(ZoneOffset.UTC); }

    private static Double nullableDouble(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    public record TelemetryTotals(long bookingStarted, long bookingCompleted, Double medianBookingDurationMs,
                                  long recurrencePreviewed, long recurrencePublished, long tripsGenerated,
                                  long assignmentsCompleted, long operationsOpened, long manifestsViewed,
                                  long operationLoadFailures, Double medianOperationOpenDurationMs) {
    }
}
