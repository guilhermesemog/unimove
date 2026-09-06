package com.guilhermesemog.unimove.services;

import com.guilhermesemog.unimove.audit.CorrelationIdContext;
import com.guilhermesemog.unimove.model.AuditEvent;
import com.guilhermesemog.unimove.model.User;
import com.guilhermesemog.unimove.model.enums.AuditAction;
import com.guilhermesemog.unimove.model.enums.Role;
import com.guilhermesemog.unimove.repository.AuditEventRepository;
import com.guilhermesemog.unimove.repository.UserRepository;
import com.guilhermesemog.unimove.service.AuditService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditService Tests")
class AuditServiceTests {

    @Mock private AuditEventRepository auditEventRepository;
    @Mock private UserRepository userRepository;

    private AuditService auditService;

    @BeforeEach
    void setUp() {
        auditService = new AuditService(auditEventRepository, userRepository, new ObjectMapper());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        CorrelationIdContext.clear();
    }

    @Test
    @DisplayName("should persist actor, correlation and only permitted audit fields")
    void shouldPersistSanitizedAuditEvent() {
        UUID correlationId = UUID.randomUUID();
        CorrelationIdContext.set(correlationId);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "12345678900",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        ));

        User actor = new User();
        actor.setId(UUID.fromString("00000000-0000-4000-8000-000000000007"));
        actor.setRole(Role.ADMIN);
        given(userRepository.findByCpf("12345678900")).willReturn(Optional.of(actor));

        auditService.record(
                AuditAction.BOOKING_CANCELLED,
                "Booking",
                UUID.fromString("00000000-0000-4000-8000-000000000021"),
                Map.of("status", "APPROVED", "cpf", "12345678900"),
                Map.of("status", "CANCELLED", "profile", Map.of("phone", "555-0100", "studentId", UUID.fromString("00000000-0000-4000-8000-000000000012"))),
                Map.of("access_token", "secret", "source", "API")
        );

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventRepository).save(captor.capture());
        AuditEvent auditEvent = captor.getValue();

        assertThat(auditEvent.getActorId()).isEqualTo(UUID.fromString("00000000-0000-4000-8000-000000000007"));
        assertThat(auditEvent.getActorRole()).isEqualTo("ADMIN");
        assertThat(auditEvent.getCorrelationId()).isEqualTo(correlationId);
        assertThat(auditEvent.getAction()).isEqualTo(AuditAction.BOOKING_CANCELLED);
        assertThat(auditEvent.getPreviousState()).contains("APPROVED").doesNotContain("cpf", "12345678900");
        assertThat(auditEvent.getResultingState()).contains("studentId", "12").doesNotContain("phone", "555-0100");
        assertThat(auditEvent.getMetadata()).contains("source", "API").doesNotContain("access_token", "secret");
    }

    @Test
    @DisplayName("should propagate persistence failures to the business transaction")
    void shouldPropagatePersistenceFailure() {
        given(auditEventRepository.save(any(AuditEvent.class)))
                .willThrow(new IllegalStateException("Audit persistence unavailable"));

        assertThatThrownBy(() -> auditService.record(
                AuditAction.TRIP_CREATED,
                "Trip",
                UUID.fromString("00000000-0000-4000-8000-000000000010"),
                Map.of(),
                Map.of("status", "PROCESSING"),
                Map.of()
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Audit persistence unavailable");
    }
}
