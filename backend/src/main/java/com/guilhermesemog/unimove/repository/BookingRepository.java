package com.guilhermesemog.unimove.repository;

import com.guilhermesemog.unimove.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findByInterestList_Id(Long studentId, Pageable pageable);

    List<Booking> findByInterestList_Id(Long studentId);

    Page<Booking> findByStudentId(Long studentId, Pageable pageable);

    boolean existsByInterestList_Id(Long studentId);

    boolean existsByStudent_IdAndInterestList_Id(Long studentId, Long interestListId);
}
