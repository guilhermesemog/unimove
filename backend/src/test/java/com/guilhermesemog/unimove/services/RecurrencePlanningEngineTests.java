package com.guilhermesemog.unimove.services;

import com.guilhermesemog.unimove.dto.recurrence.RecurrencePlanRequest;
import com.guilhermesemog.unimove.dto.recurrence.RecurrencePreviewResponse;
import com.guilhermesemog.unimove.model.InterestList;
import com.guilhermesemog.unimove.model.RecurrencePlan;
import com.guilhermesemog.unimove.model.enums.RecurrenceOccurrenceAction;
import com.guilhermesemog.unimove.repository.InterestListRepository;
import com.guilhermesemog.unimove.service.RecurrencePlanningEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class RecurrencePlanningEngineTests {
    @Mock private InterestListRepository interestListRepository;

    @Test
    void shouldUseSeveralWeekdaysAndClassifyExistingDatesWithoutWriting() {
        UUID destinationId = UUID.fromString("00000000-0000-4000-8000-000000000001");
        UUID planId = UUID.fromString("00000000-0000-4000-8000-000000000002");
        RecurrencePlan plan = new RecurrencePlan();
        plan.setId(planId);

        InterestList generated = demand("00000000-0000-4000-8000-000000000010", LocalDate.of(2026, 9, 1));
        generated.setRecurrencePlan(plan);
        InterestList conflicting = demand("00000000-0000-4000-8000-000000000011", LocalDate.of(2026, 9, 3));
        given(interestListRepository.findAllByDestination_IdAndReferenceDateBetween(
                destinationId, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 8)))
                .willReturn(List.of(generated, conflicting));

        RecurrencePlanningEngine engine = new RecurrencePlanningEngine(
                interestListRepository,
                Clock.fixed(Instant.parse("2026-09-01T12:00:00Z"), ZoneId.of("America/Sao_Paulo")),
                8
        );
        RecurrencePreviewResponse result = engine.preview(request(destinationId, 1), planId);

        assertThat(result.occurrences()).extracting(item -> item.action()).containsExactly(
                RecurrenceOccurrenceAction.SKIP,
                RecurrenceOccurrenceAction.CONFLICT,
                RecurrenceOccurrenceAction.CREATE
        );
        assertThat(result.createCount()).isEqualTo(1);
        assertThat(result.skipCount()).isEqualTo(1);
        assertThat(result.conflictCount()).isEqualTo(1);
        verify(interestListRepository).findAllByDestination_IdAndReferenceDateBetween(
                destinationId, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 8));
        verifyNoMoreInteractions(interestListRepository);
    }

    @Test
    void shouldUseConfiguredDefaultHorizonWhenRequestOmitsIt() {
        UUID destinationId = UUID.fromString("00000000-0000-4000-8000-000000000001");
        given(interestListRepository.findAllByDestination_IdAndReferenceDateBetween(
                destinationId, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 15))).willReturn(List.of());
        RecurrencePlanningEngine engine = new RecurrencePlanningEngine(
                interestListRepository,
                Clock.fixed(Instant.parse("2026-09-01T12:00:00Z"), ZoneId.of("America/Sao_Paulo")),
                2
        );
        RecurrencePlanRequest request = new RecurrencePlanRequest(
                "Campus", destinationId, Set.of(DayOfWeek.TUESDAY), LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 12, 1), LocalTime.of(16, 0), LocalTime.of(17, 30),
                LocalTime.of(19, 0), LocalTime.of(23, 0), LocalTime.of(0, 30), null
        );

        RecurrencePreviewResponse result = engine.preview(request, null);

        assertThat(result.rangeStart()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(result.rangeEnd()).isEqualTo(LocalDate.of(2026, 9, 15));
        assertThat(result.createCount()).isEqualTo(3);
    }

    private RecurrencePlanRequest request(UUID destinationId, Integer horizon) {
        return new RecurrencePlanRequest(
                "Campus", destinationId, Set.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 1), LocalTime.of(16, 0),
                LocalTime.of(17, 30), LocalTime.of(19, 0), LocalTime.of(23, 0), LocalTime.of(0, 30), horizon
        );
    }

    private InterestList demand(String id, LocalDate date) {
        InterestList demand = new InterestList();
        demand.setId(UUID.fromString(id));
        demand.setReferenceDate(date);
        return demand;
    }
}
