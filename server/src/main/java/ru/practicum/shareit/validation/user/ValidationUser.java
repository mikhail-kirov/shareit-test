package ru.practicum.shareit.validation.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.data.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.validation.exeption.NotFoundException;

@Component
@RequiredArgsConstructor
@Slf4j
public class ValidationUser {

    private final UserRepository userRepository;

    public User validationUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не зарегистрирован"));
    }

    public User validationUserByEmailSame(String email) {
        return userRepository.findUserByEmail(email);
    }
}
