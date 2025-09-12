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

    public void addFriend(Integer userId, Integer friendId) {
        log.debug("Добавление в друзья: пользователь ID={}, друг ID={}", userId, friendId);
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (user.getFriends().contains(friendId)) {
            log.warn("Пользователь ID={} уже в друзьях у пользователя ID={}", friendId, userId);
            throw new ValidationException("Пользователь уже добавлен в друзья");
        }

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        userStorage.updateUser(user);
        userStorage.updateUser(friend);
        log.info("Дружба установлена: пользователь ID={} и пользователь ID={}", userId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        log.debug("Удаление из друзей: пользователь ID={}, друг ID={}", userId, friendId);
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        userStorage.updateUser(user);
        userStorage.updateUser(friend);
        log.info("Удаление из друзей: пользователь ID={} и пользователь ID={}", userId, friendId);
    }

    public List<User> getFriends(Integer userId) {
        log.debug("Запрос друзей пользователя: ID={}", userId);
        User user = getUserById(userId);
        List<User> friends = new ArrayList<>();
        for (Integer friendId : user.getFriends()) {
            friends.add(getUserById(friendId));
        }
        log.info("Найдено {} друзей пользователя ID={}", friends.size(), userId);
        return friends;
    }

    public List<User> getCommonFriends(Integer userId1, Integer userId2) {
        log.debug("Запрос общих друзей: пользователь ID={} и пользователь ID={}", userId1, userId2);
        User user1 = getUserById(userId1);
        User user2 = getUserById(userId2);

        Set<Integer> commonFriendIds = new HashSet<>(user1.getFriends());
        commonFriendIds.retainAll(user2.getFriends());

        List<User> commonFriends = new ArrayList<>();
        for (Integer friendId : commonFriendIds) {
            commonFriends.add(getUserById(friendId));
        }

        log.info("Найдено {} общих друзей пользователей ID={} и ID={}",
                commonFriends.size(), userId1, userId2);
        return commonFriends;
    }
}
