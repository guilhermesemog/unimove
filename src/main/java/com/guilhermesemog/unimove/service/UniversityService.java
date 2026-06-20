package com.guilhermesemog.unimove.service;

import com.guilhermesemog.unimove.dto.university.UniversityCreate;
import com.guilhermesemog.unimove.dto.university.UniversityPatch;
import com.guilhermesemog.unimove.dto.university.UniversityResponse;
import com.guilhermesemog.unimove.dto.university.UniversityUpdate;
import com.guilhermesemog.unimove.exception.ResourceNotFoundException;
import com.guilhermesemog.unimove.mapper.UniversityMapper;
import com.guilhermesemog.unimove.model.University;
import com.guilhermesemog.unimove.repository.UniversityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class UniversityService {

    private final UniversityRepository universityRepository;
    private final UniversityMapper universityMapper;

    public UniversityService(UniversityRepository universityRepository, UniversityMapper universityMapper) {
        this.universityRepository = universityRepository;
        this.universityMapper = universityMapper;
    }

    public UniversityResponse create(UniversityCreate requestBody) {
        University university = universityMapper.toEntity(requestBody);
        return universityMapper.toResponse(universityRepository.save(university));
    }

    public UniversityResponse getById(Long id) {
        University university = getUniversity(id);
        return universityMapper.toResponse(university);
    }

    public UniversityResponse getByName(String name) {
        University university = getUniversityByName(name);
        return universityMapper.toResponse(university);
    }

    public Page<UniversityResponse> getAll(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return universityRepository.findAll(pageable).map(universityMapper::toResponse);
    }

    public void delete(Long id) {
        University university = getUniversity(id);
        universityRepository.delete(university);
    }

    public void update(Long id, UniversityUpdate requestBody) {
        University university = getUniversity(id);
        university = universityMapper.updateUniversity(requestBody, university);
        universityRepository.save(university);
    }

    public void update(Long id, UniversityPatch requestBody) {
        University university = getUniversity(id);
        university = universityMapper.updateUniversity(requestBody, university);
        universityRepository.save(university);
    }

    private University getUniversity(Long id) {
        return universityRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("University not found"));
    }

    private University getUniversityByName(String name) {
        return universityRepository.findByName(name).orElseThrow(() -> new ResourceNotFoundException("University not found"));
    }

}
