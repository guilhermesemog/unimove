package com.guilhermesemog.unimove.service;

import java.util.UUID;
import com.guilhermesemog.unimove.audit.CorrelationIdContext;
import com.guilhermesemog.unimove.model.AuditEvent;
import com.guilhermesemog.unimove.model.enums.AuditAction;
import com.guilhermesemog.unimove.repository.AuditEventRepository;
import com.guilhermesemog.unimove.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class AuditService {

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "cpf", "password", "phone", "address", "token", "accesstoken", "refreshtoken", "manifest"
    );

    private final AuditEventRepository auditEventRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AuditService(
            AuditEventRepository auditEventRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper
    ) {
        this.auditEventRepository = auditEventRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(
            AuditAction action,
            String entityType,
            UUID entityId,
            Map<String, ?> previousState,
            Map<String, ?> resultingState,
            Map<String, ?> metadata
    ) {
        Actor actor = resolveActor();
        AuditEvent auditEvent = new AuditEvent(
                actor.id(),
                actor.role(),
                action,
                entityType,
                entityId,
                CorrelationIdContext.currentOrCreate(),
                serialize(previousState),
                serialize(resultingState),
                serialize(metadata)
        );
        auditEventRepository.save(auditEvent);
    }

    private Actor resolveActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return new Actor(null, "SYSTEM");
        }

        return userRepository.findByCpf(authentication.getName())
                .map(user -> new Actor(user.getId(), user.getRole().name()))
                .orElseGet(() -> new Actor(null, authorityRole(authentication)));
    }

    private String authorityRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
                .findFirst()
                .orElse("SYSTEM");
    }

    private String serialize(Map<String, ?> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(sanitizeMap(values));
        } catch (JacksonException exception) {
            throw new IllegalStateException("Could not serialize audit data", exception);
        }
    }

    private Map<String, Object> sanitizeMap(Map<String, ?> values) {
        Map<String, Object> sanitized = new LinkedHashMap<>();
        values.forEach((key, value) -> {
            if (!isSensitiveKey(key)) {
                sanitized.put(key, sanitizeValue(value));
            }
        });
        return sanitized;
    }

    private Object sanitizeValue(Object value) {
        if (value instanceof Map<?, ?> nestedMap) {
            Map<String, Object> typedMap = new LinkedHashMap<>();
            nestedMap.forEach((key, nestedValue) -> typedMap.put(String.valueOf(key), nestedValue));
            return sanitizeMap(typedMap);
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(this::sanitizeValue).toList();
        }
        return value;
    }

    private String normalizeKey(String key) {
        return key.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private boolean isSensitiveKey(String key) {
        String normalizedKey = normalizeKey(key);
        return SENSITIVE_KEYS.stream().anyMatch(normalizedKey::endsWith);
    }

    private record Actor(UUID id, String role) {
    }
}
