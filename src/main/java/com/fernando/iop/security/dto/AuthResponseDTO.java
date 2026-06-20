package com.fernando.iop.security.dto;

public record AuthResponseDTO(
        Long userID,
        String email,
        String jwt
) {
}
