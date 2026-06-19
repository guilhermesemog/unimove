package com.guilhermesemog.unimove.service;

import com.guilhermesemog.unimove.dto.user.UserPatchRequestBody;
import com.guilhermesemog.unimove.dto.user.UserPostRequestBody;
import com.guilhermesemog.unimove.dto.user.UserPutRequestBody;
import com.guilhermesemog.unimove.dto.user.UserResponseBody;
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

    public UserResponseBody create(UserPostRequestBody userPostRequestBody) {
        User user = userMapper.toEntity(userPostRequestBody);
        return userMapper.toResponseBody(userRepository.save(user));
    }

    public UserResponseBody getById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toResponseBody(user);
    }

    public Page<UserResponseBody> getAll(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return userRepository.findAll(pageable).map(userMapper::toResponseBody);
    }

    public void delete(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
    }

    public void update(Long id, UserPutRequestBody userPutRequestBody) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user = userMapper.updateUser(userPutRequestBody, user);
        userRepository.save(user);
    }

    public void update(Long id, UserPatchRequestBody userPatchRequestBody) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user = userMapper.updateUser(userPatchRequestBody, user);
        userRepository.save(user);
    }

    public void toggleStatus(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setActive(!user.getActive());
        userRepository.save(user);
    }
}
