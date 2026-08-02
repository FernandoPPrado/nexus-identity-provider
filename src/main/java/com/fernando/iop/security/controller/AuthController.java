package com.fernando.iop.security.controller;

import com.fernando.iop.security.dto.AuthRequestDTO;
import com.fernando.iop.security.dto.AuthResponseDTO;
import com.fernando.iop.security.service.AuthService;
import com.fernando.iop.security.service.TokenService;
import com.fernando.iop.user.dto.UserEntityResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(path = "/auth")
public class AuthController {

    private final AuthService authService;

    public final TokenService tokenService;

    public AuthController(AuthService authService, TokenService tokenService) {
        this.authService = authService;
        this.tokenService = tokenService;
    }

    @PostMapping(path = "/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO authRequestDTO) {

        return ResponseEntity.ok().body(authService.userLogin(authRequestDTO));

    }

    @PostMapping(path = "/register")
    public ResponseEntity<UserEntityResponseDTO> createAccount(@Valid @RequestBody AuthRequestDTO authRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createUser(authRequestDTO));
    }

    @PreAuthorize("permitAll()")
    @GetMapping(path = "/.well-known/jwks.json")
    public ResponseEntity<Map<String, Object>> returnPublicKey() {
        return ResponseEntity.ok().body(tokenService.publicKey());
    }


}
