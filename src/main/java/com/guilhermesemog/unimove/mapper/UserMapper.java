package com.guilhermesemog.unimove.mapper;

import com.guilhermesemog.unimove.dto.common.CreateUserBody;
import com.guilhermesemog.unimove.dto.user.UserPatchRequestBody;
import com.guilhermesemog.unimove.dto.user.UserPostRequestBody;
import com.guilhermesemog.unimove.dto.user.UserPutRequestBody;
import com.guilhermesemog.unimove.dto.user.UserResponseBody;
import com.guilhermesemog.unimove.model.User;
import com.guilhermesemog.unimove.model.enums.Role;
import org.springframework.stereotype.Component;


@Component
public class UserMapper {

    public User toEntity(UserPostRequestBody userPostRequestBody) {
        CreateUserBody userBody = userPostRequestBody.user();

        Boolean isActive = userBody.active();

        if (isActive == null) {
            isActive = true;
        }

        return new User(
                userBody.cpf(),
                userBody.password(),
                userBody.firstName(),
                userBody.lastName(),
                userBody.phone(),
                isActive,
                userPostRequestBody.role()
        );
    }

    public User toEntity(CreateUserBody userBody, Role role) {

        Boolean isActive = userBody.active();

        if (isActive == null) {
            isActive = true;
        }

        return new User(
                userBody.cpf(),
                userBody.password(),
                userBody.firstName(),
                userBody.lastName(),
                userBody.phone(),
                isActive,
                role
        );
    }

    public UserResponseBody toResponseBody(User user) {
        return new UserResponseBody(
                user.getId(),
                user.getCpf(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getActive(),
                user.getRole()
        );
    }

    public User updateUser(UserPutRequestBody newUser, User user) {
        user.setCpf(newUser.cpf());
        user.setFirstName(newUser.firstName());
        user.setLastName(newUser.lastName());
        user.setPhone(newUser.phone());
        return user;
    }

    public User updateUser(UserPatchRequestBody newUser, User user) {

        if (newUser.cpf() != null) {
            user.setCpf(newUser.cpf());
        }
        if (newUser.firstName() != null) {
            user.setFirstName(newUser.firstName());
        }
        if (newUser.lastName() != null) {
            user.setLastName(newUser.lastName());
        }
        if (newUser.phone() != null) {
            user.setPhone(newUser.phone());
        }

        return user;
    }
}
