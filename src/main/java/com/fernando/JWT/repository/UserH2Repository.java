package com.fernando.JWT.repository;

import com.fernando.JWT.dto.UserEntityResponseDTO;
import com.fernando.JWT.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserH2Repository extends JpaRepository<User, Long> {

    public Optional<UserEntityResponseDTO> findByUserEmail(String email);

}
