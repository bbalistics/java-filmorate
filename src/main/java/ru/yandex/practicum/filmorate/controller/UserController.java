package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
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

    @PostMapping
    public User createUser(@RequestBody User user) {
        log.info("Получен запрос на создание пользователя: {}", user);
        try {
            validateUser(user);
            user.setId(nextId);

            //Если имя пустое
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
            validateUser(user);

            //Если имя пустое
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

    private void validateUser(User user) {
        log.debug("Начало валидации пользователя: {}", user);

        //Проверка email
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Попытка создания пользователя с пустым email");
            throw new ValidationException("Email не может быть пустым");
        }
        if (!user.getEmail().contains("@")) {
            log.warn("Попытка создания пользователя с email без @: {}", user.getEmail());
            throw new ValidationException("Email должен содержать символ @");
        }

        //Проверка логина
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("Попытка создания пользователя с пустым логином");
            throw new ValidationException("Логин не может быть пустым");
        }
        if (user.getLogin().contains(" ")) {
            log.warn("Попытка создания пользователя с логином, содержащим пробелы: {}", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }

        //Проверка даты рождения
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Попытка создания пользователя с датой рождения в будущем: {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        log.debug("Валидация пользователя пройдена успешно");
    }
}
