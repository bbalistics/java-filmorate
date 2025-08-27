package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESCRIPTION_LENGTH = 200;

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        log.info("Получен запрос на добавление фильма: {}", film);
        try {
            validateFilm(film);
            film.setId(nextId);
            films.put(nextId, film);
            log.info("Фильм добавлен. ID: {}, название: {}", nextId, film.getName());
            nextId++;
            return film;
        } catch (ValidationException e) {
            log.warn("Ошибка валидации при добавлении фильма: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Ошибка при добавлении фильма", e);
            throw e;
        }
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        log.info("Получен запрос на обновление фильма с ID: {}", film.getId());
        try {
            if (film.getId() == null || !films.containsKey(film.getId())) {
                String errorMessage = "Фильм с id=" + film.getId() + " не найден";
                log.warn(errorMessage);
                throw new ValidationException(errorMessage);
            }
            validateFilm(film);
            films.put(film.getId(), film);
            log.info("Фильм с ID {} успешно обновлен. Новые данные: {}", film.getId(), film);
            return film;
        } catch (ValidationException e) {
            log.warn("Ошибка валидации при обновлении фильма: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Неожиданная ошибка при обновлении фильма с ID: {}", film.getId(), e);
            throw e;
        }
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("Получен запрос на получение всех фильмов. Количество фильмов: {}", films.size());
        return new ArrayList<>(films.values());
    }

    private void validateFilm(Film film) {
        log.debug("Начало валидации фильма: {}", film);

        //Проверка названия
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Попытка добавления фильма с пустым названием");
            throw new ValidationException("Название фильма не может быть пустым");
        }

        //Проверка описания
        if (film.getDescription() != null && film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            log.warn("Попытка добавления фильма с описанием длиной {} символов (максимум {})",
                    film.getDescription().length(), MAX_DESCRIPTION_LENGTH);
            throw new ValidationException("Описание фильма не может превышать " + MAX_DESCRIPTION_LENGTH + " символов");
        }

        //Проверка даты релиза
        if (film.getReleaseDate() == null) {
            log.warn("Попытка добавления фильма без даты релиза");
            throw new ValidationException("Дата релиза не может быть пустой");
        }
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.warn("Попытка добавления фильма с датой релиза {} (минимум {})",
                    film.getReleaseDate(), MIN_RELEASE_DATE);
            throw new ValidationException("Дата релиза не может быть раньше " + MIN_RELEASE_DATE);
        }

        //Проверка продолжительности
        if (film.getDuration() == null || film.getDuration() <= 0) {
            log.warn("Попытка добавления фильма с продолжительностью: {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }

        log.debug("Валидация фильма пройдена успешно");
    }
}
