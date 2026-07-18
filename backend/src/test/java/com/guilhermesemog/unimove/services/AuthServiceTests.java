package com.guilhermesemog.unimove.services;

import com.guilhermesemog.unimove.dto.auth.LoginRequest;
import com.guilhermesemog.unimove.dto.auth.LoginResponse;
import com.guilhermesemog.unimove.dto.auth.RefreshRequest;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    @DisplayName("createAuthenticatableUser")
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
        @DisplayName("should create user with the conductor role")
        void shouldCreateUserWithConductorRole() {
            given(userRepository.existsByCpf(CPF)).willReturn(false);
            given(passwordEncoder.encode(RAW_PASSWORD)).willReturn(ENCODED_PASSWORD);

            User user = authService.createAuthenticatableUser(CPF, RAW_PASSWORD, Role.CONDUCTOR);

            assertThat(user.getRole()).isEqualTo(Role.CONDUCTOR);
        }

        @Test
        @DisplayName("should create user with the student role")
        void shouldCreateUserWithStudentRole() {
            given(userRepository.existsByCpf(CPF)).willReturn(false);
            given(passwordEncoder.encode(RAW_PASSWORD)).willReturn(ENCODED_PASSWORD);

            User user = authService.createAuthenticatableUser(CPF, RAW_PASSWORD, Role.STUDENT);

            assertThat(user.getRole()).isEqualTo(Role.STUDENT);
        }
    }

    @Nested
    @DisplayName("login")
    class LoginTests {

        @Test
        @DisplayName("should return LoginRespose with token when credentials are valid")
        void shouldLoginUser() {
            LoginRequest request = new LoginRequest(CPF, RAW_PASSWORD);

            given(authenticationManager.authenticate(any())).willReturn(authentication);
            given(authentication.getPrincipal()).willReturn(userDetails);
            given(jwtService.generateAccessToken(userDetails)).willReturn(ACCESS_TOKEN);
            given(jwtService.generateRefreshToken(userDetails)).willReturn(REFRESH_TOKEN);

            LoginResponse response = authService.login(request);

            verify(jwtService, times(1)).generateAccessToken(userDetails);
            verify(jwtService, times(1)).generateRefreshToken(userDetails);

            assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
        }


        @Test
        @DisplayName("should throw BadCredentialsException when AuthenticationManager rejects credentials")
        void shouldThrowBadCredentialsExceptionWhenAuthenticationFails() {
            LoginRequest request = new LoginRequest(CPF, RAW_PASSWORD);

            given(authenticationManager.authenticate(any())).willThrow(new BadCredentialsException("Invalid username or password"));

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Invalid username or password");

            verify(jwtService, never()).generateAccessToken(any());
            verify(jwtService, never()).generateRefreshToken(any());
        }

        @Test
        @DisplayName("should throw BadCredentialsException when user details is null")
        void shouldThrowBadCredentialsExceptionWhenCredentialsAreInvalid() {
            LoginRequest request = new LoginRequest(CPF, RAW_PASSWORD);

            given(authenticationManager.authenticate(any())).willReturn(authentication);
            given(authentication.getPrincipal()).willReturn(null);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Invalid username or password");

            verify(jwtService, never()).generateAccessToken(any());
            verify(jwtService, never()).generateRefreshToken(any());
        }
    }

    @Nested
    @DisplayName("refresh")
    class RefreshTests {

        @Test
        @DisplayName("should return new loginResponse when refresh token is valid")
        void shouldRefreshUserWhenRefreshTokenIsValid() {
            RefreshRequest request = new RefreshRequest(REFRESH_TOKEN);

            given(jwtService.extractUsername(REFRESH_TOKEN)).willReturn(CPF);
            given(userDetailsService.loadUserByUsername(CPF)).willReturn(userDetails);
            given(jwtService.isValidToken(REFRESH_TOKEN, userDetails, "refresh")).willReturn(true);
            given(jwtService.generateAccessToken(userDetails)).willReturn(ACCESS_TOKEN);
            given(jwtService.generateRefreshToken(userDetails)).willReturn(REFRESH_TOKEN);

            LoginResponse response = authService.refresh(request);

            assertThat(response).isNotNull();
            assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
        }

        @Test
        @DisplayName("should throw BadCredentials when token is invalid")
        void shouldThrowBadCredentialsExceptionWhenTokenIsInvalid() {
            RefreshRequest request = new RefreshRequest(REFRESH_TOKEN);

            given(jwtService.extractUsername(REFRESH_TOKEN)).willReturn(CPF);
            given(userDetailsService.loadUserByUsername(CPF)).willReturn(userDetails);
            given(jwtService.isValidToken(REFRESH_TOKEN, userDetails, "refresh")).willReturn(false);

            assertThatThrownBy(() -> authService.refresh(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("Invalid refresh token");

            verify(jwtService, never()).generateAccessToken(any());
            verify(jwtService, never()).generateRefreshToken(any());
        }

        @Test
        @DisplayName("should extract CPF from token and load corresponding UserDetails")
        void shouldExtractCpfAndLoadUserDetails() {
            RefreshRequest request = new RefreshRequest(REFRESH_TOKEN);

            given(jwtService.extractUsername(REFRESH_TOKEN)).willReturn(CPF);
            given(userDetailsService.loadUserByUsername(CPF)).willReturn(userDetails);
            given(jwtService.isValidToken(REFRESH_TOKEN, userDetails, "refresh")).willReturn(true);
            given(jwtService.generateAccessToken(userDetails)).willReturn(ACCESS_TOKEN);
            given(jwtService.generateRefreshToken(userDetails)).willReturn(REFRESH_TOKEN);

            authService.refresh(request);

            verify(jwtService, times(1)).extractUsername(REFRESH_TOKEN);
            verify(userDetailsService, times(1)).loadUserByUsername(CPF);
        }

        @Test
        @DisplayName("should throw UsernameNotFound when user is not found")
        void shouldPropagateExceptionWhenUserNotFound() {
            RefreshRequest request = new RefreshRequest(REFRESH_TOKEN);

            given(jwtService.extractUsername(REFRESH_TOKEN)).willReturn(CPF);
            given(userDetailsService.loadUserByUsername(CPF))
                    .willThrow(new UsernameNotFoundException("User not found: " + CPF));

            assertThatThrownBy(() -> authService.refresh(request))
                    .isInstanceOf(UsernameNotFoundException.class);

            verify(jwtService, never()).isValidToken(anyString(), any(), anyString());
        }
    }
}
