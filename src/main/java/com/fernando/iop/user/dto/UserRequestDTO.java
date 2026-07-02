package com.fernando.iop.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


import java.util.UUID;

public record UserRequestDTO(
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O formato do e-mail é inválido")
        @Size(min = 11, max = 64, message = "O e-mail deve ter entre 11 e 64 caracteres")
        String email,

        @NotNull(message = "O ID do projeto é obrigatório")
        UUID projectId
) {
}
