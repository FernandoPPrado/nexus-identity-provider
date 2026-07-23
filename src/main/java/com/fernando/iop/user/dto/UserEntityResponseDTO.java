package com.fernando.iop.user.dto;

import com.fernando.iop.project.model.Project;
import com.fernando.iop.user.enums.UserRoles;

import java.util.UUID;

public record UserEntityResponseDTO(

        String userEmail,
        Long userId,
        UUID projectId,
        UserRoles userRoles

) {
}
