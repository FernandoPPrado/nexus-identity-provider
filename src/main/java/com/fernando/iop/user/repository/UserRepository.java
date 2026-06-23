package com.fernando.iop.user.repository;

import com.fernando.iop.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {

    public Optional<User> findByUserId(Long id);

    public Optional<User> findByUserEmailAndProject_ProjectId(String email, UUID projectId);

    public boolean existsByUserEmailAndProject_ProjectId(String email, UUID projectId);

    public Optional<User> findByUserEmailAndProject_ProjectIdAndActiveTrue(String email, UUID projectId);


}
