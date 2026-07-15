package com.fernando.iop.security.service;

import com.fernando.iop.exceptions.InvalidCredentialsException;
import com.fernando.iop.exceptions.ProjectNotFoundException;
import com.fernando.iop.exceptions.UserAlreadyExistsException;
import com.fernando.iop.exceptions.UserNotFoundException;
import com.fernando.iop.security.dto.AuthRequestDTO;
import com.fernando.iop.security.dto.AuthResponseDTO;
import com.fernando.iop.project.model.Project;
import com.fernando.iop.project.repository.ProjectRepository;
import com.fernando.iop.user.dto.UserEntityResponseDTO;
import com.fernando.iop.user.enums.UserRoles;
import com.fernando.iop.user.model.User;
import com.fernando.iop.user.repository.UserRepository;
import com.fernando.iop.user.service.UserService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final PasswordEncoder bCrypt;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final ProjectRepository projectRepository;
    private final UserService userService;


    public AuthService(PasswordEncoder bCrypt, UserRepository userRepository, TokenService tokenService, ProjectRepository projectRepository, UserService userService) {
        this.bCrypt = bCrypt;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.projectRepository = projectRepository;
        this.userService = userService;
    }


    public AuthResponseDTO userLogin(AuthRequestDTO authRequestDTO) {

        User user = userRepository.findByUserEmailAndProject_ProjectIdAndActiveTrueAndConfirmedTrue(authRequestDTO.email(), authRequestDTO.projectId()).orElseThrow(()
                -> new UserNotFoundException("Usuario nao localizado"));
        if (!bCrypt.matches(authRequestDTO.password(), user.getUserPassword())) {
            throw new InvalidCredentialsException("Senha Incorreta");
        } else {
            return new AuthResponseDTO(user.getUserId(), user.getUserEmail(), tokenService.generateToken(new UserEntityResponseDTO(user.getUserEmail(), user.getUserId(), user.getProject(), user.getUserRoles())));
        }

    }

    public UserEntityResponseDTO createUser(AuthRequestDTO authRequestDTO) {

        if (userRepository.existsByUserEmailAndProject_ProjectId(authRequestDTO.email(), authRequestDTO.projectId())) {
            throw new UserAlreadyExistsException("Usuario já cadastrado");
        }

        if (!projectRepository.existsByProjectId(authRequestDTO.projectId())) {
            throw new ProjectNotFoundException("Projeto nao localizado");
        }

        return userService.createUser(authRequestDTO.email(), authRequestDTO.password(), new Project(authRequestDTO.projectId()), UserRoles.ROLE_USER);


    }


}
