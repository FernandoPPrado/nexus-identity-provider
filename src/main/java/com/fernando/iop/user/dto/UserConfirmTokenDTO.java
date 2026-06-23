package com.fernando.iop.user.dto;

import java.util.UUID;

public record UserConfirmTokenDTO(
        String userEmail,
        UUID projectId,
        String confirmToken
) {
}
