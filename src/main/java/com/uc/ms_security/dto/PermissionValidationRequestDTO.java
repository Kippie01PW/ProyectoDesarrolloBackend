package com.uc.ms_security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionValidationRequestDTO {

    @NotBlank(message = "La url es obligatoria")
    private String url;

    @NotBlank(message = "El método es obligatorio")
    private String method;
}
