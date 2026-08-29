package com.guilhermesemog.unimove.repository;

import com.guilhermesemog.unimove.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findAllByInterestList_Id(Long interestListId, Pageable pageable);

    List<Booking> findAllByInterestList_Id(Long interestListId);

    Page<Booking> findAllByStudentId(Long studentId, Pageable pageable);

    boolean existsByInterestList_Id(Long interestListId);

    boolean existsByStudent_IdAndInterestList_Id(Long studentId, Long interestListId);

    @Query("""
            SELECT b.interestList.id AS interestListId, COUNT(b.id) AS bookingCount
            FROM Booking b
            GROUP BY b.interestList.id
            """)
    List<BookingCountView> countBookingsByInterestList();
}
