package com.guilhermesemog.unimove.mapper;

import com.guilhermesemog.unimove.dto.common.CommonUserCreate;
import com.guilhermesemog.unimove.dto.user.UserPatch;
import com.guilhermesemog.unimove.dto.user.UserCreate;
import com.guilhermesemog.unimove.dto.user.UserUpdate;
import com.guilhermesemog.unimove.dto.user.UserResponse;
import com.guilhermesemog.unimove.model.User;
import com.guilhermesemog.unimove.model.enums.Role;
import org.springframework.stereotype.Component;


@Component
public class UserMapper {

    public User toEntity(UserCreate body) {
        CommonUserCreate userBody = body.user();
        Boolean isActive = userBody.active();

        isActive = isActive == null || isActive;

        return new User(
                userBody.cpf(),
                userBody.password(),
                userBody.firstName(),
                userBody.lastName(),
                userBody.phone(),
                isActive,
                body.role()
        );
    }

    public User toEntity(CommonUserCreate userBody, Role role) {

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

    public UserResponse toResponseBody(User user) {
        return new UserResponse(
                user.getId(),
                user.getCpf(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getActive(),
                user.getRole()
        );
    }

    public User updateUser(UserUpdate newUser, User user) {
        user.setCpf(newUser.cpf());
        user.setFirstName(newUser.firstName());
        user.setLastName(newUser.lastName());
        user.setPhone(newUser.phone());
        return user;
    }

    public User updateUser(UserPatch newUser, User user) {

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
