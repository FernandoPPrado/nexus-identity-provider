package com.fernando.iop.security.service;

import com.fernando.iop.user.dto.UserEntityResponseDTO;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Map;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;

    @Value("${rsa.public-key}")
    private RSAPublicKey rsaPublicKey;

    public TokenService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generateToken(UserEntityResponseDTO userEntityResponseDTO) {
        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder().issuer("idp").subject(userEntityResponseDTO.userId().toString()).issuedAt(Instant.now()).
                expiresAt(Instant.now().plusSeconds(3600)).
                claim("projectId", userEntityResponseDTO.projectId()).
                claim("email", userEntityResponseDTO.userEmail()).claim("roles", userEntityResponseDTO.userRoles()).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSet)).getTokenValue();
    }

    public String recoveryToken() {
        Long recoveryToken = new SecureRandom().nextLong(0, 1000000);
        return String.format("%06d", recoveryToken);
    }

    public Map<String, Object> publicKey() {

        RSAKey jwk = new RSAKey.Builder(rsaPublicKey)
                .keyID("chave-api-1")
                .build();
        JWKSet jwkSet = new JWKSet(jwk);
        return jwkSet.toJSONObject();


    }

}
