package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    void deleteFilm(Integer id);

    Optional<Film> getFilmById(Integer id);

    List<Film> getAllFilms();

    boolean containsFilm(Integer id);
}
