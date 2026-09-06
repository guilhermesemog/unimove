package com.guilhermesemog.unimove.services;

import java.util.UUID;
import com.guilhermesemog.unimove.dto.interestlist.InterestListResponse;
import com.guilhermesemog.unimove.dto.trip.TripResponse;
import com.guilhermesemog.unimove.mapper.InterestListMapper;
import com.guilhermesemog.unimove.mapper.TripMapper;
import com.guilhermesemog.unimove.model.InterestList;
import com.guilhermesemog.unimove.model.Trip;
import com.guilhermesemog.unimove.repository.BookingCountView;
import com.guilhermesemog.unimove.repository.BookingRepository;
import com.guilhermesemog.unimove.repository.InterestListRepository;
import com.guilhermesemog.unimove.repository.TripRepository;
import com.guilhermesemog.unimove.service.AdminOperationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminOperationService Tests")
class AdminOperationServiceTests {

    @Mock private InterestListRepository interestListRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private TripRepository tripRepository;
    @Mock private InterestListMapper interestListMapper;
    @Mock private TripMapper tripMapper;
    @Mock private BookingCountView bookingCount;
    @Mock private InterestListResponse demandResponse;
    @Mock private TripResponse tripResponse;

    private AdminOperationService service;

    @BeforeEach
    void setUp() {
        service = new AdminOperationService(
                interestListRepository,
                bookingRepository,
                tripRepository,
                interestListMapper,
                tripMapper
        );
    }

    @Test
    @DisplayName("should aggregate demand, booking count and trip without changing existing contracts")
    void shouldAggregateOperationData() {
        InterestList demand = new InterestList();
        demand.setId(UUID.fromString("00000000-0000-4000-8000-000000000010"));
        demand.setReferenceDate(LocalDate.of(2026, 8, 28));
        Trip trip = new Trip();
        trip.setId(UUID.fromString("00000000-0000-4000-8000-000000000020"));
        trip.setInterestList(demand);

        given(bookingCount.getInterestListId()).willReturn(UUID.fromString("00000000-0000-4000-8000-000000000010"));
        given(bookingCount.getBookingCount()).willReturn(14L);
        given(bookingRepository.countBookingsByInterestList()).willReturn(List.of(bookingCount));
        given(tripRepository.findAll()).willReturn(List.of(trip));
        given(interestListRepository.findAll(Sort.by("referenceDate").ascending())).willReturn(List.of(demand));
        given(interestListMapper.toResponse(demand)).willReturn(demandResponse);
        given(tripMapper.toResponse(trip)).willReturn(tripResponse);

        var result = service.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().demand()).isSameAs(demandResponse);
        assertThat(result.getFirst().bookingCount()).isEqualTo(14L);
        assertThat(result.getFirst().trip()).isSameAs(tripResponse);
    }

    @Test
    @DisplayName("should return zero bookings and no trip when planning has not started")
    void shouldHandleUnplannedDemand() {
        InterestList demand = new InterestList();
        demand.setId(UUID.fromString("00000000-0000-4000-8000-000000000011"));
        given(bookingRepository.countBookingsByInterestList()).willReturn(List.of());
        given(tripRepository.findAll()).willReturn(List.of());
        given(interestListRepository.findAll(Sort.by("referenceDate").ascending())).willReturn(List.of(demand));
        given(interestListMapper.toResponse(demand)).willReturn(demandResponse);

        var result = service.getAll();

        assertThat(result.getFirst().bookingCount()).isZero();
        assertThat(result.getFirst().trip()).isNull();
    }
}
