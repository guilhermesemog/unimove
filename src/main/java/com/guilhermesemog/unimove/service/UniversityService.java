package com.guilhermesemog.unimove.service;

import com.guilhermesemog.unimove.dto.university.UniversityPatchRequestBody;
import com.guilhermesemog.unimove.dto.university.UniversityPostRequestBody;
import com.guilhermesemog.unimove.dto.university.UniversityPutRequestBody;
import com.guilhermesemog.unimove.dto.university.UniversityResponseBody;
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

    public UniversityResponseBody create(UniversityPostRequestBody universityPostRequestBody) {
        University university = universityMapper.toEntity(universityPostRequestBody);
        return universityMapper.toResponseBody(universityRepository.save(university));
    }

    public UniversityResponseBody getById(Long id) {
        University university = universityRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("University not found"));
        return universityMapper.toResponseBody(university);
    }

    public UniversityResponseBody getByName(String name) {
        University university = universityRepository.findByName(name).orElseThrow(() -> new ResourceNotFoundException("University not found"));
        return universityMapper.toResponseBody(university);
    }

    public Page<UniversityResponseBody> getAll(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return universityRepository.findAll(pageable).map(universityMapper::toResponseBody);
    }

    public void delete(Long id) {
        University university = universityRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("University not found"));
        universityRepository.delete(university);
    }

    public void update(Long id, UniversityPutRequestBody universityPutRequestBody) {
        University university = universityRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("University not found"));
        university = universityMapper.updateUniversity(universityPutRequestBody, university);
        universityRepository.save(university);
    }

    public void update(Long id, UniversityPatchRequestBody universityPatchRequestBody) {
        University university = universityRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("University not found"));
        university = universityMapper.updateUniversity(universityPatchRequestBody, university);
        universityRepository.save(university);
    }

}
