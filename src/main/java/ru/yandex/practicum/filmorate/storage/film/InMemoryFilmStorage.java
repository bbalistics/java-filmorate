package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    @Override
    public Film addFilm(Film film) {
        film.setId(nextId++);
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        //Сохраняем существующие лайки при обновлении
        Film existingFilm = films.get(film.getId());
        if (existingFilm != null && existingFilm.getLikes() != null) {
            film.setLikes(existingFilm.getLikes());
        } else if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }

        films.put(film.getId(), film);
        return film;
    }

    @Override
    public void deleteFilm(Integer id) {
        films.remove(id);
    }

    @Override
    public Optional<Film> getFilmById(Integer id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public boolean containsFilm(Integer id) {
        return films.containsKey(id);
    }
}
