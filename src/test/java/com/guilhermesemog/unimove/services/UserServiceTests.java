package com.guilhermesemog.unimove.services;

import com.guilhermesemog.unimove.dto.common.CommonUserCreate;
import com.guilhermesemog.unimove.dto.user.UserCreate;
import com.guilhermesemog.unimove.dto.user.UserPatch;
import com.guilhermesemog.unimove.dto.user.UserResponse;
import com.guilhermesemog.unimove.dto.user.UserUpdate;
import com.guilhermesemog.unimove.mapper.UserMapper;
import com.guilhermesemog.unimove.model.User;
import com.guilhermesemog.unimove.model.enums.Role;
import com.guilhermesemog.unimove.repository.UserRepository;
import com.guilhermesemog.unimove.service.AuthService;
import com.guilhermesemog.unimove.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
public class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuthService authService;

    @Mock
    private UserCreate userCreate;

    @Mock
    private CommonUserCreate commonUserCreate;

    @Mock
    private UserUpdate userUpdate;

    @Mock
    private UserPatch userPatch;

    @Mock
    private UserResponse userResponse;

    private UserService userService;

    private static final Long USER_ID = 1L;
    private static final String CPF = "12345678900";
    private static final String PASSWORD = "password123";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String PHONE = "11999999999";

    @BeforeEach
    void setup() {
        userService = new UserService(userRepository, userMapper, authService);
    }

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("should create a new user and return UserResponse")
        void shouldCreateUserAndReturnUserResponse() {
            User createdUser = new User(CPF, PASSWORD, Role.ADMIN);
            User savedUser = new User(CPF, PASSWORD, Role.ADMIN);

            given(userCreate.user()).willReturn(commonUserCreate);
            given(userCreate.user().cpf()).willReturn(CPF);
            given(userCreate.user().password()).willReturn(PASSWORD);
            given(userCreate.user().firstName()).willReturn(FIRST_NAME);
            given(userCreate.user().lastName()).willReturn(LAST_NAME);
            given(userCreate.user().phone()).willReturn(PHONE);
            given(userCreate.role()).willReturn(Role.ADMIN);

            given(authService.createAuthenticatableUser(CPF, PASSWORD, Role.ADMIN)).willReturn(createdUser);
            given(userRepository.save(createdUser)).willReturn(savedUser);
            given(userMapper.toResponse(savedUser)).willReturn(userResponse);

            UserResponse response = userService.create(userCreate);

            assertThat(response).isEqualTo(userResponse);
        }

        @Test
        @DisplayName("should set first name, last name and phone when creating user")
        void shouldSetFirstNameLastNameAndPhoneWhenCreatingUser() {
            User createdUser = new User(CPF, PASSWORD, Role.ADMIN);

            given(userCreate.user()).willReturn(commonUserCreate);
            given(userCreate.role()).willReturn(Role.ADMIN);

            given(commonUserCreate.cpf()).willReturn(CPF);
            given(commonUserCreate.password()).willReturn(PASSWORD);
            given(commonUserCreate.firstName()).willReturn(FIRST_NAME);
            given(commonUserCreate.lastName()).willReturn(LAST_NAME);
            given(commonUserCreate.phone()).willReturn(PHONE);

            given(authService.createAuthenticatableUser(CPF, PASSWORD, Role.ADMIN)).willReturn(createdUser);
            given(userRepository.save(any())).willReturn(createdUser);
            given(userMapper.toResponse(any())).willReturn(userResponse);

            userService.create(userCreate);

            assertThat(createdUser.getFirstName()).isEqualTo(FIRST_NAME);
            assertThat(createdUser.getLastName()).isEqualTo(LAST_NAME);
            assertThat(createdUser.getPhone()).isEqualTo(PHONE);
        }

        @Test
        @DisplayName("should propagate exception when cpf already exists")
        void shouldPropagateExceptionWhenCpfAlreadyExists() {
            given(userCreate.user()).willReturn(commonUserCreate);
            given(userCreate.role()).willReturn(Role.ADMIN);
            given(commonUserCreate.cpf()).willReturn(CPF);
            given(commonUserCreate.password()).willReturn(PASSWORD);
            given(authService.createAuthenticatableUser(CPF, PASSWORD, Role.ADMIN))
                    .willThrow(new RuntimeException("CPF already exists: " + CPF));

            assertThatThrownBy(() -> userService.create(userCreate))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining(CPF);

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("should call user repository save with the created user")
        void shouldCallUserRepositorySaveWithCreatedUser() {
            User createdUser = new User(CPF, PASSWORD, Role.ADMIN);

            given(userCreate.user()).willReturn(commonUserCreate);
            given(userCreate.role()).willReturn(Role.ADMIN);
            given(commonUserCreate.cpf()).willReturn(CPF);
            given(commonUserCreate.password()).willReturn(PASSWORD);
            given(commonUserCreate.firstName()).willReturn(FIRST_NAME);
            given(commonUserCreate.lastName()).willReturn(LAST_NAME);
            given(commonUserCreate.phone()).willReturn(PHONE);
            given(authService.createAuthenticatableUser(CPF, PASSWORD, Role.ADMIN)).willReturn(createdUser);
            given(userRepository.save(any(User.class))).willReturn(createdUser);
            given(userMapper.toResponse(any(User.class))).willReturn(userResponse);

            userService.create(userCreate);

            verify(userRepository, times(1)).save(createdUser);
        }
    }
}
