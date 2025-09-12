package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.db.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.db.MpaRatingDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, MpaRatingDbStorage.class})
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;

    @Test
    void shouldReturnFilmById_WhenExists() {
        //Подготовка
        Film filmToSave = Film.builder()
                .name("Inception")
                .description("A mind-bending thriller")
                .releaseDate(LocalDate.of(2010, 7, 16))
                .duration(148)
                .mpaRating(MpaRating.builder().id(3).name("PG-13").build())
                .genres(List.of(
                        Genre.builder().id(1).name("Action").build(),
                        Genre.builder().id(2).name("Sci-Fi").build()
                ))
                .build();

        Film savedFilm = filmStorage.save(filmToSave);

        Optional<Film> result = filmStorage.getFilmById(savedFilm.getId());

        //Проверка
        assertThat(result).isPresent();
        Film film = result.get();
        assertThat(film.getName()).isEqualTo("Inception");
        assertThat(film.getDescription()).isEqualTo("A mind-bending thriller");
        assertThat(film.getDuration()).isEqualTo(148);
        assertThat(film.getReleaseDate()).isEqualTo(LocalDate.of(2010, 7, 16));

        assertThat(film.getMpaRating()).isNotNull();
        assertThat(film.getMpaRating().getId()).isEqualTo(3);
        assertThat(film.getMpaRating().getName()).isEqualTo("PG-13");

        assertThat(film.getGenres()).hasSize(2);
        assertThat(film.getGenres())
                .extracting("id")
                .containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void shouldReturnEmpty_WhenFilmNotFound() {
        Optional<Film> result = filmStorage.getFilmById(999);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldAssignId_OnAddFilm() {
        Film film = Film.builder()
                .name("New Film")
                .description("Cool movie")
                .releaseDate(LocalDate.now())
                .duration(120)
                .build();

        Film added = filmStorage.save(film);

        assertThat(added.getId()).isPositive();

        Optional<Film> saved = filmStorage.getFilmById(added.getId());
        assertThat(saved).isPresent();
        assertThat(saved.get().getName()).isEqualTo("New Film");
    }

    @Test
    void shouldUpdateFilm_WhenExists() {
        Film film = Film.builder()
                .name("Old Title")
                .description("Old desc")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(90)
                .build();
        Film added = filmStorage.save(film);

        Film updatedData = Film.builder()
                .id(added.getId())
                .name("Updated Title")
                .description("Updated description")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(150)
                .mpaRating(MpaRating.builder().id(1).name("G").build())
                .build();

        filmStorage.updateFilm(updatedData);

        //Проверка
        Optional<Film> result = filmStorage.getFilmById(added.getId());
        assertThat(result).isPresent();
        Film updated = result.get();
        assertThat(updated.getName()).isEqualTo("Updated Title");
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        assertThat(updated.getDuration()).isEqualTo(150);
        assertThat(updated.getReleaseDate()).isEqualTo(LocalDate.of(2020, 1, 1));
        assertThat(updated.getMpaRating().getId()).isEqualTo(1);
        assertThat(updated.getMpaRating().getName()).isEqualTo("G");
    }

    @Test
    void shouldReplaceGenres_OnUpdate() {
        //Добавляем фильм с жанрами
        Film film = Film.builder()
                .name("Test Film")
                .releaseDate(LocalDate.now())
                .duration(100)
                .genres(List.of(Genre.builder().id(1).name("Action").build()))
                .build();
        Film added = filmStorage.save(film);

        //Обновляем с новыми жанрами
        Film updated = Film.builder()
                .id(added.getId())
                .name("Updated")
                .releaseDate(LocalDate.now())
                .duration(120)
                .genres(List.of(Genre.builder().id(3).name("Comedy").build()))
                .build();
        filmStorage.updateFilm(updated);

        //Проверка
        Optional<Film> result = filmStorage.getFilmById(added.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getGenres())
                .extracting("id")
                .containsExactly(3);
    }

    @Test
    void shouldDeleteFilm_AndAllRelations() {
        Film film = Film.builder()
                .name("To Be Deleted")
                .releaseDate(LocalDate.now())
                .duration(90)
                .build();
        Film added = filmStorage.save(film);

        filmStorage.deleteFilm(added.getId());

        Optional<Film> result = filmStorage.getFilmById(added.getId());
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnAllFilms() {
        filmStorage.save(Film.builder()
                .name("Film 1")
                .releaseDate(LocalDate.now())
                .duration(100)
                .build());

        filmStorage.save(Film.builder()
                .name("Film 2")
                .releaseDate(LocalDate.now())
                .duration(110)
                .build());

        List<Film> all = filmStorage.getAllFilms();

        assertThat(all).hasSize(2);
        assertThat(all)
                .extracting("name")
                .contains("Film 1", "Film 2");
    }

    @Test
    void shouldPersistGenres_WhenAdded() {
        Film film = filmStorage.save(Film.builder()
                .name("Genre Test")
                .releaseDate(LocalDate.now())
                .duration(90)
                .build());

        List<Genre> genres = List.of(
                Genre.builder().id(1).name("Action").build(),
                Genre.builder().id(4).name("Drama").build()
        );

        filmStorage.addGenre(film.getId(), genres);

        Optional<Film> result = filmStorage.getFilmById(film.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getGenres())
                .extracting("id")
                .containsExactlyInAnyOrder(1, 4);
    }
}

