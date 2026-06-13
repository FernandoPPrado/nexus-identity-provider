package com.fernando.iop.dto;

import java.util.UUID;

public record AuthRequestDTO(

        String email,
        String password,
        UUID projectId

) {
}
