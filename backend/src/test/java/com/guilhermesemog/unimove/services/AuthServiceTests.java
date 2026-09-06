package com.guilhermesemog.unimove.services;

import com.guilhermesemog.unimove.dto.auth.LoginRequest;
import com.guilhermesemog.unimove.dto.auth.LoginResponse;
import com.guilhermesemog.unimove.dto.auth.RefreshRequest;
import com.guilhermesemog.unimove.dto.common.CommonUserCreate;
import com.guilhermesemog.unimove.exception.type.CpfAlreadyExistsException;
import com.guilhermesemog.unimove.model.User;
import com.guilhermesemog.unimove.model.enums.Role;
import com.guilhermesemog.unimove.repository.UserRepository;
import com.guilhermesemog.unimove.security.JwtService;
import com.guilhermesemog.unimove.service.AuthService;
import com.guilhermesemog.unimove.service.AdminBootstrapGuard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

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

    @Mock
    private AdminBootstrapGuard adminBootstrapGuard;

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
                userDetailsService,
                adminBootstrapGuard
        );
    }

    @Nested
    @DisplayName("administrator bootstrap")
    class AdministratorBootstrapTests {

        @Test
        @DisplayName("should claim the one-time bootstrap before creating the first administrator")
        void shouldClaimBootstrapBeforeRegisteringAdministrator() {
            CommonUserCreate request = new CommonUserCreate(
                    CPF, "First", "Admin", RAW_PASSWORD, "11999999999", true);
            given(userRepository.existsByCpf(CPF)).willReturn(false);
            given(passwordEncoder.encode(RAW_PASSWORD)).willReturn(ENCODED_PASSWORD);
            given(userDetailsService.loadUserByUsername(CPF)).willReturn(userDetails);
            given(jwtService.generateAccessToken(userDetails)).willReturn(ACCESS_TOKEN);
            given(jwtService.generateRefreshToken(userDetails)).willReturn(REFRESH_TOKEN);

            LoginResponse response = authService.register(request);

            InOrder order = inOrder(adminBootstrapGuard, userRepository);
            order.verify(adminBootstrapGuard).claim();
            order.verify(userRepository).existsByCpf(CPF);
            verify(userRepository).save(argThat(user -> user.getRole() == Role.ADMIN));
            assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
        }

        @Test
        @DisplayName("should not create another administrator after bootstrap is claimed")
        void shouldRejectRegistrationAfterBootstrap() {
            CommonUserCreate request = new CommonUserCreate(
                    CPF, "Another", "Admin", RAW_PASSWORD, "11999999999", true);
            doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,
                    "Administrator bootstrap registration is no longer available"))
                    .when(adminBootstrapGuard).claim();

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(ResponseStatusException.class);
            verify(userRepository, never()).save(any());
        }
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

            LoginResponse response = authService.refresh(request.refreshToken());

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

            assertThatThrownBy(() -> authService.refresh(request.refreshToken()))
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

            authService.refresh(request.refreshToken());

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

            assertThatThrownBy(() -> authService.refresh(request.refreshToken()))
                    .isInstanceOf(UsernameNotFoundException.class);

            verify(jwtService, never()).isValidToken(anyString(), any(), anyString());
        }
    }
}
