package com.fernando.iop.user.dto;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record UserRecoveryConfirmRequestDTO(
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O formato do e-mail é inválido")
        @Size(min = 11, max = 64, message = "O e-mail deve ter entre 11 e 64 caracteres")
        String email,

        @NotNull(message = "O ID do projeto é obrigatório")
        UUID projectId,

        @NotBlank(message = "A nova senha é obrigatória")
        @Size(min = 6, max = 64, message = "A senha deve ter entre 6 e 64 caracteres")
        String passWord,

        @NotBlank(message = "O token de recuperação é obrigatório")
        @Pattern(regexp = "^\\d{6}$", message = "O token de recuperação deve conter exatamente 6 dígitos numéricos")
        String recoveryToken
) {
}
