package com.fernando.iop.user.controller;

import com.fernando.iop.security.service.TokenService;
import com.fernando.iop.user.dto.UserConfirmTokenDTO;
import com.fernando.iop.user.dto.UserEntityResponseDTO;
import com.fernando.iop.user.dto.UserRecoveryConfirmRequestDTO;
import com.fernando.iop.user.dto.UserRequestDTO;
import com.fernando.iop.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = {"/user"})
public class UserController {

    private final UserService userService;


    public UserController(TokenService tokenService, UserService userService) {
        this.userService = userService;
    }

    @PostMapping(path = "/recovery-token")
    public ResponseEntity<Void> recoveryUser(@RequestBody UserRequestDTO userRecoveryDTO) {
        userService.generateRecoveryToken(userRecoveryDTO.email(), userRecoveryDTO.projectId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/recovery-validate")
    public ResponseEntity<UserEntityResponseDTO> confirmRecoveryUser(@RequestBody UserRecoveryConfirmRequestDTO userRecoveryConfirmDTO) {
        UserEntityResponseDTO user = userService.recoveryUser(userRecoveryConfirmDTO.email(), userRecoveryConfirmDTO.projectId(), userRecoveryConfirmDTO.passWord(), userRecoveryConfirmDTO.recoveryToken());
        return ResponseEntity.ok().body(user);
    }

    @PostMapping(path = "/confirm-token")
    public ResponseEntity<Void> confirmUser(@RequestBody UserRequestDTO userConfirmDTO) {
        userService.generateConfirmUserCode(userConfirmDTO.email(), userConfirmDTO.projectId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/confirm-validate")
    public ResponseEntity<UserEntityResponseDTO> validadeConfirmUser(@RequestBody UserConfirmTokenDTO userConfirmDTO) {
        UserEntityResponseDTO user = userService.confirmUser(userConfirmDTO.userEmail(), userConfirmDTO.projectId(), userConfirmDTO.confirmToken());
        return ResponseEntity.ok().body(user);
    }

}
