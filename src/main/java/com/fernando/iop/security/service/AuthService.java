package com.fernando.iop.security.service;

import com.fernando.iop.dto.AuthRequestDTO;
import com.fernando.iop.dto.AuthResponseDTO;
import com.fernando.iop.project.model.Project;
import com.fernando.iop.project.repository.ProjectRepository;
import com.fernando.iop.user.dto.UserEntityResponseDTO;
import com.fernando.iop.user.enums.UserRoles;
import com.fernando.iop.user.model.User;
import com.fernando.iop.user.repository.UserH2Repository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final PasswordEncoder bCrypt;
    private final UserH2Repository userH2Repository;
    private final TokenService tokenService;
    private final ProjectRepository projectRepository;

    public AuthService(PasswordEncoder bCrypt, UserH2Repository userH2Repository, TokenService tokenService, ProjectRepository projectRepository) {
        this.bCrypt = bCrypt;
        this.userH2Repository = userH2Repository;
        this.tokenService = tokenService;
        this.projectRepository = projectRepository;
    }


    public AuthResponseDTO userLogin(AuthRequestDTO authRequestDTO) {

        User user = userH2Repository.findByUserEmailAndProject_ProjectId(authRequestDTO.email(), authRequestDTO.projectId()).orElseThrow(() -> new EntityNotFoundException("Entidade nao localizada"));
        if (!bCrypt.matches(authRequestDTO.password(), user.getUserPassword())) {
            throw new BadCredentialsException("Credenciais incorretas");
        } else {
            return new AuthResponseDTO(user.getUserId(), user.getUserEmail(), tokenService.generateToken(new UserEntityResponseDTO(user.getUserEmail(), user.getUserId(), user.getProject(), user.getUserRoles())));
        }

    }

    public AuthResponseDTO createUser(AuthRequestDTO authRequestDTO) {

        if (userH2Repository.existsByUserEmailAndProject_ProjectId(authRequestDTO.email(), authRequestDTO.projectId())) {
            throw new EntityExistsException("Usuario já cadastrado");
        }

        if (!projectRepository.existsByProjectId(authRequestDTO.projectId())) {
            throw new EntityNotFoundException("Projeto nao localizado");
        }
        User user = new User(authRequestDTO.email(), bCrypt.encode(authRequestDTO.password()), UserRoles.ROLE_USER, new Project(authRequestDTO.projectId()));
        userH2Repository.save(user);
        return new AuthResponseDTO(user.getUserId(), user.getUserEmail(), tokenService.generateToken(new UserEntityResponseDTO(user.getUserEmail(), user.getUserId(), user.getProject(), user.getUserRoles())));

    }

}
