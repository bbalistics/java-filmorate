package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.MpaRatingStorage;

import java.sql.*;
import java.sql.Date;
import java.util.*;

@Repository
public class FilmDbStorage implements FilmStorage {

    private final MpaRatingStorage mpaRatingStorage;
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(MpaRatingStorage mpaRatingStorage, JdbcTemplate jdbcTemplate) {
        this.mpaRatingStorage = mpaRatingStorage;
        this.jdbcTemplate = jdbcTemplate;
    }

    //Маппер для Film
    private final RowMapper<Film> filmRowMapper = (rs, rowNum) -> {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        Integer mpaId = rs.getObject("mpa_rating_id", Integer.class);
        if (mpaId != null) {
            MpaRating mpa = new MpaRating();
            mpa.setId(mpaId);
            mpa.setName(rs.getString("mpa_name"));
            film.setMpaRating(mpa);
        }

        return film;
    };

    //Маппер для Genre
    private final RowMapper<Genre> genreRowMapper = (rs, rowNum) -> {
        return new Genre(rs.getInt("id"), rs.getString("name"));
    };

    @Override
    public Film save(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setObject(5, film.getMpaRating() != null ? film.getMpaRating().getId() : null);
            return ps;
        }, keyHolder);

        int filmId = keyHolder.getKey().intValue();
        film.setId(filmId);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            addGenre(filmId, film.getGenres());
        }

        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, " +
                "mpa_rating_id = ? WHERE id = ?";

        int rows = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpaRating() != null ? film.getMpaRating().getId() : null,
                film.getId());

        if (rows == 0) {
            throw new RuntimeException("Фильм с id = " + film.getId() + " не найден.");
        }

        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            addGenre(film.getId(), film.getGenres());
        }

        return film;
    }

    @Override
    public void deleteFilm(Integer id) {
        String sql = "DELETE FROM films WHERE id = ?";
        int rows = jdbcTemplate.update(sql, id);
        if (rows == 0) {
            throw new RuntimeException("Фильм с id = " + id + " не найден.");
        }
    }

    @Override
    public Optional<Film> getFilmById(Integer id) {
        String sql = """
                SELECT 
                    f.id, f.name, f.description, f.release_date, f.duration, 
                    f.mpa_rating_id,
                    mr.name AS mpa_name
                FROM films f
                LEFT JOIN mpa_ratings mr ON f.mpa_rating_id = mr.id
                WHERE f.id = ?
                """;

        try {
            Film film = jdbcTemplate.queryForObject(sql, filmRowMapper, id);

            //Загружаем жанры в порядке их добавления
            List<Genre> genres = jdbcTemplate.query(
                    """
                    SELECT g.genre_id AS id, g.genre_name AS name
                    FROM genres g
                    JOIN film_genres fg ON g.genre_id = fg.genre_id
                    WHERE fg.film_id = ?
                    ORDER BY fg.genre_id
                    """,
                    genreRowMapper, id);
            film.setGenres(genres);

            //Загружаем лайки
            List<Integer> likes = jdbcTemplate.query(
                    "SELECT user_id FROM film_likes WHERE film_id = ? ORDER BY user_id",
                    (rs, rowNum) -> rs.getInt("user_id"), id);
            film.setLikes(new LinkedHashSet<>(likes));

            return Optional.of(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = """
                SELECT 
                    f.id, f.name, f.description, f.release_date, f.duration, 
                    f.mpa_rating_id,
                    mr.name AS mpa_name
                FROM films f
                LEFT JOIN mpa_ratings mr ON f.mpa_rating_id = mr.id
                """;

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);

        for (Film film : films) {
            List<Genre> genres = jdbcTemplate.query(
                    """
                    SELECT g.genre_id AS id, g.genre_name AS name
                    FROM genres g
                    JOIN film_genres fg ON g.genre_id = fg.genre_id
                    WHERE fg.film_id = ?
                    ORDER BY fg.genre_id
                    """,
                    genreRowMapper, film.getId());
            film.setGenres(genres);

            List<Integer> likes = jdbcTemplate.query(
                    "SELECT user_id FROM film_likes WHERE film_id = ? ORDER BY user_id",
                    (rs, rowNum) -> rs.getInt("user_id"), film.getId());
            film.setLikes(new LinkedHashSet<>(likes));
        }

        return films;
    }

    @Override
    public boolean containsFilm(Integer id) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public void addGenre(int filmId, List<Genre> genres) {
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        for (Genre genre : genres) {
            jdbcTemplate.update(sql, filmId, genre.getId());
        }
    }

    @Override
    public void addLike(int filmId, int userId) {
        String sql = "MERGE INTO film_likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }
}

