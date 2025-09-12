package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.db.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.db.MpaRatingDbStorage;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, MpaRatingDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmLikeDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        //Очищаем таблицы
        jdbcTemplate.update("DELETE FROM film_likes");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");

        //Добавляем двух пользователей
        jdbcTemplate.update(
                "INSERT INTO users (id, email, login, name, birthday) VALUES (?, ?, ?, ?, ?)",
                1, "user1@yandex.ru", "user1", "User One", LocalDate.of(1990, 1, 1)
        );
        jdbcTemplate.update(
                "INSERT INTO users (id, email, login, name, birthday) VALUES (?, ?, ?, ?, ?)",
                2, "user2@yandex.ru", "user2", "User Two", LocalDate.of(1991, 1, 1)
        );
    }

    @Test
    void addLike_shouldAddLikeFromUser() {
        //Добавляем фильм
        Film film = createTestFilm();
        Film savedFilm = filmStorage.save(film);

        //Добавляем лайк от пользователя 1
        filmStorage.addLike(savedFilm.getId(), 1);

        //Проверяем
        Optional<Film> result = filmStorage.getFilmById(savedFilm.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getLikes()).contains(1);
    }

    @Test
    void addLike_shouldNotAddDuplicateLikeFromSameUser() {
        Film film = filmStorage.save(createTestFilm());

        //Добавляем лайк дважды
        filmStorage.addLike(film.getId(), 1);
        filmStorage.addLike(film.getId(), 1);

        //Проверяем
        Set<Integer> likes = filmStorage.getFilmById(film.getId())
                .map(Film::getLikes)
                .orElse(Set.of());

        assertThat(likes).hasSize(1).contains(1);
    }

    @Test
    void addLike_shouldAllowMultipleUsersToLike() {
        Film film = filmStorage.save(createTestFilm());

        filmStorage.addLike(film.getId(), 1);
        filmStorage.addLike(film.getId(), 2);

        Set<Integer> likes = filmStorage.getFilmById(film.getId())
                .map(Film::getLikes)
                .orElse(Set.of());

        assertThat(likes).hasSize(2).contains(1, 2);
    }

    @Test
    void removeLike_shouldRemoveExistingLike() {
        Film film = filmStorage.save(createTestFilm());
        filmStorage.addLike(film.getId(), 1);

        //Удаляем лайк
        filmStorage.removeLike(film.getId(), 1);

        //Проверяем
        Set<Integer> likes = filmStorage.getFilmById(film.getId())
                .map(Film::getLikes)
                .orElse(Set.of());

        assertThat(likes).isEmpty();
    }

    @Test
    void removeLike_shouldNotFailWhenLikeDoesNotExist() {
        Film film = filmStorage.save(createTestFilm());

        //Удаляем лайк, которого нет
        filmStorage.removeLike(film.getId(), 999);

        //Проверяем — ошибок нет, лайков нет
        Optional<Film> result = filmStorage.getFilmById(film.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getLikes()).isEmpty();
    }

    @Test
    void removeLike_shouldNotAffectOtherUsersLikes() {
        Film film = filmStorage.save(createTestFilm());
        filmStorage.addLike(film.getId(), 1);
        filmStorage.addLike(film.getId(), 2);

        //Удаляем только лайк от пользователя 1
        filmStorage.removeLike(film.getId(), 1);

        Set<Integer> likes = filmStorage.getFilmById(film.getId())
                .map(Film::getLikes)
                .orElse(Set.of());

        assertThat(likes).hasSize(2 - 1).contains(2).doesNotContain(1);
    }

    //Вспомогательный метод
    private Film createTestFilm() {
        return Film.builder()
                .name("Test Film")
                .description("Test description")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .build();
    }
}



