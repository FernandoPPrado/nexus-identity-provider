package com.fernando.iop.user.dto;

import java.util.UUID;

public record EmailEventDTO(
        String email,
        UUID projectID,
        String token,
        TipoEvento tipoEventoEnum

) {
    public enum TipoEvento {
        CONFIRMACAO,
        RECUPERACAO
    }
}
