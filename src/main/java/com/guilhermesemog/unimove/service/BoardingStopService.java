package com.guilhermesemog.unimove.service;

import com.guilhermesemog.unimove.dto.boardingstop.BoardingStopPostRequestBody;
import com.guilhermesemog.unimove.dto.boardingstop.BoardingStopResponseBody;
import com.guilhermesemog.unimove.mapper.BoardingStopMapper;
import com.guilhermesemog.unimove.model.BoardingStop;
import com.guilhermesemog.unimove.repository.BoardingStopRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class BoardingStopService {

    private final BoardingStopRepository boardingStopRepository;
    private final BoardingStopMapper boardingStopMapper;

    public BoardingStopService(BoardingStopRepository boardingStopRepository, BoardingStopMapper boardingStopMapper) {
        this.boardingStopRepository = boardingStopRepository;
        this.boardingStopMapper = boardingStopMapper;
    }

    public BoardingStopResponseBody create(BoardingStopPostRequestBody boardingStopPostRequestBody) {
        BoardingStop boardingStop = boardingStopMapper.toEntity(boardingStopPostRequestBody);
        return boardingStopMapper.toResponseBody(boardingStopRepository.save(boardingStop));
    }

    public BoardingStopResponseBody getById(Long id) {
        BoardingStop boardingStop = boardingStopRepository.findById(id).orElseThrow(() -> new RuntimeException("Boarding stop not found"));
        return boardingStopMapper.toResponseBody(boardingStop);
    }

    public Page<BoardingStopResponseBody> getAll(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return boardingStopRepository.findAll(pageable).map(boardingStopMapper::toResponseBody);
    }

    public void delete(Long id) {
        BoardingStop boardingStop = boardingStopRepository.findById(id).orElseThrow(() -> new RuntimeException("Boarding stop not found"));
        boardingStopRepository.delete(boardingStop);
    }
}
