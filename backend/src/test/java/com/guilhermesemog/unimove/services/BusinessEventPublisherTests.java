package com.guilhermesemog.unimove.services;

import java.util.UUID;
import com.guilhermesemog.unimove.model.OutboxEvent;
import com.guilhermesemog.unimove.model.enums.BusinessEventType;
import com.guilhermesemog.unimove.model.enums.OutboxStatus;
import com.guilhermesemog.unimove.repository.OutboxEventRepository;
import com.guilhermesemog.unimove.repository.UserRepository;
import com.guilhermesemog.unimove.service.BusinessEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("BusinessEventPublisher Tests")
class BusinessEventPublisherTests {

    @Mock private OutboxEventRepository outboxEventRepository;
    @Mock private UserRepository userRepository;

    @Test
    @DisplayName("should persist a pending event with a serialized safe payload")
    void shouldPersistPendingEvent() {
        BusinessEventPublisher publisher = new BusinessEventPublisher(
                outboxEventRepository, new ObjectMapper(), userRepository);

        publisher.publish(
                BusinessEventType.BOOKING_CREATED,
                "Booking",
                UUID.fromString("00000000-0000-4000-8000-000000000042"),
                Map.of("bookingId", UUID.fromString("00000000-0000-4000-8000-000000000042"), "interestListId", UUID.fromString("00000000-0000-4000-8000-000000000007")),
                "BOOKING_CREATED:42:0"
        );

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());

        OutboxEvent event = captor.getValue();
        assertThat(event.getId()).isNotNull();
        assertThat(event.getEventType()).isEqualTo(BusinessEventType.BOOKING_CREATED);
        assertThat(event.getAggregateType()).isEqualTo("Booking");
        assertThat(event.getAggregateId()).isEqualTo(UUID.fromString("00000000-0000-4000-8000-000000000042"));
        assertThat(event.getActorId()).isNull();
        assertThat(event.getActorRole()).isEqualTo("SYSTEM");
        assertThat(event.getCorrelationId()).isNotNull();
        assertThat(event.getPayload()).contains("\"bookingId\":\"00000000-0000-4000-8000-000000000042\"");
        assertThat(event.getDeduplicationKey()).isEqualTo("BOOKING_CREATED:42:0");
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(event.getAttempts()).isZero();
        assertThat(event.getOccurredAt()).isNotNull();
    }
}
