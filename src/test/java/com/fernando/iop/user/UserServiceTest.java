package com.fernando.iop.user;


import com.fernando.iop.exceptions.model.*;
import com.fernando.iop.message.service.RabbitService;
import com.fernando.iop.project.model.Project;
import com.fernando.iop.user.dto.UserEntityResponseDTO;
import com.fernando.iop.user.enums.UserRoles;
import com.fernando.iop.user.model.User;
import com.fernando.iop.user.repository.UserRepository;
import com.fernando.iop.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private RabbitService rabbitService;


    String emailPrincipal = "admin.alpha@iop.com";
    UUID projectIdPrincipal = UUID.fromString("11111111-2222-3333-4444-555555555555");

    @Nested
    @DisplayName("1. Busca de Usuário (findUserByEmailAndProjectId)")
    class FindUserTests {

        @Test
        @DisplayName("Caminho Feliz: Deve retornar usuario correto")
        void loadUserByUsernameAndProjectNameAndActiveTrueDeveRetornarUsuario() {

            UUID uuid = UUID.fromString("11111111-2222-3333-4444-555555555555");

            User user1 = userRepository.findByUserEmailAndProject_ProjectId("admin.alpha@iop.com", uuid).orElseThrow();
            user1.setConfirmed(true);
            userRepository.save(user1);

            UserEntityResponseDTO user = userService.findUserByEmailAndProjectIdAndActiveTrueAndConfirmed("admin.alpha@iop.com", new Project(uuid));
            assertThat(user).isNotNull();
            assertThat(user.userEmail()).isEqualTo("admin.alpha@iop.com");
            assertThat(user.project().getProjectId()).isEqualTo(uuid);

        }

        @Test
        @DisplayName("Caminho Triste: Deve retornar excessão se usuario estiver inativo")
        void loadUserByUsernameAndProjectNameAndActiveTrueRetornaErroUsuarioInativo() {

            UUID uuid = UUID.fromString("11111111-2222-3333-4444-555555555555");
            User user = new User("teste", "TestPassword", UserRoles.ROLE_USER, new Project(uuid));
            user.setActive(false);
            userRepository.save(user);

            assertThatThrownBy(() -> {
                userService.findUserByEmailAndProjectIdAndActiveTrueAndConfirmed(user.getUserEmail(), user.getProject());
            }).isInstanceOf(UserNotFoundException.class);

        }


    }

    @Nested
    @DisplayName("2. Criacao de Usuario (createUser)")
    class CreateUserTests {

        @Test
        @DisplayName("Caminho Feliz: Cria Usuario Corretamente")
        void createUserDevePersistirCorretamente() {

            UUID uuid = UUID.fromString("11111111-2222-3333-4444-555555555555");

            userService.createUser("teste", "TestPassword", new Project(uuid), UserRoles.ROLE_ADMIN);

            User user1 = userRepository.findByUserEmailAndProject_ProjectId("teste", uuid).orElseThrow();

            assertThat(user1).isNotNull();
            assertThat(user1.getUserEmail()).isEqualTo("teste");
            assertThat(user1.getUserRoles()).isEqualTo(UserRoles.ROLE_USER);
            assertThat(user1.getProject().getProjectId()).isEqualTo(uuid);
            assertThat(user1.getUserPassword()).isNotEqualTo("TestPassword");

        }

        @Test
        @DisplayName("Caminho Triste: Projeto Inexistente")
        void createUserDeveLancarExcessaoSeProjetoNaoExistir() {
            UUID uuidInexistente = UUID.fromString("11111111-2222-3333-4444-555555555566");

            assertThatThrownBy(() -> {
                userService.createUser("teste", "TestPassword", new Project(uuidInexistente), UserRoles.ROLE_ADMIN);
            }).isInstanceOf(ProjectNotFoundException.class);

        }

        @Test
        @DisplayName("Caminho Triste: Usuario ja Existente")
        void createUserDeveLancarExcessaoSeUsuarioJaExistir() {
            UUID uuid = UUID.fromString("11111111-2222-3333-4444-555555555555");
            userService.createUser("teste", "TestPassword", new Project(uuid), UserRoles.ROLE_ADMIN);

            assertThatThrownBy(() -> {
                userService.createUser("teste", "TestPassword", new Project(uuid), UserRoles.ROLE_ADMIN);
            }).isInstanceOf(UserAlreadyExistsException.class);

        }

    }

    @Nested
    @DisplayName("3. SoftDelete de Usuario (softDeleteUser)")
    class SoftDeleteUserTests {

        @Test
        @DisplayName("Caminho Feliz: Muda para usuario inativo corretamente")
        void SoftDeleteDeveMudarUsuarioCorretoParaInativo() {
            UUID uuid = UUID.fromString("11111111-2222-3333-4444-555555555555");
            UserEntityResponseDTO user = userService.createUser("teste", "TestPassword", new Project(uuid), UserRoles.ROLE_ADMIN);

            userService.softDeleteUser(user.userEmail(), user.project().getProjectId(), false);

            assertThatThrownBy(() -> {
                userService.findUserByEmailAndProjectIdAndActiveTrueAndConfirmed(user.userEmail(), user.project());
            }).isInstanceOf(UserNotFoundException.class);

            User user1 = userRepository.findByUserEmailAndProject_ProjectId(user.userEmail(), user.project().getProjectId()).orElseThrow();

            assertThat(user1.getUserEmail()).isEqualTo(user.userEmail());
            assertThat(user1.isActive()).isFalse();


        }

        @Test
        @DisplayName("Caminho Triste: Tenta mudar usuario nao cadastrado")
        void SoftDeleteDeveLancarErroUsuarioNaoCadastrado() {

            UUID uuid = UUID.fromString("11111111-2222-3333-4444-555555555555");

            assertThatThrownBy(() -> {
                userService.softDeleteUser("Usuario Inxistente", uuid, false);
            }).isInstanceOf(UserNotFoundException.class);

        }

        @Test
        @DisplayName("Caminho Triste: Tenta mudar usuario projeto Inexistente")
        void SoftDeleteDeveLancarErroProjetoNaoCadastrado() {

            UUID uuid = UUID.fromString("11111111-2222-3333-4444-555555555555");
            UserEntityResponseDTO user = userService.createUser("teste", "TestPassword", new Project(uuid), UserRoles.ROLE_ADMIN);

            Boolean userConf = userRepository.existsByUserEmailAndProject_ProjectId("teste", uuid);

            assertThat(userConf).isTrue();

            assertThatThrownBy(() -> {
                userService.softDeleteUser("teste", UUID.randomUUID(), false);
            }).isInstanceOf(UserNotFoundException.class);

        }

    }

    @Nested
    @DisplayName("4. Generate User Recovery Token (GenerateRecoveryToken)")
    class GenerateRecoveryToken {
        @Test
        @DisplayName("Caminho Feliz: Deve gerar token de recuperação e expiração corretamente")
        void generateRecoveryTokenDeveSalvarTokenETempoNoBanco() {

            UUID uuid = UUID.fromString("11111111-2222-3333-4444-555555555555");
            UserEntityResponseDTO userDTO = userService.createUser("recupera@iop.com", "Senha123", new Project(uuid), UserRoles.ROLE_USER);

            Instant now = Instant.now();

            userService.generateRecoveryToken(userDTO.userEmail(), userDTO.project().getProjectId());

            User userAlterado = userRepository.findByUserEmailAndProject_ProjectId(userDTO.userEmail(), userDTO.project().getProjectId()).orElseThrow();

            assertThat(userAlterado.getRecoveryToken()).isNotNull().isNotBlank().isNotEmpty();
            assertThat(userAlterado.getRecoveryTokenExpiry()).isAfter(now);

        }

        @Test
        @DisplayName("Caminho Triste: Email inexistente")
        void generateRecoveryTokenDeveLancarErroSeEmailNaoExistir() {
            UUID uuidValido = UUID.fromString("11111111-2222-3333-4444-555555555555");

            assertThatThrownBy(() -> {
                userService.generateRecoveryToken("naoexiste@iop.com", uuidValido);
            }).isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("Usuario nao localizado");
        }

        @Test
        @DisplayName("Caminho Triste: Email existe mas em projeto diferente")
        void generateRecoveryTokenDeveLancarErroSeProjetoNaoBaterComUsuario() {
            UUID projetoA = UUID.fromString("11111111-2222-3333-4444-555555555555");
            UUID projetoB = UUID.randomUUID();

            UserEntityResponseDTO user = userService.createUser("user.projetoA@iop.com", "Senha123", new Project(projetoA), UserRoles.ROLE_USER);

            assertThatThrownBy(() -> {
                userService.generateRecoveryToken(user.userEmail(), projetoB);
            }).isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("5. Recuperação de Senha (recoveryUser)")
    class RecoveryUserTests {

        @Test
        @DisplayName("Caminho Feliz: Deve recuperar senha e resetar tokens com sucesso")
        void recoveryUserDeveRecuperarSenhaCorretamente() {
            UUID uuid = UUID.fromString("11111111-2222-3333-4444-555555555555");
            UserEntityResponseDTO userDTO = userService.createUser("recupera@iop.com", "SenhaAntiga", new Project(uuid), UserRoles.ROLE_USER);


            User user = userRepository.findByUserEmailAndProject_ProjectId(userDTO.userEmail(), uuid).orElseThrow();
            user.setRecoveryToken("TOKEN_VALIDO_123");
            user.setRecoveryTokenExpiry(Instant.now().plusSeconds(3600));
            user.setActive(false);
            userRepository.save(user);

            UserEntityResponseDTO response = userService.recoveryUser(userDTO.userEmail(), uuid, "NovaSenhaForte", "TOKEN_VALIDO_123");

            User userRecuperado = userRepository.findByUserEmailAndProject_ProjectId(userDTO.userEmail(), uuid).orElseThrow();

            assertThat(response).isNotNull();
            assertThat(userRecuperado.isActive()).isTrue();
            assertThat(userRecuperado.getRecoveryToken()).isNull();
            assertThat(userRecuperado.getRecoveryTokenExpiry()).isNull();
            assertThat(userRecuperado.getUserPassword()).isNotEqualTo("SenhaAntiga");
        }

        @Test
        @DisplayName("Caminho Triste: Deve lançar erro se já houver pedido de recuperação ativo")
        void generateRecoveryTokenDeveLancarErroSeTokenJaAtivo() {
            UUID uuid = UUID.fromString("11111111-2222-3333-4444-555555555555");
            UserEntityResponseDTO userDTO = userService.createUser("spamrecupera@iop.com", "Senha123", new Project(uuid), UserRoles.ROLE_USER);

            // Simulando que o usuário já pediu o token há 5 minutos e ele ainda é válido
            User user = userRepository.findByUserEmailAndProject_ProjectId(userDTO.userEmail(), uuid).orElseThrow();
            user.setRecoveryToken("TOKEN_JA_ENVIADO");
            user.setRecoveryTokenExpiry(Instant.now().plusSeconds(3600));
            userRepository.save(user);

            // Tentando pedir de novo antes de expirar
            assertThatThrownBy(() -> {
                userService.generateRecoveryToken(userDTO.userEmail(), uuid);
            }).isInstanceOf(TokenAlreadySentException.class)
                    .hasMessageContaining("Pedido de recuperacao ativo");
        }

        @Test
        @DisplayName("Caminho Triste: Deve lançar erro se os parâmetros forem nulos ou vazios")
        void recoveryUserDeveLancarErroParaParametrosNulos() {
            UUID uuid = UUID.randomUUID();

            assertThatThrownBy(() -> {
                userService.recoveryUser(null, uuid, "NovaSenha", "TOKEN");
            }).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Valores nulos não suportados");

            assertThatThrownBy(() -> {
                userService.recoveryUser("email@iop.com", null, "NovaSenha", "TOKEN");
            }).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Valores nulos não suportados");
        }

        @Test
        @DisplayName("Caminho Triste: Deve lançar erro se usuário não existir")
        void recoveryUserDeveLancarErroSeUsuarioNaoExistir() {
            UUID uuid = UUID.randomUUID();

            assertThatThrownBy(() -> {
                userService.recoveryUser("naoexiste@iop.com", uuid, "NovaSenha", "TOKEN");
            }).isInstanceOf(UserNotFoundException.class).hasMessageContaining("Usuario nao localizado");
        }

        @Test
        @DisplayName("Caminho Triste: Deve lançar erro se não houver pedido de recuperação ativo")
        void recoveryUserDeveLancarErroSeNaoHouverTokenSalvo() {
            UUID uuid = UUID.fromString("11111111-2222-3333-4444-555555555555");
            UserEntityResponseDTO userDTO = userService.createUser("semtoken@iop.com", "Senha123", new Project(uuid), UserRoles.ROLE_USER);

            assertThatThrownBy(() -> {
                userService.recoveryUser(userDTO.userEmail(), uuid, "NovaSenha", "TOKEN_QUALQUER");
            }).isInstanceOf(InvalidTokenException.class).hasMessageContaining("Nenhum pedido de recuperação ativo");
        }

        @Test
        @DisplayName("Caminho Triste: Deve lançar erro se o token estiver expirado")
        void recoveryUserDeveLancarErroSeTokenExpirado() {
            UUID uuid = UUID.fromString("11111111-2222-3333-4444-555555555555");
            UserEntityResponseDTO userDTO = userService.createUser("expirado@iop.com", "Senha123", new Project(uuid), UserRoles.ROLE_USER);

            // Preparando o cenário com token expirado (1 hora no passado)
            User user = userRepository.findByUserEmailAndProject_ProjectId(userDTO.userEmail(), uuid).orElseThrow();
            user.setRecoveryToken("TOKEN_VALIDO");
            user.setRecoveryTokenExpiry(Instant.now().minusSeconds(3600));
            userRepository.save(user);

            assertThatThrownBy(() -> {
                userService.recoveryUser(userDTO.userEmail(), uuid, "NovaSenha", "TOKEN_VALIDO");
            }).isInstanceOf(InvalidTokenException.class).hasMessageContaining("Token Expirado");

        }

    }


    @Nested
    @DisplayName("6. Gerar Confirm Token (generateRecoveryToken)")
    class GenerateConfirmUserCode {
        @Test
        @DisplayName("Caminho Feliz: Deve gerar Confirm token e salvar no banco corretamente")
        void deveGerarTokenSalvarNoBancoCorretamente() {

            UUID projectId = UUID.fromString("11111111-2222-3333-4444-555555555555");
            userService.generateConfirmUserCode("admin.alpha@iop.com", projectId);

            User updatedUser = userRepository.findByUserEmailAndProject_ProjectId("admin.alpha@iop.com", projectId).orElseThrow();

            assertThat(updatedUser.getConfirmToken()).isNotNull();
            assertThat(updatedUser.getConfirmTokenExpiry()).isNotNull();
            assertThat(updatedUser.getConfirmTokenExpiry().isAfter(Instant.now())).isTrue();


        }


        @Test
        @DisplayName("Caminho Triste: Deve lançar erro se já houver pedido de confirmação ativo")
        void deveLancarExcecaoSePedidoConfirmacaoJaAtivo() {
            UUID uuid = UUID.fromString("11111111-2222-3333-4444-555555555555");
            UserEntityResponseDTO userDTO = userService.createUser("spamconfirma@iop.com", "Senha123", new Project(uuid), UserRoles.ROLE_USER);

            // Simulando que o sistema já gerou o token de confirmação e enviou o e-mail
            User user = userRepository.findByUserEmailAndProject_ProjectId(userDTO.userEmail(), uuid).orElseThrow();
            user.setConfirmed(false);
            user.setConfirmToken("TOKEN_CONFIRMACAO_ATIVO");
            user.setConfirmTokenExpiry(Instant.now().plusSeconds(3600));
            userRepository.save(user);

            // Tentando clicar no botão de "reenviar código" antes do tempo
            assertThatThrownBy(() -> {
                userService.generateConfirmUserCode(userDTO.userEmail(), uuid);
            }).isInstanceOf(TokenAlreadySentException.class)
                    .hasMessageContaining("Pedido de confirmaçao ativo");
        }

        @Test
        @DisplayName("Caminho Triste: Deve lançar exceção se e-mail ou projeto forem nulos/vazios")
        void deveLancarExcecaoParaValoresNulos() {
            UUID projectId = UUID.fromString("11111111-2222-3333-4444-555555555555");
            assertThatThrownBy(() -> userService.generateConfirmUserCode(null, projectId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Valores nulos não suportados");


            assertThatThrownBy(() -> userService.generateConfirmUserCode(emailPrincipal, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Valores nulos não suportados");
        }

        @Test
        @DisplayName("Caminho Triste: Deve lançar UserAlreadyConfirmedException se usuário já estiver confirmado")
        void deveLancarExcecaoSeUsuarioJaConfirmado() {
            User user = userRepository.findByUserEmailAndProject_ProjectId(emailPrincipal, projectIdPrincipal).orElseThrow();
            user.setConfirmed(true);
            userRepository.save(user);


            assertThatThrownBy(() -> userService.generateConfirmUserCode(emailPrincipal, projectIdPrincipal))
                    .isInstanceOf(UserAlreadyConfirmedException.class)
                    .hasMessage("Usuário já confirmado");
        }
    }

    @Nested
    @DisplayName("2. Confirmar Usuário (confirmUser)")
    class ConfirmUser {

        @Test
        @DisplayName("Caminho Feliz: Deve confirmar usuário e limpar tokens com sucesso")
        void deveConfirmarUsuarioComSucesso() {
            User user = userRepository.findByUserEmailAndProject_ProjectId(emailPrincipal, projectIdPrincipal).orElseThrow();
            user.setConfirmed(false);
            user.setConfirmToken("token-valido-123");
            user.setConfirmTokenExpiry(Instant.now().plusSeconds(3600));
            userRepository.save(user);

            UserEntityResponseDTO response = userService.confirmUser(emailPrincipal, projectIdPrincipal, "token-valido-123");

            User updatedUser = userRepository.findByUserEmailAndProject_ProjectId(emailPrincipal, projectIdPrincipal).orElseThrow();

            assertThat(updatedUser.isConfirmed()).isTrue();
            assertThat(updatedUser.getConfirmToken()).isNull();
            assertThat(updatedUser.getConfirmTokenExpiry()).isNull();

            assertThat(response).isNotNull();
            assertThat(response.userEmail()).isEqualTo(emailPrincipal);
        }

        @Test
        @DisplayName("Caminho Triste: Deve lançar exceção se não houver processo de confirmação em aberto")
        void deveLancarExcecaoSeNenhumProcessoAberto() {

            User user = userRepository.findByUserEmailAndProject_ProjectId(emailPrincipal, projectIdPrincipal).orElseThrow();
            user.setConfirmed(false);
            user.setConfirmToken(null);
            user.setConfirmTokenExpiry(null);
            userRepository.save(user);

            assertThatThrownBy(() -> userService.confirmUser(emailPrincipal, projectIdPrincipal, "qualquer-token"))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessage("Nenhum processo de confirmacao em aberto");
        }

        @Test
        @DisplayName("Caminho Triste: Deve lançar exceção se token for inválido ou estiver expirado")
        void deveLancarExcecaoSeTokenInvalidoOuExpirado() {

            User user = userRepository.findByUserEmailAndProject_ProjectId(emailPrincipal, projectIdPrincipal).orElseThrow();
            user.setConfirmed(false);
            user.setConfirmToken("token-expirado-123");
            user.setConfirmTokenExpiry(Instant.now().minusSeconds(10)); // Expirado
            userRepository.save(user);

            assertThatThrownBy(() -> userService.confirmUser(emailPrincipal, projectIdPrincipal, "token-expirado-123"))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessage("Token inválido ou expirado");

            assertThatThrownBy(() -> userService.confirmUser(emailPrincipal, projectIdPrincipal, "TOKEN-COMPLETAMENTE-ERRADO"))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessage("Token inválido ou expirado");
        }
    }


}
