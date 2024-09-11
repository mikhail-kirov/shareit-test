package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.data.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.validation.exeption.IncorrectParameterException;
import ru.practicum.shareit.validation.user.ValidationUser;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final ValidationUser validationUser;
    private final UserRepository userRepository;

    @Override
    public User addUser(User user) {
        User user1 = validationUser.validationUserByEmailSame(user.getEmail());
        if (user1 != null) {
            throw new IncorrectParameterException("Пользователь с указанным email уже зарегистрирован");
        }
        log.info("Пользователь {} добавлен", user.getEmail());
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long userId, User user) {
        User user1 = validationUser.validationUserById(userId);
        User user2 = validationUser.validationUserByEmailSame(user.getEmail());
        if (user2 != null && !user1.getId().equals(user2.getId())) {
            throw new IncorrectParameterException("Пользователь с указанным email уже зарегистрирован");
        }
        if (user.getEmail() != null) {
            user1.setEmail(user.getEmail());
        }
        if (user.getName() != null) {
            user1.setName(user.getName());
        }
        userRepository.save(user1);
        log.info("Данные пользователя {} обновлены", user1.getEmail());
        return user1;
    }

    @Override
    public User removeUser(Long id) {
        User user = validationUser.validationUserById(id);
        userRepository.delete(user);
        log.info("Пользователь {} удален", user.getEmail());
        return user;
    }

    @Override
    public User getUserById(Long id) {
        User user = validationUser.validationUserById(id);
        log.info("Найден пользователь с ID {} найден: {}", id, user.getEmail());
        return user;
    }

    @Override
    public Collection<User> getUsers() {
        Collection<User> users = userRepository.findAll();
        log.info("Список пользователей сформирован и отправлен");
        return users;
    }
}
