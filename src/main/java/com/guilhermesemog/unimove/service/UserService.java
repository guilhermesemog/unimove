package com.guilhermesemog.unimove.service;

import com.guilhermesemog.unimove.dto.user.UserCreate;
import com.guilhermesemog.unimove.dto.user.UserPatch;
import com.guilhermesemog.unimove.dto.user.UserResponse;
import com.guilhermesemog.unimove.dto.user.UserUpdate;
import com.guilhermesemog.unimove.exception.ResourceNotFoundException;
import com.guilhermesemog.unimove.mapper.UserMapper;
import com.guilhermesemog.unimove.model.User;
import com.guilhermesemog.unimove.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserResponse create(UserCreate userCreate) {
        User user = userMapper.toEntity(userCreate);
        return userMapper.toResponseBody(userRepository.save(user));
    }

    public UserResponse getById(Long id) {
        User user = getUser(id);
        return userMapper.toResponseBody(user);
    }

    public Page<UserResponse> getAll(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return userRepository.findAll(pageable).map(userMapper::toResponseBody);
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
