package com.fernando.iop.security.controller;

import com.fernando.iop.dto.AuthRequestDTO;
import com.fernando.iop.dto.AuthResponseDTO;
import com.fernando.iop.security.service.AuthService;
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
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO authRequestDTO) {

        return ResponseEntity.ok().body(authService.userLogin(authRequestDTO));

    }

}
