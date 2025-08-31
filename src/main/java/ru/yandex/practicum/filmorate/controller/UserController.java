package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;
    private final UserValidator userValidator;

    public UserController(UserValidator userValidator) {
        this.userValidator = userValidator;
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        log.info("Получен запрос на создание пользователя: {}", user);
        try {
            userValidator.validate(user);
            user.setId(nextId);

            if (user.getName() == null || user.getName().isBlank()) {
                log.debug("Имя пользователя пустое, будет использован логин: {}", user.getLogin());
                user.setName(user.getLogin());
            }

            users.put(nextId, user);
            log.info("Пользователь создан успешно. ID: {}, логин: {}", nextId, user.getLogin());
            nextId++;
            return user;
        } catch (ValidationException e) {
            log.warn("Ошибка валидации при создании пользователя: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Ошибка при создании пользователя", e);
            throw e;
        }
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        log.info("Получен запрос на обновление пользователя с ID: {}", user.getId());
        try {
            if (user.getId() == null || !users.containsKey(user.getId())) {
                String errorMessage = "Пользователь с id=" + user.getId() + " не найден";
                log.warn(errorMessage);
                throw new ValidationException(errorMessage);
            }
            userValidator.validate(user);

            if (user.getName() == null || user.getName().isBlank()) {
                log.debug("Имя пользователя пустое, будет использован логин: {}", user.getLogin());
                user.setName(user.getLogin());
            }

            users.put(user.getId(), user);
            log.info("Пользователь с ID {} успешно обновлен. Новые данные: {}", user.getId(), user);
            return user;
        } catch (ValidationException e) {
            log.warn("Ошибка валидации при обновлении пользователя: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Ошибка при обновлении пользователя с ID: {}", user.getId(), e);
            throw e;
        }
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.info("Получен запрос на получение всех пользователей. Количество пользователей: {}", users.size());
        return new ArrayList<>(users.values());
    }
}
