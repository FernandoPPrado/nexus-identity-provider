package com.fernando.iop.user;


import com.fernando.iop.project.model.Project;
import com.fernando.iop.user.dto.UserEntityResponseDTO;
import com.fernando.iop.user.enums.UserRoles;
import com.fernando.iop.user.model.User;
import com.fernando.iop.user.repository.UserH2Repository;
import com.fernando.iop.user.service.UserService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceTest {

    @Autowired
    public UserService userService;
    @Autowired
    public UserH2Repository userH2Repository;


    @Nested
    @DisplayName("1. Busca de Usuário (findUserByEmailAndProjectId)")
    class FindUserTests {

        @Test
        @DisplayName("Caminho Feliz: Deve retornar usuario correto")
        void loadUserByUsernameAndProjectNameAndActiveTrueDeveRetornarUsuario() {

            UUID uuid = UUID.fromString("11111111-2222-3333-4444-555555555555");

            UserEntityResponseDTO user = userService.findUserByEmailAndProjectId("admin.alpha@iop.com", new Project(uuid));
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
            userH2Repository.save(user);

            assertThatThrownBy(() -> {
                userService.findUserByEmailAndProjectId(user.getUserEmail(), user.getProject());
            }).isInstanceOf(EntityNotFoundException.class);

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

            User user1 = userH2Repository.findByUserEmailAndProject_ProjectId("teste", uuid).orElseThrow();

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
            }).isInstanceOf(EntityNotFoundException.class);

        }

        @Test
        @DisplayName("Caminho Triste: Usuario ja Existente")
        void createUserDeveLancarExcessaoSeUsuarioJaExistir() {
            UUID uuid = UUID.fromString("11111111-2222-3333-4444-555555555555");
            userService.createUser("teste", "TestPassword", new Project(uuid), UserRoles.ROLE_ADMIN);

            assertThatThrownBy(() -> {
                userService.createUser("teste", "TestPassword", new Project(uuid), UserRoles.ROLE_ADMIN);
            }).isInstanceOf(EntityExistsException.class);

        }

    }


}
