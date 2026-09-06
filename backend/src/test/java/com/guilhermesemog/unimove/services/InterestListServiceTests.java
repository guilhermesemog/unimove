package com.guilhermesemog.unimove.services;

import java.util.UUID;
import com.guilhermesemog.unimove.dto.interestlist.InterestListToggleStatus;
import com.guilhermesemog.unimove.mapper.InterestListMapper;
import com.guilhermesemog.unimove.model.InterestList;
import com.guilhermesemog.unimove.model.University;
import com.guilhermesemog.unimove.model.enums.AuditAction;
import com.guilhermesemog.unimove.model.enums.BusinessEventType;
import com.guilhermesemog.unimove.model.enums.ListStatus;
import com.guilhermesemog.unimove.repository.BookingRepository;
import com.guilhermesemog.unimove.repository.InterestListRepository;
import com.guilhermesemog.unimove.repository.UniversityRepository;
import com.guilhermesemog.unimove.service.AuditService;
import com.guilhermesemog.unimove.service.BusinessEventPublisher;
import com.guilhermesemog.unimove.service.InterestListService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("InterestListService Tests")
class InterestListServiceTests {

    @Mock private InterestListMapper interestListMapper;
    @Mock private InterestListRepository interestListRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private UniversityRepository universityRepository;
    @Mock private BusinessEventPublisher eventPublisher;
    @Mock private AuditService auditService;

    private InterestListService interestListService;

    @BeforeEach
    void setUp() {
        interestListService = new InterestListService(
                interestListMapper,
                interestListRepository,
                bookingRepository,
                universityRepository,
                eventPublisher,
                auditService,
                Clock.fixed(Instant.parse("2026-09-01T12:00:00Z"), ZoneId.of("America/Sao_Paulo"))
        );
    }

    @Test
    @DisplayName("should audit a demand status change exactly once")
    void shouldAuditDemandStatusChange() {
        University destination = new University();
        destination.setId(UUID.fromString("00000000-0000-4000-8000-000000000009"));
        InterestList interestList = new InterestList(
                LocalDate.of(2026, 9, 1),
                LocalTime.of(12, 0),
                LocalTime.of(17, 30),
                LocalTime.of(19, 0),
                LocalTime.of(23, 0),
                LocalTime.of(0, 30),
                destination
        );
        interestList.setId(UUID.fromString("00000000-0000-4000-8000-000000000015"));
        interestList.setListStatus(ListStatus.OPEN);

        given(interestListRepository.findById(UUID.fromString("00000000-0000-4000-8000-000000000015"))).willReturn(Optional.of(interestList));
        given(interestListRepository.save(interestList)).willReturn(interestList);

        interestListService.toggleStatus(UUID.fromString("00000000-0000-4000-8000-000000000015"), new InterestListToggleStatus(ListStatus.PROCESSING));

        assertThat(interestList.getListStatus()).isEqualTo(ListStatus.PROCESSING);
        verify(eventPublisher).publish(
                eq(BusinessEventType.DEMAND_STATUS_CHANGED),
                eq("InterestList"),
                eq(UUID.fromString("00000000-0000-4000-8000-000000000015")),
                any(),
                any()
        );
        verify(auditService).record(
                eq(AuditAction.DEMAND_STATUS_CHANGED),
                eq("InterestList"),
                eq(UUID.fromString("00000000-0000-4000-8000-000000000015")),
                any(),
                any(),
                any()
        );
    }
}
