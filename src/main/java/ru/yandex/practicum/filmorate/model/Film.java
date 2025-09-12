package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List; // ← Меняем импорт
import java.util.ArrayList;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Film {
    private Integer id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;

    @JsonProperty("mpa")
    private MpaRating mpaRating;

    @JsonProperty("genres")
    @Builder.Default
    private List<Genre> genres = new ArrayList<>();

    @Builder.Default
    private Set<Integer> likes = new HashSet<>();
}
