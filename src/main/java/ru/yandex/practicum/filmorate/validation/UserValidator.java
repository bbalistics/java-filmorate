package ru.yandex.practicum.filmorate.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@Slf4j
@Component
public class UserValidator {

    public void validate(User user) {
        log.debug("Начало валидации пользователя: {}", user);

        validateEmail(user);
        validateLogin(user);
        validateBirthday(user);

        log.debug("Валидация пользователя пройдена успешно");
    }

    //Проверка email
    private void validateEmail(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Попытка создания пользователя с пустым email");
            throw new ValidationException("Email не может быть пустым");
        }
        if (!user.getEmail().contains("@")) {
            log.warn("Попытка создания пользователя с email без @: {}", user.getEmail());
            throw new ValidationException("Email должен содержать символ @");
        }
    }

    //Проверка логина
    private void validateLogin(User user) {
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("Попытка создания пользователя с пустым логином");
            throw new ValidationException("Логин не может быть пустым");
        }
        if (user.getLogin().contains(" ")) {
            log.warn("Попытка создания пользователя с логином, содержащим пробелы: {}", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }
    }

    //Проверка даты рождения
    private void validateBirthday(User user) {
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Попытка создания пользователя с датой рождения в будущем: {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
