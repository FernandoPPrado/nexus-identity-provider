package com.fernando.iop.user.service;

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

    public UserService(UserH2Repository userH2Repository) {
        this.userH2Repository = userH2Repository;
    }


    public UserEntityResponseDTO findUserByEmailAndProjectId(String email, UUID projectId) {
        User user = userH2Repository.findByUserEmailAndProjectId(email, projectId).orElseThrow(() -> new EntityNotFoundException("Entidade não encontrada"));
        return new UserEntityResponseDTO(user.getUserEmail(), user.getUserId(), user.getProjectId(), user.getUserRoles());
    }


    public void createUser(String email, String password, UUID projectId, UserRoles roles) {

        if (userH2Repository.existsByUserEmailAndProjectId(email, projectId)) {
            throw new EntityExistsException();
        }
        userH2Repository.save(new User(email, password, UserRoles.ROLE_USER, projectId));


    }
}
