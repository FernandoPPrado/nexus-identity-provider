package com.fernando.iop.exceptions.customHandler;

import com.fernando.iop.exceptions.dto.CustomExceptionDTO;
import com.fernando.iop.exceptions.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class CustomExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomExceptionHandler.class);

    @ExceptionHandler({UserNotFoundException.class, InvalidCredentialsException.class})
    public ResponseEntity<CustomExceptionDTO> handleUserNotFound(RuntimeException ex) {

        log.warn("Falha de autenticação: {}", ex.getMessage());
        CustomExceptionDTO customExceptionDTO = new CustomExceptionDTO(Instant.now(), HttpStatus.UNAUTHORIZED.value(), "E-mail ou senha incorretos");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(customExceptionDTO);
    }

    @ExceptionHandler({UserAlreadyExistsException.class})
    public ResponseEntity<CustomExceptionDTO> handleUserExists(UserAlreadyExistsException ex) {

        log.warn("Falha criacao usuario: {}", ex.getMessage());

        CustomExceptionDTO customExceptionDTO = new CustomExceptionDTO(Instant.now(), HttpStatus.OK.value(), "Ok");
        return ResponseEntity.status(HttpStatus.OK).body(customExceptionDTO);
    }

    @ExceptionHandler({TokenAlreadySentException.class})
    public ResponseEntity<CustomExceptionDTO> handleTokenExists(TokenAlreadySentException ex) {

        log.warn("Falha Token: {}", ex.getMessage());

        CustomExceptionDTO customExceptionDTO = new CustomExceptionDTO(Instant.now(), HttpStatus.BAD_REQUEST.value(), "Verifique seu e-mail. Um link de confirmação já foi enviado recentemente.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(customExceptionDTO);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<CustomExceptionDTO> handleInvalidToken(InvalidTokenException ex) {

        log.warn("Falha Token: {}", ex.getMessage());

        CustomExceptionDTO customExceptionDTO = new CustomExceptionDTO(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "O código fornecido é inválido ou já expirou."
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(customExceptionDTO);
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<CustomExceptionDTO> handleProjectNotFound(ProjectNotFoundException ex) {

        log.warn("Falha projeto: {}", ex.getMessage());

        CustomExceptionDTO customExceptionDTO = new CustomExceptionDTO(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "Projeto não localizado"
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(customExceptionDTO);
    }

    @ExceptionHandler(UserAlreadyConfirmedException.class)
    public ResponseEntity<CustomExceptionDTO> handleUserAlreadyConfirmed(UserAlreadyConfirmedException ex) {

        log.warn("Falha Confirmacao: {}", ex.getMessage());

        CustomExceptionDTO customExceptionDTO = new CustomExceptionDTO(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Esta conta já se encontra ativa e confirmada no sistema."
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(customExceptionDTO);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomExceptionDTO> handleGenericException(Exception ex) {
        log.error("Erro interno, Motivo {}", ex.getMessage(), ex);
        CustomExceptionDTO customExceptionDTO = new CustomExceptionDTO(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocorreu um erro interno inesperado. Tente novamente mais tarde."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(customExceptionDTO);
    }

}
