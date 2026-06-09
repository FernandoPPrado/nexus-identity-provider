package com.fernando.JWT.dto;

import java.util.UUID;

public record UserEntityResponseDTO(

        Long userEmail,
        String userId,
        UUID projectId

) {
}
