package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> createUser(@Valid @RequestBody UserDto user) {
        log.info("Запрос на добавление пользователя {}", user.getEmail());
        return userClient.addUser(user);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateUser(@RequestBody UserDto user, @PathVariable long id) {
        log.info("Запрос на изменение пользователя с ID: {}", id);
        return userClient.updateUser(id, user);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUser(@PathVariable long userId) {
        log.info("Запрос на получение пользователя по ID: {}", userId);
        return userClient.getUserById(userId);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(@PathVariable long userId) {
        log.info("Запрос на удаление пользователя с ID: {}", userId);
        return userClient.removeUser(userId);
    }
}
