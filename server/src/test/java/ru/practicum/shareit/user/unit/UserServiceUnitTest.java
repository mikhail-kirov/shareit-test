package ru.practicum.shareit.user.unit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.data.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.service.UserServiceImpl;
import ru.practicum.shareit.validation.exeption.IncorrectParameterException;
import ru.practicum.shareit.validation.exeption.NotFoundException;
import ru.practicum.shareit.validation.user.ValidationUser;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {
    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private ValidationUser mockValidationUser;
    private UserService userService;
    private User user;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(mockValidationUser, mockUserRepository);
        user = new User(1L, "John", "test1@mail.com");
    }

    @Test
    void testCreateUser() {
        when(mockUserRepository.save(any())).thenReturn(user);

        User result = userService.addUser(user);

        assertThat(result, equalTo(user));
    }

    @Test
    void testCreateUserWhenException() {
        when(mockValidationUser.validationUserByEmailSame(any()))
                .thenReturn(user);

        final IncorrectParameterException exception = Assertions.assertThrows(
                IncorrectParameterException.class,
                () -> userService.addUser(user)
        );
        Assertions.assertEquals("Пользователь с указанным email уже зарегистрирован", exception.getMessage());
    }

    @Test
    void testUpdateUser() {
        User updatedUser = new User(1L, "Mike", "test1@mail.com");
        when(mockValidationUser.validationUserById(updatedUser.getId())).thenReturn(user);
        when(mockUserRepository.save(any())).thenReturn(updatedUser);

        User result = userService.updateUser(updatedUser.getId(), updatedUser);

        assertThat(result.getId(), equalTo(user.getId()));
        assertThat(result.getName(), equalTo(updatedUser.getName()));
        assertThat(result.getEmail(), equalTo(user.getEmail()));
    }

    @Test
    void testUpdateUserNotFoundException() {
        when(mockValidationUser.validationUserById(anyLong()))
                .thenThrow(new NotFoundException("Пользователь с ID 1 не зарегистрирован"));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> userService.updateUser(anyLong(), user)
        );
        Assertions.assertEquals("Пользователь с ID 1 не зарегистрирован", exception.getMessage());
    }

    @Test
    void testUpdateUserIncorrectParameterException() {
        when(mockValidationUser.validationUserById(anyLong())).thenReturn(user);
        when(mockValidationUser.validationUserByEmailSame(user.getEmail()))
                .thenReturn(new User(2L, "Mark", "test1@mail.com"));

        final IncorrectParameterException exception = Assertions.assertThrows(
                IncorrectParameterException.class,
                () -> userService.updateUser(anyLong(), user)
        );
        Assertions.assertEquals("Пользователь с указанным email уже зарегистрирован", exception.getMessage());
    }

    @Test
    void testDeleteUser() {
        when(mockValidationUser.validationUserById(user.getId())).thenReturn(user);
        User result = userService.removeUser(user.getId());
        assertThat(result, equalTo(user));
    }

    @Test
    void testDeleteUserNotFoundException() {
        when(mockValidationUser.validationUserById(anyLong()))
                .thenThrow(new NotFoundException("Пользователь с ID 1 не зарегистрирован"));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> userService.removeUser(user.getId())
        );
        Assertions.assertEquals("Пользователь с ID 1 не зарегистрирован", exception.getMessage());
    }

    @Test
    void testGetUser() {
        when(mockValidationUser.validationUserById(anyLong())).thenReturn(user);
        User result = userService.getUserById(1L);
        assertThat(result, equalTo(user));
    }

    @Test
    void testGetUserNotFoundException() {
        when(mockValidationUser.validationUserById(anyLong()))
                .thenThrow(new NotFoundException("Пользователь с ID 1 не зарегистрирован"));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> userService.getUserById(user.getId())
        );
        Assertions.assertEquals("Пользователь с ID 1 не зарегистрирован", exception.getMessage());
    }

    @Test
    void testGetAllUsers() {
        List<User> users = List.of(user, new User(2L,"Mike", "test2@mail.com"));
        when(mockUserRepository.findAll()).thenReturn(users);

        Collection<User> resultUsers = userService.getUsers();
        users.forEach(
                user -> assertThat(resultUsers, hasItem(allOf(
                        hasProperty("id", notNullValue()),
                        hasProperty("name", equalTo(user.getName())),
                        hasProperty("email", equalTo(user.getEmail()))
                )))
        );
    }
}
