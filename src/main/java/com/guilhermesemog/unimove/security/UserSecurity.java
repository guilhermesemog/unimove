package com.guilhermesemog.unimove.security;

import com.guilhermesemog.unimove.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class UserSecurity {

    private final UserRepository userRepository;

    public UserSecurity(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isOwner(Long id, Authentication authentication) {
        return userRepository.findByCpf(authentication.getName())
                .map(user -> user.getId().equals(id))
                .orElse(false);
    }
}
