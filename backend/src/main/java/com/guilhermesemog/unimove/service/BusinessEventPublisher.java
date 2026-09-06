package com.guilhermesemog.unimove.service;

import java.util.UUID;
import com.guilhermesemog.unimove.audit.CorrelationIdContext;
import com.guilhermesemog.unimove.model.OutboxEvent;
import com.guilhermesemog.unimove.model.enums.BusinessEventType;
import com.guilhermesemog.unimove.repository.OutboxEventRepository;
import com.guilhermesemog.unimove.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class BusinessEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    public BusinessEventPublisher(
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper,
            UserRepository userRepository
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(
            BusinessEventType eventType,
            String aggregateType,
            UUID aggregateId,
            Object payload,
            String deduplicationKey
    ) {
        try {
            Actor actor = resolveActor();
            outboxEventRepository.save(new OutboxEvent(
                    eventType,
                    aggregateType,
                    aggregateId,
                    actor.id(),
                    actor.role(),
                    CorrelationIdContext.currentOrCreate(),
                    objectMapper.writeValueAsString(payload),
                    deduplicationKey
            ));
        } catch (JacksonException exception) {
            throw new IllegalStateException("Could not serialize business event", exception);
        }
    }

    private Actor resolveActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return new Actor(null, "SYSTEM");
        }

        return userRepository.findByCpf(authentication.getName())
                .map(user -> new Actor(user.getId(), user.getRole().name()))
                .orElseGet(() -> new Actor(
                        null,
                        authentication.getAuthorities().stream()
                                .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
                                .findFirst()
                                .orElse("SYSTEM")
                ));
    }

    private record Actor(UUID id, String role) {
    }
}
