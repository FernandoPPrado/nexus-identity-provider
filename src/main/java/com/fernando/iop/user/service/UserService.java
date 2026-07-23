package com.fernando.iop.user.service;

import com.fernando.iop.exceptions.model.*;
import com.fernando.iop.message.service.RabbitService;
import com.fernando.iop.project.model.Project;
import com.fernando.iop.project.repository.ProjectRepository;
import com.fernando.iop.security.service.TokenService;
import com.fernando.iop.user.dto.EmailEventDTO;
import com.fernando.iop.user.dto.UserEntityResponseDTO;
import com.fernando.iop.user.enums.UserRoles;
import com.fernando.iop.user.model.User;
import com.fernando.iop.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class UserService {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final PasswordEncoder passwordEncoder;

    private final RabbitService rabbitService;

    public UserService(TokenService tokenService, UserRepository userRepository, ProjectRepository projectRepository, PasswordEncoder passwordEncoder, RabbitService rabbitService) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.passwordEncoder = passwordEncoder;
        this.rabbitService = rabbitService;
    }


    public UserEntityResponseDTO findUserByEmailAndProjectIdAndActiveTrueAndConfirmed(String email, UUID project) {

        if (email == null || email.isBlank() || project == null) {
            throw new IllegalArgumentException("Valores nulos não suportados");
        }

        User user = userRepository.findByUserEmailAndProject_ProjectIdAndActiveTrueAndConfirmedTrue(email, project).orElseThrow(() -> new UserNotFoundException("Entidade não encontrada"));
        return new UserEntityResponseDTO(user.getUserEmail(), user.getUserId(), user.getProject().getProjectId(), user.getUserRoles());
    }


    public UserEntityResponseDTO createUser(String email, String password, Project project, UserRoles roles) {

        if (email == null || email.isBlank() || password == null || password.isBlank() || project == null) {
            throw new IllegalArgumentException("Valores nulos não suportados");
        }

        if (!projectRepository.existsByProjectId(project.getProjectId())) {
            throw new ProjectNotFoundException("Projeto não localizado");
        }

        if (userRepository.existsByUserEmailAndProject_ProjectId(email, project.getProjectId())) {
            throw new UserAlreadyExistsException("Usuário já cadastrado neste projeto");
        }

        User user = userRepository.save(new User(email, passwordEncoder.encode(password), UserRoles.ROLE_USER, project));
        return new UserEntityResponseDTO(user.getUserEmail(), user.getUserId(), user.getProject().getProjectId(), user.getUserRoles());
    }

    public void softDeleteUser(String email, UUID projectid, boolean status) {

        if (email == null || email.isBlank() || projectid == null) {
            throw new IllegalArgumentException("Valores nulos não suportados");
        }

        User user = userRepository.findByUserEmailAndProject_ProjectId(email, projectid).orElseThrow(() -> new UserNotFoundException("Usuario nao localizado"));
        user.setActive(status);
        userRepository.save(user);
    }


    @Transactional
    public void generateRecoveryToken(String email, UUID projectId) {

        if (email == null || email.isBlank() || projectId == null) {
            return;
        }

        User user = userRepository.findByUserEmailAndProject_ProjectId(email, projectId).orElseThrow(() -> new UserNotFoundException("Usuario nao localizado"));

        if (user.getRecoveryToken() != null && user.getRecoveryTokenExpiry().isAfter(Instant.now())) {
            throw new TokenAlreadySentException("Pedido de recuperacao ativo");
        }

        String recoveryToken = tokenService.recoveryToken();
        Instant instant = Instant.now().plusSeconds(3600);

        user.setRecoveryToken(recoveryToken);
        user.setRecoveryTokenExpiry(instant);

        User user1 = userRepository.save(user);
        System.out.println(user1.getRecoveryToken());
        rabbitService.dispararEmailEvento(new EmailEventDTO(user1.getUserEmail(), user1.getProject().getProjectId(), user.getRecoveryToken(), EmailEventDTO.TipoEvento.RECUPERACAO));
    }

    @Transactional
    public UserEntityResponseDTO recoveryUser(String email, UUID projectId, String newPassword, String recoveryToken) {

        if (email == null || email.isBlank() || projectId == null || newPassword == null || newPassword.isBlank() || recoveryToken == null || recoveryToken.isBlank()) {
            throw new IllegalArgumentException("Valores nulos não suportados");
        }

        User user = userRepository.findByUserEmailAndProject_ProjectId(email, projectId).orElseThrow(() -> new UserNotFoundException("Usuario nao localizado"));

        if (user.getRecoveryToken() == null || user.getRecoveryTokenExpiry() == null) {
            throw new InvalidTokenException("Nenhum pedido de recuperação ativo para esta conta.");
        }

        if (user.getRecoveryTokenExpiry().isBefore(Instant.now())) {
            throw new InvalidTokenException("Token Expirado");
        }

        if (!user.getRecoveryToken().equals(recoveryToken)) {
            throw new InvalidTokenException("Token Invalido");
        }

        user.setActive(true);
        user.setRecoveryTokenExpiry(null);
        user.setRecoveryToken(null);
        user.setUserPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return new UserEntityResponseDTO(user.getUserEmail(), user.getUserId(), user.getProject().getProjectId(), user.getUserRoles());

    }


    @Transactional
    public void generateConfirmUserCode(String userEmail, UUID projectId) {

        if (userEmail == null || userEmail.isBlank() || projectId == null) {
            throw new IllegalArgumentException("Valores nulos não suportados");
        }

        User user = userRepository.findByUserEmailAndProject_ProjectId(userEmail, projectId).orElseThrow(() -> new UserNotFoundException("Usuario nao localizado"));

        if (user.isConfirmed()) {
            throw new UserAlreadyConfirmedException("Usuário já confirmado");
        }

        if (user.getConfirmToken() != null && user.getConfirmTokenExpiry().isAfter(Instant.now())) {
            throw new TokenAlreadySentException("Pedido de confirmaçao ativo");
        }

        String token = tokenService.recoveryToken();
        Instant confirmTokenExp = Instant.now().plusSeconds(3600);
        user.setConfirmToken(token);
        user.setConfirmTokenExpiry(confirmTokenExp);
        User user1 = userRepository.save(user);

        System.out.println(user1.getRecoveryToken());
        rabbitService.dispararEmailEvento(new EmailEventDTO(user1.getUserEmail(), user1.getProject().getProjectId(), user.getConfirmToken(), EmailEventDTO.TipoEvento.CONFIRMACAO));

        System.out.println("AQUI ENVIAMOS O EVENTO PARA O RABBIT");
        System.out.println(user.getConfirmToken());

    }

    @Transactional
    public UserEntityResponseDTO confirmUser(String userEmail, UUID projectId, String confirmationCode) {

        if (userEmail == null || userEmail.isBlank() || projectId == null || confirmationCode == null || confirmationCode.isBlank()) {
            throw new IllegalArgumentException("Valores nulos não suportados");
        }

        User user = userRepository.findByUserEmailAndProject_ProjectId(userEmail, projectId).orElseThrow(() -> new UserNotFoundException("Usuario nao localizado"));

        if (user.isConfirmed()) {
            throw new UserAlreadyConfirmedException("Usuário já confirmado");
        }

        if (user.getConfirmToken() == null || user.getConfirmTokenExpiry() == null) {
            throw new InvalidTokenException("Nenhum processo de confirmacao em aberto");
        }

        if (!user.getConfirmToken().equals(confirmationCode) || !user.getConfirmTokenExpiry().isAfter(Instant.now())) {
            throw new InvalidTokenException("Token inválido ou expirado");
        }
        user.setConfirmed(true);
        user.setConfirmTokenExpiry(null);
        user.setConfirmToken(null);
        userRepository.save(user);

        return new UserEntityResponseDTO(user.getUserEmail(), user.getUserId(), user.getProject().getProjectId(), user.getUserRoles());

    }


}
