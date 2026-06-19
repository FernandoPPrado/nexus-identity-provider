package com.fernando.iop.user.service;

import com.fernando.iop.project.model.Project;
import com.fernando.iop.project.repository.ProjectRepository;
import com.fernando.iop.security.service.AuthService;
import com.fernando.iop.security.service.TokenService;
import com.fernando.iop.user.dto.UserEntityResponseDTO;
import com.fernando.iop.user.enums.UserRoles;
import com.fernando.iop.user.model.User;
import com.fernando.iop.user.repository.UserH2Repository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final TokenService tokenService;
    private final UserH2Repository userH2Repository;
    private final ProjectRepository projectRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(TokenService tokenService, UserH2Repository userH2Repository, ProjectRepository projectRepository, PasswordEncoder passwordEncoder) {
        this.tokenService = tokenService;
        this.userH2Repository = userH2Repository;
        this.projectRepository = projectRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public UserEntityResponseDTO findUserByEmailAndProjectId(String email, Project project) {
        User user = userH2Repository.findByUserEmailAndProject_ProjectIdAndActiveTrue(email, project.getProjectId()).orElseThrow(() -> new EntityNotFoundException("Entidade não encontrada"));
        return new UserEntityResponseDTO(user.getUserEmail(), user.getUserId(), user.getProject(), user.getUserRoles());
    }


    public void createUser(String email, String password, Project project, UserRoles roles) {

        if (!projectRepository.existsByProjectId(project.getProjectId())) {
            throw new EntityNotFoundException("Projeto não localizado");
        }

        if (userH2Repository.existsByUserEmailAndProject_ProjectId(email, project.getProjectId())) {
            throw new EntityExistsException("Usuário já cadastrado neste projeto");
        }

        userH2Repository.save(new User(email, passwordEncoder.encode(password), UserRoles.ROLE_USER, project));

    }

    public void softDeleteUser(String email, UUID projectid, boolean status) {
        User user = userH2Repository.findByUserEmailAndProject_ProjectId(email, projectid).orElseThrow(() -> new EntityNotFoundException("Usuario nao localizado"));
        user.setActive(status);
        userH2Repository.save(user);
    }


    public void generateRecoveryToken(String email, UUID projectId) {

        User user = userH2Repository.findByUserEmailAndProject_ProjectId(email, projectId).orElseThrow(() -> new EntityNotFoundException("Usuario nao localizado"));

        String recoveryToken = tokenService.recoveryToken();
        Instant instant = Instant.now().plusSeconds(3600);

        user.setRecoveryToken(recoveryToken);
        user.setRecoveryTokenExpirity(instant);

        userH2Repository.save(user);

    }

    public UserEntityResponseDTO recoveryUser(String email, UUID projectId, String newPassword, String recoveryToken) {

        User user = userH2Repository.findByUserEmailAndProject_ProjectId(email, projectId).orElseThrow(() -> new EntityNotFoundException("Usuario nao localizado"));

        if (user.getRecoveryToken() == null || user.getRecoveryTokenExpirity() == null) {
            throw new IllegalArgumentException("Nenhum pedido de recuperação ativo para esta conta.");
        }
        if (user.getRecoveryTokenExpirity().isBefore(Instant.now())) {
            throw new DateTimeException("Token Expirado");
        }

        if (!user.getRecoveryToken().equals(recoveryToken)) {
            throw new IllegalArgumentException("Token Invalido");
        }

        user.setActive(true);
        user.setRecoveryTokenExpirity(null);
        user.setRecoveryToken(null);
        user.setUserPassword(passwordEncoder.encode(newPassword));
        userH2Repository.save(user);
        return new UserEntityResponseDTO(user.getUserEmail(), user.getUserId(), user.getProject(), user.getUserRoles());

    }

}
