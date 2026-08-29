package com.guilhermesemog.unimove.service;

import com.guilhermesemog.unimove.dto.admin.AdminOperationResponse;
import com.guilhermesemog.unimove.mapper.InterestListMapper;
import com.guilhermesemog.unimove.mapper.TripMapper;
import com.guilhermesemog.unimove.model.Trip;
import com.guilhermesemog.unimove.repository.BookingCountView;
import com.guilhermesemog.unimove.repository.BookingRepository;
import com.guilhermesemog.unimove.repository.InterestListRepository;
import com.guilhermesemog.unimove.repository.TripRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminOperationService {

    private final InterestListRepository interestListRepository;
    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;
    private final InterestListMapper interestListMapper;
    private final TripMapper tripMapper;

    public AdminOperationService(
            InterestListRepository interestListRepository,
            BookingRepository bookingRepository,
            TripRepository tripRepository,
            InterestListMapper interestListMapper,
            TripMapper tripMapper
    ) {
        this.interestListRepository = interestListRepository;
        this.bookingRepository = bookingRepository;
        this.tripRepository = tripRepository;
        this.interestListMapper = interestListMapper;
        this.tripMapper = tripMapper;
    }

    @Transactional(readOnly = true)
    public List<AdminOperationResponse> getAll() {
        Map<Long, Long> bookingCounts = bookingRepository.countBookingsByInterestList().stream()
                .collect(Collectors.toMap(BookingCountView::getInterestListId, BookingCountView::getBookingCount));
        Map<Long, Trip> tripsByDemand = tripRepository.findAll().stream()
                .collect(Collectors.toMap(
                        trip -> trip.getInterestList().getId(),
                        Function.identity(),
                        (first, ignored) -> first
                ));

        return interestListRepository.findAll(Sort.by("referenceDate").ascending()).stream()
                .map(demand -> {
                    Trip trip = tripsByDemand.get(demand.getId());
                    return new AdminOperationResponse(
                            interestListMapper.toResponse(demand),
                            bookingCounts.getOrDefault(demand.getId(), 0L),
                            trip == null ? null : tripMapper.toResponse(trip)
                    );
                })
                .toList();
    }
}
