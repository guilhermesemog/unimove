package com.guilhermesemog.unimove.dto.user;

import com.guilhermesemog.unimove.dto.common.CommonUserCreate;
import com.guilhermesemog.unimove.model.enums.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record UserCreate(

        @Valid CommonUserCreate user,
        @NotNull(message = "Role is required") Role role

) {
}
