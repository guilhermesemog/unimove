package com.guilhermesemog.unimove;

import com.guilhermesemog.unimove.exception.type.ResourceNotFoundException;
import com.guilhermesemog.unimove.model.Notification;
import com.guilhermesemog.unimove.model.OutboxEvent;
import com.guilhermesemog.unimove.model.InterestList;
import com.guilhermesemog.unimove.model.University;
import com.guilhermesemog.unimove.model.User;
import com.guilhermesemog.unimove.model.enums.BusinessEventType;
import com.guilhermesemog.unimove.model.enums.OutboxStatus;
import com.guilhermesemog.unimove.model.enums.Role;
import com.guilhermesemog.unimove.repository.NotificationRepository;
import com.guilhermesemog.unimove.repository.InterestListRepository;
import com.guilhermesemog.unimove.repository.OutboxEventRepository;
import com.guilhermesemog.unimove.repository.UniversityRepository;
import com.guilhermesemog.unimove.repository.UserRepository;
import com.guilhermesemog.unimove.service.NotificationService;
import com.guilhermesemog.unimove.service.NotificationReminderService;
import com.guilhermesemog.unimove.service.OutboxClaimService;
import com.guilhermesemog.unimove.service.OutboxEventProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;
import java.time.Clock;
import java.time.LocalTime;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class NotificationPersistenceIntegrationTest {
    @Autowired private UserRepository userRepository;
    @Autowired private OutboxEventRepository outboxEventRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private OutboxClaimService claimService;
    @Autowired private OutboxEventProcessor processor;
    @Autowired private NotificationService notificationService;
    @Autowired private NotificationReminderService reminderService;
    @Autowired private UniversityRepository universityRepository;
    @Autowired private InterestListRepository interestListRepository;
    @Autowired private Clock businessClock;
    @Autowired private TransactionTemplate transactionTemplate;

    @Test
    void shouldProcessOnceKeepNotificationsPrivateAndPersistReadState() {
        User recipient = saveUser(Role.STUDENT);
        User anotherUser = saveUser(Role.STUDENT);
        OutboxEvent event = saveEvent(recipient.getId(), validPayload(recipient.getId()));

        List<UUID> claimed = claimService.claim();
        assertThat(claimed).contains(event.getId());
        processor.process(event.getId());
        processor.process(event.getId());

        List<Notification> notifications = notificationRepository
                .findAllByRecipient_Id(recipient.getId(), org.springframework.data.domain.Pageable.unpaged())
                .getContent();
        assertThat(notifications).hasSize(1);
        assertThat(outboxEventRepository.findById(event.getId()).orElseThrow().getStatus())
                .isEqualTo(OutboxStatus.PUBLISHED);

        Authentication recipientAuth = authentication(recipient.getCpf());
        Authentication anotherAuth = authentication(anotherUser.getCpf());
        assertThat(notificationService.unreadCount(recipientAuth)).isEqualTo(1);
        assertThatThrownBy(() -> notificationService.markRead(anotherAuth, notifications.getFirst().getId()))
                .isInstanceOf(ResourceNotFoundException.class);

        notificationService.markRead(recipientAuth, notifications.getFirst().getId());
        assertThat(notificationService.unreadCount(recipientAuth)).isZero();
        assertThat(notificationRepository.findById(notifications.getFirst().getId()).orElseThrow().getReadAt())
                .isNotNull();
    }

    @Test
    void shouldRecordRetryWithoutRollingBackPreviouslyCommittedBusinessData() {
        User user = saveUser(Role.STUDENT);
        OutboxEvent event = saveEvent(user.getId(), "{\"bookingId\":\"missing-student\"}");

        assertThat(claimService.claim()).contains(event.getId());
        processor.process(event.getId());

        OutboxEvent failed = outboxEventRepository.findById(event.getId()).orElseThrow();
        assertThat(failed.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(failed.getAttempts()).isEqualTo(1);
        assertThat(failed.getNextAttemptAt()).isNotNull();
        assertThat(failed.getLastError()).contains("Missing outbox field");
        assertThat(userRepository.findById(user.getId())).isPresent();
    }

    @Test
    void shouldDeduplicateScheduledAdminIssue() {
        User admin = saveUser(Role.ADMIN);
        ZonedDateTime closing = ZonedDateTime.now(businessClock).plusMinutes(30);
        transactionTemplate.executeWithoutResult(status -> {
            University destination = universityRepository.save(new University(
                    "Notification Campus " + UUID.randomUUID(), "Test address"));
            interestListRepository.save(new InterestList(closing.toLocalDate(), closing.toLocalTime(),
                    LocalTime.of(17, 30), LocalTime.of(19, 0), LocalTime.of(23, 0),
                    LocalTime.of(0, 30), destination));
        });

        assertThat(reminderService.createDueNotifications()).isEqualTo(1);
        assertThat(reminderService.createDueNotifications()).isZero();
        assertThat(notificationRepository.countByRecipient_IdAndReadAtIsNull(admin.getId())).isEqualTo(1);
    }

    private User saveUser(Role role) {
        return transactionTemplate.execute(status -> userRepository.save(new User(
                UUID.randomUUID().toString().replace("-", "").substring(0, 11), "password",
                "Notification", "Test", UUID.randomUUID().toString(), true, role)));
    }

    private OutboxEvent saveEvent(UUID aggregateId, String payload) {
        return transactionTemplate.execute(status -> outboxEventRepository.save(new OutboxEvent(
                BusinessEventType.BOOKING_CREATED, "Booking", aggregateId, null, "SYSTEM",
                UUID.randomUUID(), payload, "notification-test:" + UUID.randomUUID())));
    }

    private String validPayload(UUID studentId) {
        return "{\"bookingId\":\"" + UUID.randomUUID() + "\",\"studentId\":\"" + studentId + "\"}";
    }

    private Authentication authentication(String name) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(name);
        return authentication;
    }
}
