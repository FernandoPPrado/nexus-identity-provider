package com.fernando.iop.user.dto;

import java.util.UUID;

public record UserRequestDTO(
        String email,
        UUID projectId
) {
}
