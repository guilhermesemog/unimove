package com.guilhermesemog.unimove.repository;

import com.guilhermesemog.unimove.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    public List<Booking> findByInterestList_Id(Long studentId);

    public List<Booking> findByStudentId(Long studentId);

    boolean existsByStudent_Id(Long studentId);

    boolean existsByStudent_IdAndInterestList_Id(Long studentId, Long interestListId);
}
