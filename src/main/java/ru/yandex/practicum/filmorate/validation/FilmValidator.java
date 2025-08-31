package ru.yandex.practicum.filmorate.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

@Slf4j
@Component
public class FilmValidator {
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESCRIPTION_LENGTH = 200;

    public void validate(Film film) {
        log.debug("Начало валидации фильма: {}", film);

        validateName(film);
        validateDescription(film);
        validateReleaseDate(film);
        validateDuration(film);

        log.debug("Валидация фильма пройдена");
    }

    //Проверка названия
    private void validateName(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Попытка добавления фильма с пустым названием");
            throw new ValidationException("Название фильма не может быть пустым");
        }
    }

    //Проверка описания
    private void validateDescription(Film film) {
        if (film.getDescription() != null && film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            log.warn("Попытка добавления фильма с описанием длиной {} символов (максимум {})",
                    film.getDescription().length(), MAX_DESCRIPTION_LENGTH);
            throw new ValidationException("Описание фильма не может превышать " + MAX_DESCRIPTION_LENGTH + " символов");
        }
    }

    //Проверка даты релиза
    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate() == null) {
            log.warn("Попытка добавления фильма без даты релиза");
            throw new ValidationException("Дата релиза не может быть пустой");
        }
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.warn("Попытка добавления фильма с датой релиза {} (минимум {})",
                    film.getReleaseDate(), MIN_RELEASE_DATE);
            throw new ValidationException("Дата релиза не может быть раньше " + MIN_RELEASE_DATE);
        }
    }

    //Проверка продолжительности
    private void validateDuration(Film film) {
        if (film.getDuration() == null || film.getDuration() <= 0) {
            log.warn("Попытка добавления фильма с продолжительностью: {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}
