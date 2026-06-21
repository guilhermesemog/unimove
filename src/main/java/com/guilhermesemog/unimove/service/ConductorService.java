package com.guilhermesemog.unimove.service;

import com.guilhermesemog.unimove.dto.conductor.ConductorCreate;
import com.guilhermesemog.unimove.dto.conductor.ConductorPatch;
import com.guilhermesemog.unimove.dto.conductor.ConductorResponse;
import com.guilhermesemog.unimove.dto.conductor.ConductorUpdate;
import com.guilhermesemog.unimove.exception.ResourceNotFoundException;
import com.guilhermesemog.unimove.mapper.ConductorMapper;
import com.guilhermesemog.unimove.mapper.UserMapper;
import com.guilhermesemog.unimove.model.Conductor;
import com.guilhermesemog.unimove.model.User;
import com.guilhermesemog.unimove.model.enums.Role;
import com.guilhermesemog.unimove.repository.ConductorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ConductorService {

    private final ConductorRepository conductorRepository;
    private final ConductorMapper conductorMapper;
    private final UserMapper userMapper;

    public ConductorService(ConductorRepository conductorRepository, ConductorMapper conductorMapper, UserMapper userMapper) {
        this.conductorRepository = conductorRepository;
        this.conductorMapper = conductorMapper;
        this.userMapper = userMapper;
    }

    public ConductorResponse create(ConductorCreate requestBody) {
        User user = userMapper.toEntity(requestBody.user(), Role.CONDUCTOR);
        Conductor conductor = conductorMapper.toEntity(requestBody, user);

        return conductorMapper.toResponse(conductorRepository.save(conductor));
    }

    public ConductorResponse getById(Long id) {
        return conductorMapper.toResponse(getConductor(id));
    }

    public Page<ConductorResponse> getAll(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return conductorRepository.findAll(pageable).map(conductorMapper::toResponse);
    }

    public void delete(Long id) {
        conductorRepository.delete(getConductor(id));
    }

    public void update(Long id, ConductorUpdate requestBody) {
        Conductor conductor = getConductor(id);

        conductor = conductorMapper.update(requestBody, conductor);

        conductorRepository.save(conductor);
    }

    public void update(Long id, ConductorPatch requestBody) {
        Conductor conductor = getConductor(id);

        conductor = conductorMapper.update(requestBody, conductor);

        conductorRepository.save(conductor);
    }

    private Conductor getConductor(Long id) {
        return conductorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Conductor not found"));
    }
}
