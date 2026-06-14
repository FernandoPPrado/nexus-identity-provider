package com.fernando.iop.security.service;

import com.fernando.iop.user.dto.UserEntityResponseDTO;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;

    public TokenService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generateToken(UserEntityResponseDTO userEntityResponseDTO) {
        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder().issuer("idp").subject(userEntityResponseDTO.userId().toString()).issuedAt(Instant.now()).
                expiresAt(Instant.now().plusSeconds(3600)).
                claim("projectId", userEntityResponseDTO.project().getProjectId()).
                claim("email", userEntityResponseDTO.userEmail()).claim("roles", userEntityResponseDTO.userRoles()).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSet)).getTokenValue();
    }

}
