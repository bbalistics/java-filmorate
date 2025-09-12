package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.db.GenreDbStorage;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({GenreDbStorage.class})
class GenreDbStorageTest {

    @Autowired
    private GenreDbStorage genreStorage;
    private final JdbcTemplate jdbcTemplate;

    @Test
    void shouldFindGenreById() {
        Genre genre = genreStorage.getGenreById(1);

        assertThat(genre)
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "Комедия");
    }

    @Test
    void shouldReturnAllGenresInOrder() {
        List<Genre> genres = genreStorage.getAllGenres();

        assertThat(genres).hasSize(6)
                .extracting("id", "name")
                .containsExactly(
                        tuple(1, "Комедия"),
                        tuple(2, "Драма"),
                        tuple(3, "Мультфильм"),
                        tuple(4, "Триллер"),
                        tuple(5, "Документальный"),
                        tuple(6, "Боевик")
                );
    }

    @Test
    void shouldDeleteAllGenresForFilm() {
        int filmId = 1;

        genreStorage.deleteAllGenres(filmId);

        List<Genre> remaining = jdbcTemplate.query(
                "SELECT g.genre_id AS id, g.genre_name AS name " +
                        "FROM genres g " +
                        "JOIN film_genres fg ON g.genre_id = fg.genre_id " +
                        "WHERE fg.film_id = ?",
                (rs, rn) -> new Genre(rs.getInt("id"), rs.getString("name")),
                filmId
        );

        assertThat(remaining).isEmpty();
    }
}
