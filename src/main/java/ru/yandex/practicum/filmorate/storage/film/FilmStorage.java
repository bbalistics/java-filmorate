package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Film updateFilm(Film film);

    void deleteFilm(Integer id);

    Optional<Film> getFilmById(Integer id);

    List<Film> getAllFilms();

    Film save(Film film);

    boolean containsFilm(Integer id);

    void addGenre(int filmId, List<Genre> genres);

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);
}
