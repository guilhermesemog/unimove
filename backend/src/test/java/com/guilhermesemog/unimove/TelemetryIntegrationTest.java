package com.guilhermesemog.unimove;

import com.guilhermesemog.unimove.dto.telemetry.*;
import com.guilhermesemog.unimove.model.enums.Role;
import com.guilhermesemog.unimove.model.enums.TelemetryEventName;
import com.guilhermesemog.unimove.repository.TelemetryRepository;
import com.guilhermesemog.unimove.service.TelemetryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "unimove.phase-five.telemetry.enabled=true")
@AutoConfigureMockMvc
@Transactional
class TelemetryIntegrationTest {
    @Autowired private TelemetryService service;
    @Autowired private TelemetryRepository repository;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @BeforeEach
    void clearEvents() {
        jdbc.update("DELETE FROM product_telemetry_events");
    }

    @Test
    void shouldPersistDeduplicateAndSummarizeProductLearningEvents() {
        Instant now = Instant.now();
        UUID firstSession = UUID.randomUUID();
        UUID secondSession = UUID.randomUUID();
        TelemetryEventRequest started = event(TelemetryEventName.booking_started, firstSession, now, Map.of("screen", "booking_checkout"));
        TelemetryEventRequest completed = event(TelemetryEventName.booking_completed, firstSession, now,
                Map.of("screen", "booking_checkout", "result", "success", "durationMs", 1200));
        service.ingest("student-a", Role.STUDENT, new TelemetryBatchRequest(List.of(started, completed,
                event(TelemetryEventName.booking_started, secondSession, now, Map.of("screen", "booking_checkout")),
                event(TelemetryEventName.booking_abandoned, secondSession, now,
                        Map.of("screen", "booking_checkout", "step", "review", "result", "abandoned")))));
        service.ingest("manager", Role.ADMIN, new TelemetryBatchRequest(List.of(
                event(TelemetryEventName.recurrence_previewed, UUID.randomUUID(), now, Map.of("screen", "recurrence_editor")),
                event(TelemetryEventName.recurrence_published, UUID.randomUUID(), now,
                        Map.of("screen", "recurrence_editor", "result", "success")))));
        service.ingest("driver", Role.CONDUCTOR, new TelemetryBatchRequest(List.of(
                event(TelemetryEventName.operation_opened, UUID.randomUUID(), now,
                        Map.of("screen", "operation_detail", "durationMs", 500)),
                event(TelemetryEventName.manifest_viewed, UUID.randomUUID(), now,
                        Map.of("screen", "operation_detail", "step", "manifest")))));

        assertThat(service.ingest("student-a", Role.STUDENT, new TelemetryBatchRequest(List.of(started)))).isZero();
        TelemetrySummaryResponse summary = service.summary(7);

        assertThat(repository.count()).isEqualTo(8);
        assertThat(summary.bookingStarted()).isEqualTo(2);
        assertThat(summary.bookingCompleted()).isEqualTo(1);
        assertThat(summary.bookingCompletionRate()).isEqualTo(50.0);
        assertThat(summary.medianBookingDurationMs()).isEqualTo(1200.0);
        assertThat(summary.bookingAbandonments()).containsExactly(new TelemetryBreakdownResponse("review", 1));
        assertThat(summary.recurrencePreviewed()).isEqualTo(1);
        assertThat(summary.recurrencePublished()).isEqualTo(1);
        assertThat(summary.operationsOpened()).isEqualTo(1);
        assertThat(summary.manifestsViewed()).isEqualTo(1);
        assertThat(summary.medianOperationOpenDurationMs()).isEqualTo(500.0);
    }

    @Test
    void shouldRejectPersonalOrRoleIncompatibleDataAndRollbackTheBatch() {
        Instant now = Instant.now();
        TelemetryEventRequest valid = event(TelemetryEventName.booking_started, UUID.randomUUID(), now,
                Map.of("screen", "booking_checkout"));
        TelemetryEventRequest personal = event(TelemetryEventName.booking_completed, UUID.randomUUID(), now,
                Map.of("screen", "booking_checkout", "studentId", UUID.randomUUID().toString()));

        assertThatThrownBy(() -> service.ingest("student", Role.STUDENT,
                new TelemetryBatchRequest(List.of(valid, personal))))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("unsupported property");
        assertThat(repository.count()).isZero();
        assertThatThrownBy(() -> service.ingest("student", Role.STUDENT, new TelemetryBatchRequest(List.of(
                event(TelemetryEventName.recurrence_started, UUID.randomUUID(), now,
                        Map.of("screen", "recurrence_editor"))))))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("authenticated role");
    }

    @Test
    void shouldAuthorizeCollectionAndRestrictTheAggregateToAdministrators() throws Exception {
        TelemetryBatchRequest request = new TelemetryBatchRequest(List.of(event(
                TelemetryEventName.availability_viewed, UUID.randomUUID(), Instant.now(), Map.of("screen", "availability"))));

        mockMvc.perform(post("/telemetry/events").with(user("student").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted()).andExpect(jsonPath("$.accepted").value(1));
        mockMvc.perform(get("/admin/telemetry/summary").with(user("student").roles("STUDENT")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/telemetry/summary").param("days", "7").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.timezone").value("America/Sao_Paulo"));
        mockMvc.perform(get("/admin/telemetry/summary").param("days", "8").with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteOnlyEventsPastTheConfiguredRetention() {
        Instant now = Instant.now();
        insertRawEvent(now.minusSeconds(181L * 86_400), now.minusSeconds(181L * 86_400));
        insertRawEvent(now.minusSeconds(10), now.minusSeconds(10));

        assertThat(service.deleteExpired(180)).isEqualTo(1);
        assertThat(repository.count()).isEqualTo(1);
    }

    private TelemetryEventRequest event(TelemetryEventName name, UUID session, Instant occurredAt,
                                        Map<String, Object> properties) {
        return new TelemetryEventRequest(UUID.randomUUID(), name, session, occurredAt, properties);
    }

    private void insertRawEvent(Instant occurredAt, Instant receivedAt) {
        jdbc.update("""
                INSERT INTO product_telemetry_events
                  (id,event_name,actor_role,session_id,occurred_at,received_at,properties)
                VALUES (?,'availability_viewed','STUDENT',?,?,?,CAST(? AS jsonb))
                """, UUID.randomUUID(), UUID.randomUUID(), Timestamp.from(occurredAt), Timestamp.from(receivedAt), "{}");
    }
}
