package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface GenreStorage {
    void deleteAllGenres(int filmId);

    Genre getGenreById(int genreId);

    List<Genre> getAllGenres();
}
