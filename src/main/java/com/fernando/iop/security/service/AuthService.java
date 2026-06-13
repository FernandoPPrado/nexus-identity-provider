package com.fernando.iop.security.service;

import com.fernando.iop.dto.AuthRequestDTO;
import com.fernando.iop.user.dto.UserEntityResponseDTO;
import com.fernando.iop.user.enums.UserRoles;
import com.fernando.iop.user.model.User;
import com.fernando.iop.user.repository.UserH2Repository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final BCryptPasswordEncoder bCrypt;
    private final UserH2Repository userH2Repository;
    private final TokenService tokenService;

    public AuthService(BCryptPasswordEncoder bCrypt, UserH2Repository userH2Repository, TokenService tokenService) {
        this.bCrypt = bCrypt;
        this.userH2Repository = userH2Repository;
        this.tokenService = tokenService;
    }


    public String userLogin(AuthRequestDTO authRequestDTO) {

        User user = userH2Repository.findByUserEmailAndProjectId(authRequestDTO.email(), authRequestDTO.projectId()).orElseThrow(() -> new EntityNotFoundException("Entidade nao localizada"));

        if (!bCrypt.matches(authRequestDTO.password(), user.getUserPassword())) {
            throw new BadCredentialsException("Credenciais incorretas");
        } else {
            return tokenService.generateToken(new UserEntityResponseDTO(user.getUserEmail(), user.getUserId(), user.getProjectId(), UserRoles.ROLE_USER));
        }

    }

    public String createUser(AuthRequestDTO authRequestDTO) {

        boolean exists = userH2Repository.existsByUserEmailAndProjectId(authRequestDTO.email(), authRequestDTO.projectId());
        if (exists) {
            throw new EntityExistsException();
        } else {
            User user = new User(authRequestDTO.email(), bCrypt.encode(authRequestDTO.password()), UserRoles.ROLE_USER, authRequestDTO.projectId());
            userH2Repository.save(user);
            return tokenService.generateToken(new UserEntityResponseDTO(user.getUserEmail(), user.getUserId(), user.getProjectId(), user.getUserRoles()));
        }


    }

}
