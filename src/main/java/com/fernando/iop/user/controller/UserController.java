package com.fernando.iop.user.controller;

import com.fernando.iop.user.dto.*;
import com.fernando.iop.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = {"/user"})
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
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
    public ResponseEntity<UserEntityResponseDTO> validadeConfirmUser(@RequestBody UserConfirmTokenRequestDTO userConfirmDTO) {
        UserEntityResponseDTO user = userService.confirmUser(userConfirmDTO.userEmail(), userConfirmDTO.projectId(), userConfirmDTO.confirmToken());
        return ResponseEntity.ok().body(user);
    }

}
