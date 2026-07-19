package com.guilhermesemog.unimove.service;

import com.guilhermesemog.unimove.dto.auth.LoginRequest;
import com.guilhermesemog.unimove.dto.auth.LoginResponse;
import com.guilhermesemog.unimove.dto.auth.RegisterRequest;
import com.guilhermesemog.unimove.exception.type.CpfAlreadyExistsException;
import com.guilhermesemog.unimove.model.User;
import com.guilhermesemog.unimove.model.enums.Role;
import com.guilhermesemog.unimove.repository.UserRepository;
import com.guilhermesemog.unimove.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;


    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService, UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    public User createAuthenticatableUser(String cpf, String password, Role role) {
        if (userRepository.existsByCpf(cpf)) {
            throw new CpfAlreadyExistsException("CPF already exists: " + cpf);
        }

        return new User(cpf, passwordEncoder.encode(password), role);
    }

    public LoginResponse register(RegisterRequest registerRequest) {
        User user = createAuthenticatableUser(registerRequest.cpf(), registerRequest.password(), Role.ADMIN);

        user.setFirstName(registerRequest.firstName());
        user.setLastName(registerRequest.lastName());

        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(registerRequest.cpf());

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return new LoginResponse(accessToken, refreshToken);
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.cpf(), request.password())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        if (userDetails == null) {
            throw new BadCredentialsException("Invalid username or password");
        }

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return new LoginResponse(accessToken, refreshToken);
    }

    public LoginResponse refresh(String token) {

        String cpf = jwtService.extractUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(cpf);

        if (!jwtService.isValidToken(token, userDetails, "refresh")) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return new LoginResponse(accessToken, refreshToken);
    }
}