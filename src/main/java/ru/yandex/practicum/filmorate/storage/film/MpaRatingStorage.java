package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.MpaRating;

public interface MpaRatingStorage {
    MpaRating getMpaRatingById(int id);
    java.util.List<MpaRating> getAllMpaRatings();
}
