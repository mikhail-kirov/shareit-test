package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserService {

    User addUser(User user);

    User updateUser(Long id, User user);

    User removeUser(Long id);

    User getUserById(Long id);

    Collection<User> getUsers();
}
