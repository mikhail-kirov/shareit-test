package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Запрос на добавление пользователя {}", user.getEmail());
        return userService.addUser(user);
    }

    @PatchMapping("/{id}")
    public User updateUser(@RequestBody User user,
                           @PathVariable long id) {
        log.info("Запрос на изменение пользователя с ID: {}", id);
        return userService.updateUser(id, user);
    }

    @GetMapping
    public Collection<User> getAllUsers() {
        log.info("Запрос на получение списка всех пользователей");
        return userService.getUsers();
    }

    @GetMapping("/{userId}")
    public User getUser(@PathVariable long userId) {
        log.info("Запрос на получение пользователя по ID: {}", userId);
        return userService.getUserById(userId);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable long userId) {
        log.info("Запрос на удаление пользователя с ID: {}", userId);
        userService.removeUser(userId);
    }
}
