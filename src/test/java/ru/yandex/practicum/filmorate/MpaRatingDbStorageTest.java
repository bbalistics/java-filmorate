package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.db.MpaRatingDbStorage;
import ru.yandex.practicum.filmorate.storage.db.UserDbStorage;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, MpaRatingDbStorage.class})
class MpaRatingDbStorageTest {

    @Autowired
    private MpaRatingDbStorage mpaRatingStorage;

    @Test
    void shouldFindMpaRatingById() {
        MpaRating rating = mpaRatingStorage.getMpaRatingById(1);

        assertThat(rating).hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "G");
    }

    @Test
    void shouldReturnAllMpaRatings() {
        List<MpaRating> ratings = mpaRatingStorage.getAllMpaRatings();

        assertThat(ratings).hasSize(5)
                .extracting("id", "name")
                .containsExactly(
                        tuple(1, "G"),
                        tuple(2, "PG"),
                        tuple(3, "PG-13"),
                        tuple(4, "R"),
                        tuple(5, "NC-17")
                );
    }
}
