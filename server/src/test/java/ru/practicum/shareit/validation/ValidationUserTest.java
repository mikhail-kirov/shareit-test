package ru.practicum.shareit.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.data.UserRepository;
import ru.practicum.shareit.validation.exeption.NotFoundException;
import ru.practicum.shareit.validation.user.ValidationUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class ValidationUserTest {
    @Mock
    private UserRepository userRepo;

    private ValidationUser validationUser;

    @BeforeEach
    public void setUp() {
        validationUser = new ValidationUser(userRepo);
    }

    @Test
    void testValidateUserByIdNotFoundException() {
        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> validationUser.validationUserById(1L)
        );
        assertEquals("Пользователь с ID 1 не зарегистрирован", exception.getMessage());
    }
}
