package com.fernando.iop.user.dto;

import com.fernando.iop.user.enums.UserRoles;

import java.util.UUID;

public record UserEntityResponseDTO(

        Long userEmail,
        String userId,
        UUID projectId,
        UserRoles userRoles

) {
}
