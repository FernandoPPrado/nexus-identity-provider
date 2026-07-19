package com.fernando.iop.security;

import com.fernando.iop.exceptions.model.InvalidCredentialsException;
import com.fernando.iop.exceptions.model.ProjectNotFoundException;
import com.fernando.iop.exceptions.model.UserAlreadyExistsException;
import com.fernando.iop.exceptions.model.UserNotFoundException;
import com.fernando.iop.message.service.RabbitService;
import com.fernando.iop.project.model.Project;
import com.fernando.iop.project.repository.ProjectRepository;
import com.fernando.iop.security.dto.AuthRequestDTO;
import com.fernando.iop.security.dto.AuthResponseDTO;
import com.fernando.iop.security.service.AuthService;
import com.fernando.iop.user.dto.UserEntityResponseDTO;
import com.fernando.iop.user.enums.UserRoles;
import com.fernando.iop.user.model.User;
import com.fernando.iop.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthServiceTest {

    @Autowired
    AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Nested
    @DisplayName("1. Login de Usuário (userLogin)")
    class UserLoginTests {

        @Test
        @DisplayName("Caminho Feliz: Deve realizar login e retornar token")
        void userLoginDeveRetornarTokenComCredenciaisCorretas() {
            UUID projectId = UUID.fromString("11111111-2222-3333-4444-555555555555");

            User user = new User("login@iop.com", passwordEncoder.encode("SenhaForte123"), UserRoles.ROLE_USER, new Project(projectId));
            user.setActive(true);
            user.setConfirmed(true);
            userRepository.save(user);

            AuthRequestDTO request = new AuthRequestDTO("login@iop.com", "SenhaForte123", projectId);

            AuthResponseDTO response = authService.userLogin(request);

            assertThat(response).isNotNull();
            assertThat(response.email()).isEqualTo("login@iop.com");
            assertThat(response.jwt()).isNotBlank();
        }

        @Test
        @DisplayName("Caminho Triste: Deve lançar erro se o usuário não existir ou estiver inativo")
        void userLoginDeveLancarErroSeUsuarioInexistente() {
            UUID projectId = UUID.fromString("11111111-2222-3333-4444-555555555555");
            AuthRequestDTO request = new AuthRequestDTO("naoexiste@iop.com", "Senha123", projectId);

            assertThatThrownBy(() -> {
                authService.userLogin(request);
            }).isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("Caminho Triste: Deve lançar erro se a senha estiver incorreta")
        void userLoginDeveLancarErroSeSenhaIncorreta() {
            UUID projectId = UUID.fromString("11111111-2222-3333-4444-555555555555");

            User user = new User("senhaerrada@iop.com", passwordEncoder.encode("SenhaCorreta123"), UserRoles.ROLE_USER, new Project(projectId));
            user.setActive(true);
            user.setConfirmed(true);
            userRepository.save(user);

            AuthRequestDTO request = new AuthRequestDTO("senhaerrada@iop.com", "SenhaIncorreta", projectId);

            assertThatThrownBy(() -> {
                authService.userLogin(request);
            }).isInstanceOf(InvalidCredentialsException.class);
        }
    }

    @Nested
    @DisplayName("2. Criação de Usuário no Auth (createUser)")
    class CreateUserTests {

        @MockitoBean
        private RabbitService rabbitService;

        @Test
        @DisplayName("Caminho Feliz: Deve criar usuário com sucesso e sem autenticar direto")
        void createUserDeveSalvarNoBancoERetornarDadosDoUsuario() {

            UUID projectId = UUID.fromString("11111111-2222-3333-4444-555555555555");
            AuthRequestDTO request = new AuthRequestDTO("novo@iop.com", "Senha123", projectId);

            UserEntityResponseDTO response = authService.createUser(request);

            assertThat(response).isNotNull();
            assertThat(response.userEmail()).isEqualTo("novo@iop.com");
            assertThat(response.userId()).isNotNull();

            User savedUser = userRepository.findByUserEmailAndProject_ProjectId("novo@iop.com", projectId).orElseThrow();
            assertThat(savedUser).isNotNull();
            assertThat(savedUser.getUserRoles()).isEqualTo(UserRoles.ROLE_USER);

            assertThat(savedUser.isConfirmed()).isFalse();
        }

        @Test
        @DisplayName("Caminho Triste: Deve lançar erro se usuário já existir no projeto")
        void createUserDeveLancarErroSeUsuarioJaExistir() {
            UUID projectId = UUID.fromString("11111111-2222-3333-4444-555555555555");

            User user = new User("existente@iop.com", passwordEncoder.encode("Senha123"), UserRoles.ROLE_USER, new Project(projectId));
            userRepository.save(user);

            AuthRequestDTO request = new AuthRequestDTO("existente@iop.com", "Senha123", projectId);

            assertThatThrownBy(() -> {
                authService.createUser(request);
            }).isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessageContaining("Usuario já cadastrado");
        }

        @Test
        @DisplayName("Caminho Triste: Deve lançar erro se projeto não existir")
        void createUserDeveLancarErroSeProjetoNaoExistir() {
            UUID projectIdInexistente = UUID.randomUUID();
            AuthRequestDTO request = new AuthRequestDTO("projetoinexistente@iop.com", "Senha123", projectIdInexistente);

            assertThatThrownBy(() -> {
                authService.createUser(request);
            }).isInstanceOf(ProjectNotFoundException.class)
                    .hasMessageContaining("Projeto nao localizado");
        }
    }


}
