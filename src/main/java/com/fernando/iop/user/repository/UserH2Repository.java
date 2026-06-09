package com.fernando.iop.user.repository;

import com.fernando.iop.user.dto.UserEntityResponseDTO;
import com.fernando.iop.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserH2Repository extends JpaRepository<User, Long> {

    public Optional<UserEntityResponseDTO> findByUserEmail(String email);

}
