package com.fernando.iop.dto;

public record AuthResponseDTO(
        Long userID,
        String email,
        String jwt
) {
}
