package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;
    private final UserValidator userValidator;

    @Autowired
    public UserService(UserStorage userStorage, UserValidator userValidator) {
        this.userStorage = userStorage;
        this.userValidator = userValidator;
    }

    public User addUser(User user) {
        log.debug("Добавление пользователя: {}", user);
        userValidator.validate(user);
        User addedUser = userStorage.addUser(user);
        log.info("Пользователь добавлен: ID={}, логин='{}'", addedUser.getId(), addedUser.getLogin());
        return addedUser;
    }

    public User updateUser(User user) {
        log.debug("Обновление пользователя: {}", user);
        if (user.getId() == null || !userStorage.containsUser(user.getId())) {
            log.warn("Попытка обновления несуществующего пользователя: ID={}", user.getId());
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
        userValidator.validate(user);
        User updatedUser = userStorage.updateUser(user);
        log.info("Пользователь обновлен: ID={}, логин='{}'", updatedUser.getId(), updatedUser.getLogin());
        return updatedUser;
    }

    public List<User> getAllUsers() {
        log.debug("Запрос всех пользователей");
        List<User> users = userStorage.getAllUsers();
        log.info("Возвращено {} пользователей", users.size());
        return users;
    }

    public User getUserById(Integer id) {
        log.debug("Запрос пользователя по ID: {}", id);
        Optional<User> userOptional = userStorage.getUserById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            log.info("Найден пользователь: ID={}, логин='{}'", user.getId(), user.getLogin());
            return user;
        } else {
            log.warn("Пользователь не найден: ID={}", id);
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
    }

    public void addFriendRequest(Integer userId, Integer friendId) {
        log.debug("Добавление в друзья: {} → {}", userId, friendId);

        if (userId == null || friendId == null) {
            throw new ValidationException("ID пользователя и друга не могут быть null");
        }

        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        if (!userStorage.containsUser(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        if (!userStorage.containsUser(friendId)) {
            throw new NotFoundException("Пользователь с id=" + friendId + " не найден");
        }

        userStorage.addFriend(userId, friendId);
        log.info("Пользователь {} добавил {} в друзья", userId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        log.debug("Удаление из друзей: {} и {}", userId, friendId);

        if (!userStorage.containsUser(userId) || !userStorage.containsUser(friendId)) {
            throw new NotFoundException("Один или оба пользователя не найдены");
        }

        userStorage.removeFriend(userId, friendId);
        log.info("Пользователь {} удалил {} из друзей", userId, friendId);
    }

    public List<User> getFriends(Integer userId) {
        log.debug("Запрос друзей пользователя: {}", userId);
        if (!userStorage.containsUser(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        Set<User> friends = userStorage.getFriends(userId);
        List<User> result = new ArrayList<>(friends);
        log.info("Найдено {} друзей", result.size());
        return result;
    }

    public List<User> getCommonFriends(Integer userId1, Integer userId2) {
        log.debug("Запрос общих друзей: {} и {}", userId1, userId2);

        if (!userStorage.containsUser(userId1)) {
            throw new NotFoundException("Пользователь с id=" + userId1 + " не найден");
        }
        if (!userStorage.containsUser(userId2)) {
            throw new NotFoundException("Пользователь с id=" + userId2 + " не найден");
        }

        Set<User> friends1 = userStorage.getFriends(userId1);
        Set<User> friends2 = userStorage.getFriends(userId2);

        List<User> common = friends1.stream()
                .filter(friends2::contains)
                .collect(Collectors.toList());

        log.info("Найдено {} общих друзей", common.size());
        return common;
    }
}

