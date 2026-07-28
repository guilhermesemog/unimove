package com.guilhermesemog.unimove.repository;

import com.guilhermesemog.unimove.model.BoardingStop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardingStopRepository extends JpaRepository<BoardingStop, Long> {
    Page<BoardingStop> findAllByLocalContainsIgnoreCase(String local, Pageable pageable);
}
