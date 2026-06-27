package com.guilhermesemog.unimove.security;

import com.guilhermesemog.unimove.model.User;
import com.guilhermesemog.unimove.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String cpf) {
        User user = userRepository.findByCpf(cpf).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!user.getActive()) {
            throw new DisabledException("User account is disabled");
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getCpf())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole())
                .disabled(!user.getActive())
                .build();
    }
}
