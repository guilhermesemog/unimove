package com.guilhermesemog.unimove.services;

import com.guilhermesemog.unimove.exception.type.CpfAlreadyExistsException;
import com.guilhermesemog.unimove.model.User;
import com.guilhermesemog.unimove.model.enums.Role;
import com.guilhermesemog.unimove.repository.UserRepository;
import com.guilhermesemog.unimove.security.JwtService;
import com.guilhermesemog.unimove.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
public class AuthServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetails userDetails;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    private static String CPF = "12345678900";
    private static String RAW_PASSWORD = "12341234";
    private static String ENCODED_PASSWORD = "43214321";
    private static String ACCESS_TOKEN = "access-token";
    private static String REFRESH_TOKEN = "refresh-token";

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                authenticationManager,
                jwtService,
                userDetailsService
        );
    }

    @Nested
    @DisplayName("createAuthenticatableUser Tests")
    class CreateAuthenticatableUserTests {

        @Test
        @DisplayName("should create user when CPF does not exist")
        void shouldCreateUserWhenCPFNotExists() {
            given(userRepository.existsByCpf(CPF)).willReturn(false);
            given(passwordEncoder.encode(RAW_PASSWORD)).willReturn(ENCODED_PASSWORD);

            User user = authService.createAuthenticatableUser(CPF, RAW_PASSWORD, Role.ADMIN);

            assertThat(user).isNotNull();
            assertThat(user.getCpf()).isEqualTo(CPF);
            assertThat(user.getPassword()).isEqualTo(ENCODED_PASSWORD);
            assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        }

        @Test
        @DisplayName("should throw exception when CPF already exists")
        void shouldThrowExceptionWhenCPFExists() {
            given(userRepository.existsByCpf(CPF)).willReturn(true);

            assertThatThrownBy(() -> authService.createAuthenticatableUser(CPF, RAW_PASSWORD, Role.ADMIN))
                    .isInstanceOf(CpfAlreadyExistsException.class)
                    .hasMessage("CPF already exists: " + CPF);

            verify(passwordEncoder, never()).encode(anyString());
        }

        @Test
        @DisplayName("should encode password before create user")
        void shouldEncodePasswordBeforeCreateUser() {
            given(userRepository.existsByCpf(CPF)).willReturn(false);
            given(passwordEncoder.encode(RAW_PASSWORD)).willReturn(ENCODED_PASSWORD);

            authService.createAuthenticatableUser(CPF, RAW_PASSWORD, Role.ADMIN);

            verify(passwordEncoder, times(1)).encode(RAW_PASSWORD);
        }

        @Test
        @DisplayName("should create user with the specified role")
        void shouldCreateUserWithTheSpecifiedRole() {
            given(userRepository.existsByCpf(CPF)).willReturn(false);
            given(passwordEncoder.encode(RAW_PASSWORD)).willReturn(ENCODED_PASSWORD);

            User user = authService.createAuthenticatableUser(CPF, RAW_PASSWORD, Role.CONDUCTOR);

            assertThat(user.getRole()).isEqualTo(Role.CONDUCTOR);
        }

    }

}
