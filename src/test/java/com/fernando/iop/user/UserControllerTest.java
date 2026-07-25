package com.fernando.iop.user;

import com.fernando.iop.exceptions.model.InvalidTokenException;
import com.fernando.iop.exceptions.model.TokenAlreadySentException;
import com.fernando.iop.exceptions.model.UserAlreadyConfirmedException;
import com.fernando.iop.exceptions.model.UserNotFoundException;
import com.fernando.iop.user.controller.UserController;
import com.fernando.iop.user.dto.UserConfirmTokenRequestDTO;
import com.fernando.iop.user.dto.UserEntityResponseDTO;
import com.fernando.iop.user.dto.UserRecoveryConfirmRequestDTO;
import com.fernando.iop.user.dto.UserRequestDTO;
import com.fernando.iop.user.enums.UserRoles;
import com.fernando.iop.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private String email = "emailTeste@gmail.com";
    private String senha = "NovaSenha123";
    private String token = "MeuTokenTeste123";
    private UUID projetoId = UUID.randomUUID();
    private Long userId = Long.parseLong("1");

    private UserRequestDTO userRequestDTO = new UserRequestDTO(email, projetoId);
    private UserRecoveryConfirmRequestDTO userRecoveryConfirmRequestDTO = new UserRecoveryConfirmRequestDTO(email, projetoId, senha, token);
    private UserConfirmTokenRequestDTO userConfirmTokenRequestDTO = new UserConfirmTokenRequestDTO(email, projetoId, token);
    private UserEntityResponseDTO userEntityResponseDTO = new UserEntityResponseDTO(email, userId, projetoId, UserRoles.ROLE_USER);


    @Nested
    @DisplayName("1. Solicitação de Token de Recuperação (/recovery-token)")
    class RecoveryTokenTest {

        @Test
        @DisplayName("Caminho Feliz: Deve gerar token de recuperação e retornar 204")
        public void deveGerarTokenDeRecuperacaoERetornar204() throws Exception {


            doNothing().when(userService).generateRecoveryToken(email, projetoId);

            mockMvc.perform(post("/user/recovery-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userRequestDTO)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Caminho Triste: Deve retornar 400 se já houver token ativo (TokenAlreadySentException)")
        public void deveRetornar400SeTokenDeRecuperacaoJaEnviado() throws Exception {

            doThrow(TokenAlreadySentException.class).when(userService).generateRecoveryToken(email, projetoId);

            mockMvc.perform(post("/user/recovery-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Verifique seu e-mail. Um link de confirmação já foi enviado recentemente."));
        }

        @Test
        @DisplayName("Caminho Triste: Deve retornar 401 se usuário não for encontrado (UserNotFoundException)")
        public void deveRetornar401SeUsuarioNaoExistir() throws Exception {

            doThrow(UserNotFoundException.class).when(userService).generateRecoveryToken(email, projetoId);

            mockMvc.perform(post("/user/recovery-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userRequestDTO)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.message").value("E-mail ou senha incorretos"));
        }
    }

    @Nested
    @DisplayName("2. Validação e Redefinição de Senha (/recovery-validate)")
    class RecoveryValidateTest {

        @Test
        @DisplayName("Caminho Feliz: Deve redefinir senha e retornar 200 com os dados do usuário")
        public void deveRedefinirSenhaERetornarDadosDoUsuario() throws Exception {

            when(userService.recoveryUser(email, projetoId, senha, token)).thenReturn(userEntityResponseDTO);

            mockMvc.perform(post("/user/recovery-validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userRecoveryConfirmRequestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userEmail").value(email))
                    .andExpect(jsonPath("$.projectId").value(projetoId.toString()));
        }

        @Test
        @DisplayName("Caminho Triste: Deve retornar 400 se o token for inválido (InvalidTokenException)")
        public void deveRetornar400SeTokenRecuperacaoInvalido() throws Exception {

            when(userService.recoveryUser(email, projetoId, senha, token)).thenThrow(InvalidTokenException.class);

            mockMvc.perform(post("/user/recovery-validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userRecoveryConfirmRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("O código fornecido é inválido ou já expirou."));
        }
    }

    @Nested
    @DisplayName("3. Solicitação de Token de Confirmação (/confirm-token)")
    class ConfirmTokenTest {

        @Test
        @DisplayName("Caminho Feliz: Deve gerar token de confirmação e retornar 204")
        public void deveGerarTokenDeConfirmacaoERetornar204() throws Exception {

            doNothing().when(userService).generateConfirmUserCode(email, projetoId);

            mockMvc.perform(post("/user/confirm-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userRequestDTO)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Caminho Triste: Deve retornar 409 se a conta já estiver confirmada (UserAlreadyConfirmedException)")
        public void deveRetornar409SeContaJaConfirmada() throws Exception {

            doThrow(UserAlreadyConfirmedException.class).when(userService).generateConfirmUserCode(email, projetoId);

            mockMvc.perform(post("/user/confirm-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userRequestDTO)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.message").value("Esta conta já se encontra ativa e confirmada no sistema."));
        }
    }

    @Nested
    @DisplayName("4. Validação da Confirmação (/confirm-validate)")
    class ConfirmValidateTest {

        @Test
        @DisplayName("Caminho Feliz: Deve validar conta e retornar 200 com os dados do usuário")
        public void deveValidarContaERetornarDadosDoUsuario() throws Exception {

            when(userService.confirmUser(email, projetoId, token)).thenReturn(userEntityResponseDTO);

            mockMvc.perform(post("/user/confirm-validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userConfirmTokenRequestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userEmail").value(email))
                    .andExpect(jsonPath("$.projectId").value(projetoId.toString()));
        }

        @Test
        @DisplayName("Caminho Triste: Deve retornar 400 se o token for inválido (InvalidTokenException)")
        public void deveRetornar400SeTokenConfirmacaoInvalido() throws Exception {

            when(userService.confirmUser(email, projetoId, token)).thenThrow(InvalidTokenException.class);

            mockMvc.perform(post("/user/confirm-validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userConfirmTokenRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("O código fornecido é inválido ou já expirou."));
        }
    }
}