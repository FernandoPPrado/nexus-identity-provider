package com.fernando.iop.exceptions.dto;

import java.time.Instant;

public record CustomExceptionDTO(

        Instant timestamp,
        Integer status,
        String message

) {
}
