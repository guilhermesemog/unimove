package com.guilhermesemog.unimove.repository;

import java.util.UUID;
import com.guilhermesemog.unimove.model.BoardingStop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardingStopRepository extends JpaRepository<BoardingStop, UUID> {
    Page<BoardingStop> findAllByLocalContainsIgnoreCase(String local, Pageable pageable);
}
