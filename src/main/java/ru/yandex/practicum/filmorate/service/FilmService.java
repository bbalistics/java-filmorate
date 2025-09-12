package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.validation.FilmValidator;

import java.util.*;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final FilmValidator filmValidator;
    private final UserService userService;

    @Autowired
    public FilmService(FilmStorage filmStorage, FilmValidator filmValidator, UserService userService) {
        this.filmStorage = filmStorage;
        this.filmValidator = filmValidator;
        this.userService = userService;
    }

    public Film addFilm(Film film) {
        log.debug("Добавление фильма: {}", film);
        filmValidator.validate(film);
        Film addedFilm = filmStorage.addFilm(film);
        log.info("Фильм добавлен: ID={}, название='{}'", addedFilm.getId(), addedFilm.getName());
        return addedFilm;
    }

    public Film updateFilm(Film film) {
        log.debug("Обновление фильма: {}", film);
        if (film.getId() == null || !filmStorage.containsFilm(film.getId())) {
            log.warn("Попытка обновления несуществующего фильма: ID={}", film.getId());
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
        filmValidator.validate(film);
        Film updatedFilm = filmStorage.updateFilm(film);
        log.info("Фильм обновлен: ID={}, название='{}'", updatedFilm.getId(), updatedFilm.getName());
        return updatedFilm;
    }

    public List<Film> getAllFilms() {
        log.debug("Запрос всех фильмов");
        List<Film> films = filmStorage.getAllFilms();
        log.info("Возвращено {} фильмов", films.size());
        return films;
    }

    public Film getFilmById(Integer id) {
        log.debug("Запрос фильма по ID: {}", id);
        Optional<Film> filmOptional = filmStorage.getFilmById(id);
        if (filmOptional.isPresent()) {
            Film film = filmOptional.get();
            log.info("Найден фильм: ID={}, название='{}'", film.getId(), film.getName());
            return film;
        } else {
            log.warn("Фильм не найден: ID={}", id);
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
    }

    public void addLike(Integer filmId, Integer userId) {
        log.debug("Добавление лайка: фильм ID={}, пользователь ID={}", filmId, userId);
        Film film = getFilmById(filmId);

        //Проверка существования пользователя
        userService.getUserById(userId);

        if (film.getLikes().contains(userId)) {
            log.warn("Пользователь ID={} уже поставил лайк фильму ID={}", userId, filmId);
            throw new ValidationException("Пользователь уже поставил лайк этому фильму");
        }

        film.getLikes().add(userId);
        filmStorage.updateFilm(film);
        log.info("Лайк добавлен: фильм ID={}, пользователь ID={}, всего лайков: {}",
                filmId, userId, film.getLikes().size());
    }

    public void removeLike(Integer filmId, Integer userId) {
        log.debug("Удаление лайка: фильм ID={}, пользователь ID={}", filmId, userId);
        Film film = getFilmById(filmId);

        //Проверка существования пользователя
        userService.getUserById(userId);

        if (!film.getLikes().contains(userId)) {
            log.warn("Пользователь ID={} не ставил лайк фильму ID={}", userId, filmId);
            throw new ValidationException("Пользователь не ставил лайк этому фильму");
        }

        film.getLikes().remove(userId);
        filmStorage.updateFilm(film);
        log.info("Лайк удален: фильм ID={}, пользователь ID={}, осталось лайков: {}",
                filmId, userId, film.getLikes().size());
    }

    public List<Film> getPopularFilms() {
        log.info("Запрос популярных фильмов");
        List<Film> allFilms = filmStorage.getAllFilms();

        //Сортируем фильмы по количеству лайков по убыванию
        Collections.sort(allFilms, new Comparator<Film>() {
            @Override
            public int compare(Film film1, Film film2) {
                return Integer.compare(film2.getLikes().size(), film1.getLikes().size());
            }
        });

        //Берем первые 10 фильмов, либо сколько есть (если <10)
        List<Film> popularFilms = new ArrayList<>();
        for (int i = 0; i < Math.min(10, allFilms.size()); i++) {
            popularFilms.add(allFilms.get(i));
        }

        log.info("Возвращено {} популярных фильмов", popularFilms.size());
        return popularFilms;
    }
}
