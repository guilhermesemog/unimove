package com.guilhermesemog.unimove;

import com.guilhermesemog.unimove.dto.recurrence.RecurrenceGenerationResponse;
import com.guilhermesemog.unimove.dto.recurrence.RecurrencePlanRequest;
import com.guilhermesemog.unimove.dto.recurrence.RecurrencePlanResponse;
import com.guilhermesemog.unimove.dto.recurrence.RecurrenceStatusUpdate;
import com.guilhermesemog.unimove.exception.type.IllegalUpdateException;
import com.guilhermesemog.unimove.model.InterestList;
import com.guilhermesemog.unimove.model.University;
import com.guilhermesemog.unimove.model.enums.RecurrencePlanStatus;
import com.guilhermesemog.unimove.repository.InterestListRepository;
import com.guilhermesemog.unimove.repository.UniversityRepository;
import com.guilhermesemog.unimove.service.RecurrencePlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class RecurrencePersistenceIntegrationTest {
    @Autowired private RecurrencePlanService recurrencePlanService;
    @Autowired private UniversityRepository universityRepository;
    @Autowired private InterestListRepository interestListRepository;
    @Autowired private Clock businessClock;

    @Test
    @Transactional
    void shouldKeepPreviewAndRepeatedGenerationConsistentAndPreservePublishedDemandsWhenPaused() {
        LocalDate today = LocalDate.now(businessClock);
        University destination = universityRepository.save(new University(
                "Recurring Campus " + UUID.randomUUID(), "Recurring Address " + UUID.randomUUID()));
        InterestList conflict = new InterestList(
                today.plusDays(1), LocalTime.of(16, 0), LocalTime.of(17, 30), LocalTime.of(19, 0),
                LocalTime.of(23, 0), LocalTime.of(0, 30), destination);
        interestListRepository.save(conflict);

        RecurrencePlanRequest request = new RecurrencePlanRequest(
                "Every day test", destination.getId(), new LinkedHashSet<>(Arrays.asList(DayOfWeek.values())),
                today, today.plusDays(2), LocalTime.of(16, 0), LocalTime.of(17, 30), LocalTime.of(19, 0),
                LocalTime.of(23, 0), LocalTime.of(0, 30), 8);

        assertThat(recurrencePlanService.preview(request).createCount()).isEqualTo(2);
        assertThat(recurrencePlanService.preview(request).conflictCount()).isEqualTo(1);
        RecurrencePlanResponse plan = recurrencePlanService.create(request);

        RecurrenceGenerationResponse first = recurrencePlanService.generate(plan.id());
        RecurrenceGenerationResponse repeated = recurrencePlanService.generate(plan.id());

        assertThat(first.createdCount()).isEqualTo(2);
        assertThat(first.conflictCount()).isEqualTo(1);
        assertThat(repeated.createdCount()).isZero();
        assertThat(repeated.skippedCount()).isEqualTo(2);
        assertThat(repeated.conflictCount()).isEqualTo(1);
        assertThat(interestListRepository.countByRecurrencePlan_IdAndReferenceDateGreaterThanEqual(plan.id(), today))
                .isEqualTo(2);

        recurrencePlanService.updateStatus(plan.id(), new RecurrenceStatusUpdate(RecurrencePlanStatus.PAUSED));
        assertThatThrownBy(() -> recurrencePlanService.generate(plan.id())).isInstanceOf(IllegalUpdateException.class);
        assertThat(interestListRepository.countByRecurrencePlan_IdAndReferenceDateGreaterThanEqual(plan.id(), today))
                .isEqualTo(2);
    }
}
