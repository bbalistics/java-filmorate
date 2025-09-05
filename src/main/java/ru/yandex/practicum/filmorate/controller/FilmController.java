package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.FilmValidator;

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
    private final FilmValidator filmValidator;

    public FilmController(FilmValidator filmValidator) {
        this.filmValidator = filmValidator;
    }

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        log.info("Получен запрос на добавление фильма: {}", film);
        try {
            filmValidator.validate(film);
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
            filmValidator.validate(film);
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
}
