package com.fernando.iop.security;

import com.fernando.iop.exceptions.model.UserNotFoundException;
import com.fernando.iop.security.controller.AuthController;
import com.fernando.iop.security.dto.AuthRequestDTO;
import com.fernando.iop.security.dto.AuthResponseDTO;
import com.fernando.iop.security.service.AuthService;
import com.fernando.iop.user.dto.UserEntityResponseDTO;
import com.fernando.iop.user.enums.UserRoles;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.Mockito.when;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private String email = "emailTeste@gmail.com";
    private String senha = "senha123";
    private String token = "MeuTokenTeste";
    private UUID projeto = UUID.randomUUID();
    private Long userId = Long.parseLong("1");
    private AuthRequestDTO authRequestDTO = new AuthRequestDTO(email, senha, projeto);
    private AuthResponseDTO authResponseDTO = new AuthResponseDTO(userId, email, token);
    private UserEntityResponseDTO userEntityResponseDTO = new UserEntityResponseDTO(email, userId, projeto, UserRoles.ROLE_ADMIN);

    @Nested
    @DisplayName("1. Login de Usuário (userLogin)")
    class AuthLoginTest {

        @Test
        @DisplayName("Caminho Feliz: Deve realizar login e retornar token")
        public void deveRetornarUsuarioComSucessoLogin() throws Exception {
            when(authService.userLogin(authRequestDTO)).thenReturn(authResponseDTO);
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(authRequestDTO)))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.jwt").value(token))
                    .andExpect(jsonPath("$.email").value(email));

        }

        @Test
        @DisplayName("Caminho Triste: Deve retornar 401 e excessao pesonalizada")
        public void deveRetornarErroLoginInvalidoExcessaoPersonalizada() throws Exception {
            when(authService.userLogin(authRequestDTO)).thenThrow(UserNotFoundException.class);
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(authRequestDTO)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("message").value("E-mail ou senha incorretos"));

        }

        @Test
        @DisplayName("Caminho Triste: Deve retornar 500 e excessao generica")
        public void deveRetornarErroLoginEmailESenhaInvalidos() throws Exception {
            when(authService.userLogin(authRequestDTO)).thenThrow(MethodArgumentNotValidException.class);
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(authRequestDTO)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500)).andExpect(jsonPath("message").value("Ocorreu um erro interno inesperado. Tente novamente mais tarde."));
            ;

        }


    }

    @Nested
    @DisplayName("2. Register de Usuário (UserRegister)")
    class AuthRegisterTest {
        @Test
        @DisplayName("Caminho Feliz: Deve registrar usuario e retornar dto")
        public void deveRegistrarUsuarioCorretamenteERetornarOk() throws Exception {

            when(authService.createUser(any(AuthRequestDTO.class))).thenReturn(userEntityResponseDTO);

            mockMvc.perform(post("/auth/register").
                            contentType(MediaType.APPLICATION_JSON).
                            content(objectMapper.writeValueAsString(authRequestDTO)))
                    .andExpect(status().isCreated()).andExpect(jsonPath("userEmail").value(email)).andExpect(jsonPath("userId").value(userId)).andExpect(jsonPath("projectId").value(projeto.toString())).andExpect(jsonPath("userRoles").value(UserRoles.ROLE_ADMIN.toString()));

        }


    }


}

