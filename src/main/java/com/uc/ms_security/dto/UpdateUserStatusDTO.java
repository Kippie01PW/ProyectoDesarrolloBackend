package com.uc.ms_security.dto;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserStatusDTO {

    @NotNull(
            message = "El estado es obligatorio"
    )
    private Boolean active;
}
