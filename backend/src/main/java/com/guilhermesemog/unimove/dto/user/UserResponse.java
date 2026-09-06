package com.guilhermesemog.unimove.dto.user;

import java.util.UUID;
import com.guilhermesemog.unimove.model.enums.Role;

public record UserResponse(

        UUID id,
        String cpf,
        String firstName,
        String lastName,
        String phone,
        Boolean active,
        Role role

) {
}
