package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.db.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.GenreStorage;
import ru.yandex.practicum.filmorate.storage.film.MpaRatingStorage;
import ru.yandex.practicum.filmorate.validation.FilmValidator;

import java.util.*;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final FilmValidator filmValidator;
    private final UserService userService;
    private final MpaRatingStorage mpaRatingStorage;
    private final GenreStorage genreStorage;
    private final GenreDbStorage genreDbStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage,
                       FilmValidator filmValidator,
                       UserService userService,
                       MpaRatingStorage mpaRatingStorage,
                       GenreStorage genreStorage, GenreDbStorage genreDbStorage) {
        this.filmStorage = filmStorage;
        this.filmValidator = filmValidator;
        this.userService = userService;
        this.mpaRatingStorage = mpaRatingStorage;
        this.genreStorage = genreStorage;
        this.genreDbStorage = genreDbStorage;
    }

    public Film addFilm(Film film) {
        //Валидация
        filmValidator.validate(film);

        if (film.getMpaRating() != null && film.getMpaRating().getId() != null) {
            Integer mpaId = film.getMpaRating().getId();
            try {
                MpaRating mpa = mpaRatingStorage.getMpaRatingById(mpaId);
                film.setMpaRating(mpa);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "MPA рейтинг с id = " + mpaId + " не найден.");
            }
        } else {
            try {
                MpaRating defaultMpa = mpaRatingStorage.getMpaRatingById(1);
                film.setMpaRating(defaultMpa);
            } catch (Exception e) {
                throw new RuntimeException("Справочник MPA не заполнен. Проверьте schema.sql.");
            }
        }

        if (film.getGenres() != null) {
            Set<Integer> seenIds = new HashSet<>();
            List<Genre> uniqueGenres = new ArrayList<>();

            for (Genre genre : film.getGenres()) {
                if (genre.getId() == null) {
                    throw new ValidationException("У жанра должен быть указан id.");
                }
                Genre dbGenre = genreDbStorage.getGenreById(genre.getId());

                if (seenIds.add(dbGenre.getId())) {
                    uniqueGenres.add(dbGenre);
                }
            }

            film.setGenres(uniqueGenres);
        } else {
            film.setGenres(new ArrayList<>());
        }

        return filmStorage.save(film);
    }

    public Film updateFilm(Film film) {
        if (film.getId() == null) {
            throw new ValidationException("Идентификатор фильма должен быть указан при обновлении.");
        }

        //Проверяем, что фильм существует
        if (!filmStorage.containsFilm(film.getId())) {
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден.");
        }

        //Валидация полей
        filmValidator.validate(film);

        //Обработка жанров, дубли убираются через id
        if (film.getGenres() != null) {
            Set<Integer> seenIds = new HashSet<>();
            List<Genre> uniqueGenres = new ArrayList<>();

            for (Genre genre : film.getGenres()) {
                if (genre.getId() == null) {
                    throw new ValidationException("У жанра должен быть указан id.");
                }

                Genre dbGenre = genreDbStorage.getGenreById(genre.getId());

                if (seenIds.add(dbGenre.getId())) { //true, если id ещё не встречался
                    uniqueGenres.add(dbGenre);
                }
            }

            film.setGenres(uniqueGenres);
        } else {
            film.setGenres(new ArrayList<>());
        }

        return filmStorage.updateFilm(film);
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
        userService.getUserById(userId);

        if (film.getLikes().contains(userId)) {
            throw new ValidationException("Пользователь уже поставил лайк этому фильму");
        }

        film.getLikes().add(userId);
        filmStorage.addLike(filmId, userId);

        log.info("Лайк добавлен: фильм ID={}, пользователь ID={}, всего лайков: {}",
                filmId, userId, film.getLikes().size());
    }

    public void removeLike(Integer filmId, Integer userId) {
        log.debug("Удаление лайка: фильм ID={}, пользователь ID={}", filmId, userId);
        Film film = getFilmById(filmId);
        userService.getUserById(userId);

        if (!film.getLikes().contains(userId)) {
            throw new ValidationException("Пользователь не ставил лайк этому фильму");
        }

        film.getLikes().remove(userId);
        filmStorage.removeLike(filmId, userId);

        log.info("Лайк удален: фильм ID={}, пользователь ID={}, осталось лайков: {}",
                filmId, userId, film.getLikes().size());
    }

    public List<Film> getPopularFilms(int count) {
        List<Film> sortedFilms = new ArrayList<>(filmStorage.getAllFilms());
        sortedFilms.sort((f1, f2) -> {
            int likes1 = f1.getLikes() != null ? f1.getLikes().size() : 0;
            int likes2 = f2.getLikes() != null ? f2.getLikes().size() : 0;
            return Integer.compare(likes2, likes1); //по убыванию
        });

        return sortedFilms.stream()
                .limit(Math.min(count, 10))
                .toList();
    }

    //MPA
    public List<MpaRating> getAllMpaRatings() {
        log.debug("Запрос всех MPA-рейтингов");
        return mpaRatingStorage.getAllMpaRatings();
    }

    public MpaRating getMpaRatingById(int id) {
        log.debug("Запрос MPA-рейтинга по ID: {}", id);
        List<MpaRating> allRatings = mpaRatingStorage.getAllMpaRatings();
        for (MpaRating rating : allRatings) {
            if (rating.getId() == id) {
                return rating;
            }
        }
        throw new NotFoundException("MPA-рейтинг с id=" + id + " не найден");
    }
}

