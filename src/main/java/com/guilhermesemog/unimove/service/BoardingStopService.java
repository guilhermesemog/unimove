package com.guilhermesemog.unimove.service;

import com.guilhermesemog.unimove.dto.boardingstop.BoardingStopCreate;
import com.guilhermesemog.unimove.dto.boardingstop.BoardingStopResponse;
import com.guilhermesemog.unimove.exception.ResourceNotFoundException;
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

    public BoardingStopResponse create(BoardingStopCreate createBody) {
        BoardingStop boardingStop = boardingStopMapper.toEntity(createBody);
        return boardingStopMapper.toResponseBody(boardingStopRepository.save(boardingStop));
    }

    public BoardingStopResponse getById(Long id) {
        return boardingStopMapper.toResponseBody(getBoardingStop(id));
    }

    public Page<BoardingStopResponse> getAll(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return boardingStopRepository.findAll(pageable).map(boardingStopMapper::toResponseBody);
    }

    public void delete(Long id) {
        boardingStopRepository.delete(getBoardingStop(id));
    }

    private BoardingStop getBoardingStop(Long id) {
        return boardingStopRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Boarding stop not found"));
    }
}
