package com.guilhermesemog.unimove.service;

import java.util.UUID;
import com.guilhermesemog.unimove.dto.user.UserCreate;
import com.guilhermesemog.unimove.dto.user.UserPatch;
import com.guilhermesemog.unimove.dto.user.UserResponse;
import com.guilhermesemog.unimove.dto.user.UserUpdate;
import com.guilhermesemog.unimove.exception.type.CpfAlreadyExistsException;
import com.guilhermesemog.unimove.exception.type.ResourceNotFoundException;
import com.guilhermesemog.unimove.mapper.UserMapper;
import com.guilhermesemog.unimove.model.User;
import com.guilhermesemog.unimove.model.enums.AuditAction;
import com.guilhermesemog.unimove.repository.ConductorRepository;
import com.guilhermesemog.unimove.repository.StudentRepository;
import com.guilhermesemog.unimove.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ConductorRepository conductorRepository;

    private final UserMapper userMapper;
    private final AuthService authService;
    private final UserValidationService userValidationService;
    private final AuditService auditService;

    public UserService(UserRepository userRepository, StudentRepository studentRepository,
                       ConductorRepository conductorRepository, UserMapper userMapper, AuthService authService,
                       UserValidationService userValidationService, AuditService auditService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.authService = authService;
        this.studentRepository = studentRepository;
        this.conductorRepository = conductorRepository;
        this.userValidationService = userValidationService;
        this.auditService = auditService;
    }

    public UserResponse create(UserCreate userCreate) {
        User user = authService.createAuthenticatableUser(userCreate.user().cpf(), userCreate.user().password(),
                userCreate.role());

        user.setFirstName(userCreate.user().firstName());
        user.setLastName(userCreate.user().lastName());
        user.setPhone(userCreate.user().phone());

        return userMapper.toResponse(userRepository.save(user));
    }

    public UserResponse getByAuthentication(Authentication authentication) {
        return userMapper.toResponse(userRepository.findByCpf(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }

    public UserResponse getById(UUID id) {
        User user = getUser(id);
        return userMapper.toResponse(user);
    }

    public Page<UserResponse> getAll(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    public Page<UserResponse> getAllByFullName(int page, int size, String sortBy, String sortDirection,
                                               String fullName) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        String safeFullName = fullName != null ? fullName : "";

        return userRepository.findAllByFullName(safeFullName, pageable).map(userMapper::toResponse);
    }

    public void delete(UUID id) {
        User user = getUser(id);
        studentRepository.findById(id).ifPresent(studentRepository::delete);
        conductorRepository.findById(id).ifPresent(conductorRepository::delete);
        userRepository.delete(user);
    }

    public void update(UUID id, UserUpdate userUpdate) {
        User user = getUser(id);
        this.userValidationService.validateCpf(id, userUpdate.cpf());
        user = userMapper.updateUser(userUpdate, user);
        userRepository.save(user);
    }

    public void update(UUID id, UserPatch userPatch) {
        User user = getUser(id);
        this.userValidationService.validateCpf(id, userPatch.cpf());
        user = userMapper.updateUser(userPatch, user);
        userRepository.save(user);
    }

    @Transactional
    public void toggleStatus(UUID id) {
        User user = getUser(id);
        Map<String, Object> previousState = userStatusAuditState(user);
        user.setActive(!user.getActive());
        User savedUser = userRepository.save(user);
        auditService.record(
                savedUser.getActive() ? AuditAction.USER_ACTIVATED : AuditAction.USER_DEACTIVATED,
                "User",
                savedUser.getId(),
                previousState,
                userStatusAuditState(savedUser),
                Map.of()
        );
    }

    private User getUser(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Map<String, Object> userStatusAuditState(User user) {
        return Map.of(
                "active", user.getActive(),
                "role", user.getRole().name()
        );
    }
}
