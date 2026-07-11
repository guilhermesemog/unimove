package com.guilhermesemog.unimove.service;

import com.guilhermesemog.unimove.dto.interestlist.*;
import com.guilhermesemog.unimove.exception.type.IllegalUpdateException;
import com.guilhermesemog.unimove.exception.type.ResourceNotFoundException;
import com.guilhermesemog.unimove.mapper.InterestListMapper;
import com.guilhermesemog.unimove.model.InterestList;
import com.guilhermesemog.unimove.model.enums.ListStatus;
import com.guilhermesemog.unimove.repository.BookingRepository;
import com.guilhermesemog.unimove.repository.InterestListRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class InterestListService {

    private final InterestListRepository interestListRepository;
    private final InterestListMapper interestListMapper;
    private final BookingRepository bookingRepository;

    public InterestListService(InterestListMapper interestListMapper, InterestListRepository interestListRepository, BookingRepository bookingRepository) {
        this.interestListMapper = interestListMapper;
        this.interestListRepository = interestListRepository;
        this.bookingRepository = bookingRepository;
    }

    public InterestListResponse create(InterestListCreate requestBody) {
        InterestList interestList = interestListMapper.toEntity(requestBody);
        interestList = interestListRepository.save(interestList);
        return interestListMapper.toResponse(interestList);
    }

    public InterestListResponse getById(Long id) {
        InterestList interestList = getInterestList(id);
        return interestListMapper.toResponse(interestList);
    }

    public Page<InterestListResponse> getAll(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return interestListRepository.findAll(pageable).map(interestListMapper::toResponse);
    }

    public void delete(Long id) {
        InterestList interestList = getInterestList(id);

        if (bookingRepository.existsByInterestList_Id(id)) {
            throw new IllegalUpdateException("Cannot delete interest list with existing bookings");
        }

        interestListRepository.delete(interestList);
    }

    public void update(Long id, InterestListUpdate requestBody) {
        InterestList interestList = getInterestList(id);
        interestListMapper.update(requestBody, interestList);
        interestListRepository.save(interestList);
    }

    public void update(Long id, InterestListPatch requestBody) {
        InterestList interestList = getInterestList(id);
        interestListMapper.update(requestBody, interestList);
        interestListRepository.save(interestList);
    }

    public void toggleStatus(Long id, InterestListToggleStatus requestBody) {
        InterestList interestList = getInterestList(id);

        if (requestBody.listStatus() != null) {
            interestList.setListStatus(requestBody.listStatus());
        } else {
            if (interestList.getListStatus() == ListStatus.OPEN) {
                interestList.setListStatus(ListStatus.PROCESSING);
            } else if (interestList.getListStatus() == ListStatus.PROCESSING) {
                interestList.setListStatus(ListStatus.CLOSED);
            }
        }

        interestListRepository.save(interestList);
    }

    private InterestList getInterestList(Long id) {
        return interestListRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("InterestList not found"));
    }
}
