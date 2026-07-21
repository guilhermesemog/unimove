package com.guilhermesemog.unimove.service;

import com.guilhermesemog.unimove.dto.user.UserCreate;
import com.guilhermesemog.unimove.dto.user.UserPatch;
import com.guilhermesemog.unimove.dto.user.UserResponse;
import com.guilhermesemog.unimove.dto.user.UserUpdate;
import com.guilhermesemog.unimove.exception.type.ResourceNotFoundException;
import com.guilhermesemog.unimove.mapper.UserMapper;
import com.guilhermesemog.unimove.model.User;
import com.guilhermesemog.unimove.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthService authService;

    public UserService(UserRepository userRepository, UserMapper userMapper, AuthService authService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.authService = authService;
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

    public UserResponse getById(Long id) {
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

    public void delete(Long id) {
        User user = getUser(id);
        userRepository.delete(user);
    }

    public void update(Long id, UserUpdate userUpdate) {
        User user = getUser(id);
        user = userMapper.updateUser(userUpdate, user);
        userRepository.save(user);
    }

    public void update(Long id, UserPatch userPatch) {
        User user = getUser(id);
        user = userMapper.updateUser(userPatch, user);
        userRepository.save(user);
    }

    public void toggleStatus(Long id) {
        User user = getUser(id);
        user.setActive(!user.getActive());
        userRepository.save(user);
    }

    private User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
