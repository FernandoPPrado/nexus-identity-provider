package com.fernando.iop.security.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AuthRequestDTO(


        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O formato do e-mail é inválido")
        @Size(min = 11, max = 64, message = "O e-mail deve ter entre 11 e 64 caracteres")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, max = 64, message = "A senha deve ter entre 6 e 64 caracteres")
        String password,

        @NotNull(message = "O ID do projeto é obrigatório")
        UUID projectId

) {
}
