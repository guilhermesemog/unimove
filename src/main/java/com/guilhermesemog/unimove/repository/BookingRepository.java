package com.guilhermesemog.unimove.repository;

import com.guilhermesemog.unimove.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByInterestList_Id(Long studentId);

    List<Booking> findByStudentId(Long studentId);

    boolean existsByInterestList_Id(Long studentId);

    boolean existsByStudent_IdAndInterestList_Id(Long studentId, Long interestListId);
}
