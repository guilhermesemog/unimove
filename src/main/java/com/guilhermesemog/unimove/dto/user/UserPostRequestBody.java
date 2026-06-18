package com.guilhermesemog.unimove.dto.user;

import com.guilhermesemog.unimove.dto.common.CreateUserBody;
import com.guilhermesemog.unimove.model.enums.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record UserPostRequestBody(

        @Valid CreateUserBody user,
        @NotNull(message = "Role is required") Role role

) {
}
