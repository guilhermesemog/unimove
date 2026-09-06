package com.guilhermesemog.unimove.service;

import java.util.UUID;
import com.guilhermesemog.unimove.dto.conductor.ConductorCreate;
import com.guilhermesemog.unimove.dto.conductor.ConductorPatch;
import com.guilhermesemog.unimove.dto.conductor.ConductorResponse;
import com.guilhermesemog.unimove.dto.conductor.ConductorUpdate;
import com.guilhermesemog.unimove.exception.type.ResourceNotFoundException;
import com.guilhermesemog.unimove.mapper.ConductorMapper;
import com.guilhermesemog.unimove.model.Conductor;
import com.guilhermesemog.unimove.model.User;
import com.guilhermesemog.unimove.model.enums.Role;
import com.guilhermesemog.unimove.repository.ConductorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConductorService {

    private final ConductorRepository conductorRepository;
    private final ConductorMapper conductorMapper;
    private final AuthService authService;
    private final UserValidationService userValidationService;

    public ConductorService(ConductorRepository conductorRepository, ConductorMapper conductorMapper, AuthService authService, UserValidationService userValidationService) {
        this.conductorRepository = conductorRepository;
        this.conductorMapper = conductorMapper;
        this.authService = authService;
        this.userValidationService = userValidationService;
    }

    public ConductorResponse create(ConductorCreate requestBody) {
        User user = authService.createAuthenticatableUser(requestBody.user().cpf(), requestBody.user().password(), Role.CONDUCTOR);
        user.setFirstName(requestBody.user().firstName());
        user.setLastName(requestBody.user().lastName());
        user.setPhone(requestBody.user().phone());

        Conductor conductor = conductorMapper.toEntity(requestBody, user);

        return conductorMapper.toResponse(conductorRepository.save(conductor));
    }

    public ConductorResponse getById(UUID id) {
        return conductorMapper.toResponse(getConductor(id));
    }

    public Page<ConductorResponse> getAll(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return conductorRepository.findAll(pageable).map(conductorMapper::toResponse);
    }

    public List<ConductorResponse> getAll() {
        return conductorRepository.findAll().stream()
                .map(conductorMapper::toResponse)
                .toList();
    }

    public void delete(UUID id) {
        conductorRepository.delete(getConductor(id));
    }

    public void update(UUID id, ConductorUpdate requestBody) {
        Conductor conductor = getConductor(id);
        this.userValidationService.validateCpf(id, requestBody.user().cpf());
        conductor = conductorMapper.update(requestBody, conductor);
        conductorRepository.save(conductor);
    }

    public void update(UUID id, ConductorPatch requestBody) {
        Conductor conductor = getConductor(id);
        this.userValidationService.validateCpf(id, requestBody.user().cpf());
        conductor = conductorMapper.update(requestBody, conductor);
        conductorRepository.save(conductor);
    }

    private Conductor getConductor(UUID id) {
        return conductorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Conductor not found"));
    }
}
