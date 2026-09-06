package com.guilhermesemog.unimove.repository;

import java.util.UUID;
import com.guilhermesemog.unimove.model.Booking;
import com.guilhermesemog.unimove.model.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    Page<Booking> findAllByInterestList_IdAndBookingStatusNot(UUID interestListId, BookingStatus bookingStatus, Pageable pageable);

    List<Booking> findAllByInterestList_IdAndBookingStatusNot(UUID interestListId, BookingStatus bookingStatus);

    @EntityGraph(attributePaths = {"student.user", "boardingLocation"})
    List<Booking> findAllByInterestList_IdAndBookingStatus(UUID interestListId, BookingStatus bookingStatus);

    Page<Booking> findAllByStudentIdAndBookingStatusNot(UUID studentId, BookingStatus bookingStatus, Pageable pageable);

    boolean existsByInterestList_Id(UUID interestListId);

    Optional<Booking> findByStudent_IdAndInterestList_Id(UUID studentId, UUID interestListId);

    @Query("""
            SELECT b.interestList.id AS interestListId, COUNT(b.id) AS bookingCount
            FROM Booking b
            WHERE b.bookingStatus <> com.guilhermesemog.unimove.model.enums.BookingStatus.CANCELLED
            GROUP BY b.interestList.id
            """)
    List<BookingCountView> countBookingsByInterestList();
}
