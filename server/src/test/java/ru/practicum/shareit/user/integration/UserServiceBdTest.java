package ru.practicum.shareit.user.integration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;
import ru.practicum.shareit.validation.exeption.NotFoundException;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import static org.junit.jupiter.api.Assertions.*;


@Transactional
@SpringBootTest(properties = "jdbc.url=jdbc:h2:mem:shareit://localhost:9070/test",
                webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UserServiceBdTest {

    private final UserServiceImpl userService;
    private final EntityManager em;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User(null,"Mike", "test@mail.com");
        em.persist(user);
        em.flush();
    }

    @Test
    void createUser() {
        user = new User(null,"Oleg", "test2@mail.com");
        User user1 = userService.addUser(user);
        TypedQuery<User> query = em.createQuery("select u from User u where u.id = :id", User.class);
        User result = query.setParameter("id", user1.getId()).getSingleResult();

        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(user.getId()));
        assertThat(result.getEmail(), equalTo(user.getEmail()));
    }

    @Test
    void updateUserTest() {
        Long id = user.getId();
        user = new User(null,"Mik", "test1@mail.com");
        userService.updateUser(id, user);

        TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.id = :id", User.class);
        User userUpdated = query.setParameter("id", id).getSingleResult();

        assertThat(userUpdated.getId(), notNullValue());
        assertThat(userUpdated.getName(), equalTo("Mik"));
        assertThat(userUpdated.getEmail(), equalTo("test1@mail.com"));
    }

    @Test
    void getUserByIdTest() {
        User result = userService.getUserById(user.getId());

        TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.id = :id", User.class);
        User userUpdated = query.setParameter("id", user.getId()).getSingleResult();

        assertThat(userUpdated.getId(), notNullValue());
        assertThat(userUpdated.getName(), equalTo(result.getName()));
        assertThat(userUpdated.getEmail(), equalTo(result.getEmail()));
    }

    @Test
    void removeUserByIdTest() {
        user = new User(null,"Max", "test3@mail.com");
        User user1 = userService.addUser(user);
        userService.removeUser(user1.getId());

        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.getUserById(user1.getId())
        );
        assertEquals("Пользователь с ID 2 не зарегистрирован", exception.getMessage());
    }

    @Test
    void getAllUsersTest() {
        userService.addUser(new User(null,"Petr", "test4@mail.com"));
        Collection<User> userCollection = userService.getUsers();

        TypedQuery<User> query = em.createQuery("SELECT u FROM User u", User.class);
        List<User> userResult = query.getResultList();

        assertEquals(userCollection, userResult);
    }
}
