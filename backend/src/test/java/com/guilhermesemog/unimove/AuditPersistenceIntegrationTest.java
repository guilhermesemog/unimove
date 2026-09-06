package com.guilhermesemog.unimove;

import java.util.UUID;
import com.guilhermesemog.unimove.model.enums.AuditAction;
import com.guilhermesemog.unimove.model.University;
import com.guilhermesemog.unimove.repository.AuditEventRepository;
import com.guilhermesemog.unimove.repository.UniversityRepository;
import com.guilhermesemog.unimove.service.AuditService;
import com.guilhermesemog.unimove.service.AuditQueryService;
import com.guilhermesemog.unimove.dto.audit.AuditEventDetailResponse;
import com.guilhermesemog.unimove.dto.audit.AuditEventResponse;
import com.guilhermesemog.unimove.dto.audit.AuditQuery;
import com.guilhermesemog.unimove.model.User;
import com.guilhermesemog.unimove.model.Vehicle;
import com.guilhermesemog.unimove.model.enums.Role;
import com.guilhermesemog.unimove.repository.UserRepository;
import com.guilhermesemog.unimove.repository.VehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import jakarta.persistence.EntityManager;

import java.util.Map;
import java.util.Set;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DisplayName("Audit persistence integration")
class AuditPersistenceIntegrationTest {

    @Autowired private AuditService auditService;
    @Autowired private AuditEventRepository auditEventRepository;
    @Autowired private UniversityRepository universityRepository;
    @Autowired private TransactionTemplate transactionTemplate;
    @Autowired private EntityManager entityManager;
    @Autowired private AuditQueryService auditQueryService;
    @Autowired private UserRepository userRepository;
    @Autowired private VehicleRepository vehicleRepository;

    @Test
    @Transactional
    @DisplayName("should persist an immutable audit event in the current transaction")
    void shouldPersistAuditEvent() {
        long previousCount = auditEventRepository.count();

        auditService.record(
                AuditAction.DEMAND_PUBLISHED,
                "InterestList",
                UUID.fromString("00000000-0000-4000-8000-000000000099"),
                Map.of(),
                Map.of("status", "OPEN"),
                Map.of("source", "INTEGRATION_TEST")
        );

        assertThat(auditEventRepository.count()).isEqualTo(previousCount + 1);
    }

    @Test
    @DisplayName("should roll back the domain change when audit persistence fails")
    void shouldRollbackDomainChangeWhenAuditFails() {
        String universityName = "Rollback Audit University";

        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status -> {
            University university = universityRepository.save(
                    new University(universityName, "Rollback Audit Campus"));
            auditService.record(
                    AuditAction.DEMAND_PUBLISHED,
                    "x".repeat(81),
                    university.getId(),
                    Map.of(),
                    Map.of("status", "OPEN"),
                    Map.of()
            );
            entityManager.flush();
        })).isInstanceOf(RuntimeException.class);

        assertThat(universityRepository.findByName(universityName)).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("should filter a bounded page and render resolved relevant changes")
    void shouldQueryAndDescribeAuditEvents() {
        User admin = userRepository.save(new User(randomCpf(), "password", "Trace", "Admin",
                UUID.randomUUID().toString(), true, Role.ADMIN));
        User driver = userRepository.save(new User(randomCpf(), "password", "Alex", "Driver",
                UUID.randomUUID().toString(), true, Role.CONDUCTOR));
        Vehicle vehicle = vehicleRepository.save(new Vehicle("AUD-" + UUID.randomUUID().toString().substring(0, 4), 42));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                admin.getCpf(), null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        UUID tripId = UUID.randomUUID();
        Instant before = Instant.now().minusSeconds(5);

        auditService.record(AuditAction.TRIP_ASSIGNMENT_CHANGED, "Trip", tripId,
                mapWithNulls("conductorId", null, "vehicleId", null),
                Map.of("conductorId", driver.getId(), "vehicleId", vehicle.getId()), Map.of("source", "test"));
        auditService.record(AuditAction.TRIP_ASSIGNMENT_CHANGED, "Trip", tripId,
                Map.of("conductorId", driver.getId(), "vehicleId", vehicle.getId()),
                mapWithNulls("conductorId", null, "vehicleId", vehicle.getId()), Map.of("source", "test"));

        Set<UUID> actorIds = auditQueryService.findActorIds("Trace Admin");
        Page<AuditEventResponse> result = auditQueryService.search(new AuditQuery(before,
                Instant.now().plusSeconds(5), actorIds, AuditAction.TRIP_ASSIGNMENT_CHANGED,
                "trip", tripId, Set.of(), null), 0, 10, "asc");

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().getFirst().actorName()).isEqualTo("Trace Admin");
        assertThat(result.getContent().getFirst().description()).isEqualTo("Driver changed from None to Alex Driver");
        assertThat(result.getContent().getFirst().occurredAt())
                .isBeforeOrEqualTo(result.getContent().getLast().occurredAt());
        AuditEventDetailResponse detail = auditQueryService.getById(result.getContent().getFirst().id());
        assertThat(detail.resultingState()).containsEntry("vehicleId", vehicle.getId().toString());
        assertThat(detail.metadata()).containsEntry("source", "test");
        SecurityContextHolder.clearContext();
    }

    private Map<String, Object> mapWithNulls(Object... entries) {
        Map<String, Object> values = new java.util.LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) values.put((String) entries[index], entries[index + 1]);
        return values;
    }

    private String randomCpf() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 11);
    }
}
