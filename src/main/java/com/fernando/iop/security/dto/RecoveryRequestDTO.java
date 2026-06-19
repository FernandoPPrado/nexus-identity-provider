package com.fernando.iop.security.dto;

import java.util.UUID;

public record RecoveryRequestDTO(

        String email,
        String password,
        UUID projectId,
        String recoveryToken

) {
}
