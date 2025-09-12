package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        log.info("POST /films - Создание нового фильма");
        return filmService.addFilm(film);
    }

    @PutMapping
    public ResponseEntity<Film> updateFilm(@RequestBody Film film) {
        log.info("PUT /films - Обновление фильма с id={}", film.getId());

        if (film.getId() == null) {
            throw new ValidationException("ID фильма не может быть null при обновлении");
        }

        Film updatedFilm = filmService.updateFilm(film);
        return ResponseEntity.ok(updatedFilm);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Film> updateFilm(@PathVariable Integer id, @RequestBody Film film) {
        log.info("PUT /films/{} - Обновление фильма", id);

        //Если id в теле null — подставляем из URL
        if (film.getId() == null) {
            film.setId(id);
        } else if (!id.equals(film.getId())) {
            throw new ValidationException("ID в пути и в теле не совпадают");
        }

        Film updatedFilm = filmService.updateFilm(film);
        return ResponseEntity.ok(updatedFilm);
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("GET /films - Получение всех фильмов");
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Integer id) {
        log.info("GET /films/{} - Получение фильма по ID", id);
        return filmService.getFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("PUT /films/{}/like/{} - Добавление лайка", id, userId);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("DELETE /films/{}/like/{} - Удаление лайка", id, userId);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("GET /films/popular - Получение популярных фильмов (count = {})", count);
        return filmService.getPopularFilms(count);
    }


    @GetMapping("/mpa")
    public List<MpaRating> getAllMpaRatings() {
        log.info("GET /films/mpa - Получение всех MPA-рейтингов");
        return filmService.getAllMpaRatings();
    }

    @GetMapping("/mpa/{id}")
    public MpaRating getMpaRatingById(@PathVariable Integer id) {
        log.info("GET /films/mpa/{} - Получение MPA-рейтинга", id);
        return filmService.getMpaRatingById(id);
    }
}

