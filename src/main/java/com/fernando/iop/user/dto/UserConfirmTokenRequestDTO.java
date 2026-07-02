package com.fernando.iop.user.dto;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record UserConfirmTokenRequestDTO(
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O formato do e-mail é inválido")
        @Size(min = 11, max = 64, message = "O e-mail deve ter entre 11 e 64 caracteres")
        String userEmail,

        @NotNull(message = "O ID do projeto é obrigatório")
        UUID projectId,

        @NotBlank(message = "O token de confirmação é obrigatório")
        @Pattern(regexp = "^\\d{6}$", message = "O token de confirmação deve conter exatamente 6 dígitos numéricos")
        String confirmToken
) {
}
