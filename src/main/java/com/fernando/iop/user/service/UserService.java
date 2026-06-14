package com.fernando.iop.user.service;

import com.fernando.iop.project.model.Project;
import com.fernando.iop.project.repository.ProjectRepository;
import com.fernando.iop.user.dto.UserEntityResponseDTO;
import com.fernando.iop.user.enums.UserRoles;
import com.fernando.iop.user.model.User;
import com.fernando.iop.user.repository.UserH2Repository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserH2Repository userH2Repository;
    private final ProjectRepository projectRepository;

    public UserService(UserH2Repository userH2Repository, ProjectRepository projectRepository) {
        this.userH2Repository = userH2Repository;
        this.projectRepository = projectRepository;
    }


    public UserEntityResponseDTO findUserByEmailAndProjectId(String email, Project project) {
        User user = userH2Repository.findByUserEmailAndProjectId(email, project.getProjectId()).orElseThrow(() -> new EntityNotFoundException("Entidade não encontrada"));
        return new UserEntityResponseDTO(user.getUserEmail(), user.getUserId(), user.getProject(), user.getUserRoles());
    }


    public void createUser(String email, String password, Project project, UserRoles roles) {

        if (userH2Repository.existsByUserEmailAndProjectId(email, project.getProjectId()) || !projectRepository.existsByProjectId(project.getProjectId())) {
            throw new EntityExistsException();
        }
        userH2Repository.save(new User(email, password, UserRoles.ROLE_USER, project));


    }
}
