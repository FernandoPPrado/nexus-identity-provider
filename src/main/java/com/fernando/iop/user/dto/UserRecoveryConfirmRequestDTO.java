package com.fernando.iop.user.dto;

import java.util.UUID;

public record UserRecoveryConfirmRequestDTO(
        String email,
        UUID projectId,
        String passWord,
        String recoveryToken
) {
}
