package com.guilhermesemog.unimove.dto.user;

import com.guilhermesemog.unimove.model.enums.Role;

public record UserResponse(

        Long id,
        String cpf,
        String firstName,
        String lastName,
        String phone,
        Boolean active,
        Role role

) {
}
