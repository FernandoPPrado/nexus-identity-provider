package com.fernando.iop.security.controller;

import com.fernando.iop.security.dto.AuthRequestDTO;
import com.fernando.iop.security.dto.AuthResponseDTO;
import com.fernando.iop.security.service.AuthService;
import com.fernando.iop.user.dto.UserEntityResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/auth")
public class AuthController {

    private final AuthService authService;


    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(path = "/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO authRequestDTO) {

        return ResponseEntity.ok().body(authService.userLogin(authRequestDTO));

    }

    @PostMapping(path = "/register")
    public ResponseEntity<UserEntityResponseDTO> createAccount(@Valid @RequestBody AuthRequestDTO authRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createUser(authRequestDTO));
    }


}
