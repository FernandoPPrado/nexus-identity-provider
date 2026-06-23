package com.fernando.iop.user.service;

import com.fernando.iop.project.model.Project;
import com.fernando.iop.project.repository.ProjectRepository;
import com.fernando.iop.security.service.TokenService;
import com.fernando.iop.user.dto.UserEntityResponseDTO;
import com.fernando.iop.user.enums.UserRoles;
import com.fernando.iop.user.model.User;
import com.fernando.iop.user.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.Instant;
import java.util.UUID;

@Service
public class UserService {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(TokenService tokenService, UserRepository userRepository, ProjectRepository projectRepository, PasswordEncoder passwordEncoder) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public UserEntityResponseDTO findUserByEmailAndProjectIdAndActiveTrue(String email, Project project) {

        if (email == null || email.isBlank() || project == null) {
            throw new IllegalArgumentException("Valores nulos não suportados");
        }

        User user = userRepository.findByUserEmailAndProject_ProjectIdAndActiveTrue(email, project.getProjectId()).orElseThrow(() -> new EntityNotFoundException("Entidade não encontrada"));
        return new UserEntityResponseDTO(user.getUserEmail(), user.getUserId(), user.getProject(), user.getUserRoles());
    }


    public UserEntityResponseDTO createUser(String email, String password, Project project, UserRoles roles) {

        if (email == null || email.isBlank() || password == null || password.isBlank() || project == null) {
            throw new IllegalArgumentException("Valores nulos não suportados");
        }

        if (!projectRepository.existsByProjectId(project.getProjectId())) {
            throw new EntityNotFoundException("Projeto não localizado");
        }

        if (userRepository.existsByUserEmailAndProject_ProjectId(email, project.getProjectId())) {
            throw new EntityExistsException("Usuário já cadastrado neste projeto");
        }

        User user = userRepository.save(new User(email, passwordEncoder.encode(password), UserRoles.ROLE_USER, project));
        return new UserEntityResponseDTO(user.getUserEmail(), user.getUserId(), user.getProject(), user.getUserRoles());
    }

    public void softDeleteUser(String email, UUID projectid, boolean status) {

        if (email == null || email.isBlank() || projectid == null) {
            throw new IllegalArgumentException("Valores nulos não suportados");
        }

        User user = userRepository.findByUserEmailAndProject_ProjectId(email, projectid).orElseThrow(() -> new EntityNotFoundException("Usuario nao localizado"));
        user.setActive(status);
        userRepository.save(user);
    }


    @Transactional
    public void generateRecoveryToken(String email, UUID projectId) {

        if (email == null || email.isBlank() || projectId == null) {
            throw new IllegalArgumentException("Valores nulos não suportados");
        }

        User user = userRepository.findByUserEmailAndProject_ProjectId(email, projectId).orElseThrow(() -> new EntityNotFoundException("Usuario nao localizado"));

        String recoveryToken = tokenService.recoveryToken();
        Instant instant = Instant.now().plusSeconds(3600);

        user.setRecoveryToken(recoveryToken);
        user.setRecoveryTokenExpirity(instant);

        userRepository.save(user);
        System.out.println("AQUI ENVIAMOS O EVENTO PARA O RABBIT");
    }

    public UserEntityResponseDTO recoveryUser(String email, UUID projectId, String newPassword, String recoveryToken) {

        if (email == null || email.isBlank() || projectId == null || newPassword == null || newPassword.isBlank() || recoveryToken == null || recoveryToken.isBlank()) {
            throw new IllegalArgumentException("Valores nulos não suportados");
        }

        User user = userRepository.findByUserEmailAndProject_ProjectId(email, projectId).orElseThrow(() -> new EntityNotFoundException("Usuario nao localizado"));

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
        userRepository.save(user);
        return new UserEntityResponseDTO(user.getUserEmail(), user.getUserId(), user.getProject(), user.getUserRoles());

    }


    @Transactional
    public void generateConfirmUserCode(String userEmail, UUID projectId) {

        if (userEmail == null || userEmail.isBlank() || projectId == null) {
            throw new IllegalArgumentException("Valores nulos não suportados");
        }

        User user = userRepository.findByUserEmailAndProject_ProjectId(userEmail, projectId).orElseThrow(() -> new EntityNotFoundException("Usuario nao localizado"));
        if (user.isConfirmed()) {
            //TO-DO Adicionar excessao correta
            throw new IllegalStateException("Usuário já confirmado");
        }
        String token = tokenService.recoveryToken();
        Instant confirmTokenExp = Instant.now().plusSeconds(3600);
        user.setConfirmToken(token);
        user.setConfirmTokenExpiry(confirmTokenExp);
        userRepository.save(user);

        System.out.println("AQUI ENVIAMOS O EVENTO PARA O RABBIT");

    }

    @Transactional
    public UserEntityResponseDTO confirmUser(String userEmail, UUID projectId, String confirmationCode) {

        if (userEmail == null || userEmail.isBlank() || projectId == null || confirmationCode == null || confirmationCode.isBlank()) {
            throw new IllegalArgumentException("Valores nulos não suportados");
        }

        User user = userRepository.findByUserEmailAndProject_ProjectId(userEmail, projectId).orElseThrow(() -> new EntityNotFoundException("Usuario nao localizado"));

        if (user.isConfirmed()) {
            throw new IllegalStateException("Usuário já confirmado");
        }

        if (user.getConfirmToken() == null || user.getConfirmTokenExpiry() == null) {
            throw new IllegalStateException("Nenhum processo de confirmacao em aberto");
        }

        if (!user.getConfirmToken().equals(confirmationCode) || !user.getConfirmTokenExpiry().isAfter(Instant.now())) {
            throw new IllegalStateException("Token inválido ou expirado");
        }
        user.setConfirmed(true);
        user.setConfirmTokenExpiry(null);
        user.setConfirmToken(null);
        userRepository.save(user);

        return new UserEntityResponseDTO(user.getUserEmail(), user.getUserId(), user.getProject(), user.getUserRoles());

    }



}
