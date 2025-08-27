package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
class FilmorateApplicationTests {
	private FilmController filmController;
	private UserController userController;

	@BeforeEach
	void setUp() {
		filmController = new FilmController();
		userController = new UserController();
	}

	@Test
	void filmDescription_Boundary199Symbols_ShouldBeValid() {
		Film film = createValidFilm();
		film.setDescription("A".repeat(199));

		try {
			filmController.addFilm(film);
		} catch (ValidationException e) {
			fail("Не должно было выбросить ValidationException для 199 символов");
		}
	}

	@Test
	void filmDescription_Boundary200Symbols_ShouldBeValid() {
		Film film = createValidFilm();
		film.setDescription("A".repeat(200));

		try {
			filmController.addFilm(film);
		} catch (ValidationException e) {
			fail("Не должно было выбросить ValidationException для 200 символов");
		}
	}

	@Test
	void filmDescription_Boundary201Symbols_ShouldThrowException() {
		Film film = createValidFilm();
		film.setDescription("A".repeat(201));

		assertThrows(ValidationException.class, new org.junit.jupiter.api.function.Executable() {
			@Override
			public void execute() {
				filmController.addFilm(film);
			}
		});
	}

	@Test
	void filmReleaseDate_BoundaryMinDate_ShouldBeValid() {
		Film film = createValidFilm();
		film.setReleaseDate(LocalDate.of(1895, 12, 28));

		try {
			filmController.addFilm(film);
		} catch (ValidationException e) {
			fail("Не должно было выбросить ValidationException для минимальной даты");
		}
	}

	@Test
	void filmReleaseDate_BoundaryDayBeforeMin_ShouldThrowException() {
		Film film = createValidFilm();
		film.setReleaseDate(LocalDate.of(1895, 12, 27));

		assertThrows(ValidationException.class, new org.junit.jupiter.api.function.Executable() {
			@Override
			public void execute() {
				filmController.addFilm(film);
			}
		});
	}

	@Test
	void userBirthday_BoundaryToday_ShouldBeValid() {
		User user = createValidUser();
		user.setBirthday(LocalDate.now());

		try {
			userController.createUser(user);
		} catch (ValidationException e) {
			fail("Не должно было выбросить ValidationException для сегодняшней даты");
		}
	}

	@Test
	void userBirthday_BoundaryTomorrow_ShouldThrowException() {
		User user = createValidUser();
		user.setBirthday(LocalDate.now().plusDays(1));

		assertThrows(ValidationException.class, new org.junit.jupiter.api.function.Executable() {
			@Override
			public void execute() {
				userController.createUser(user);
			}
		});
	}

	@Test
	void userLogin_BoundaryWithSpace_ShouldThrowException() {
		User user = createValidUser();
		user.setLogin("user name");

		assertThrows(ValidationException.class, new org.junit.jupiter.api.function.Executable() {
			@Override
			public void execute() {
				userController.createUser(user);
			}
		});
	}

	@Test
	void userLogin_BoundaryWithoutSpace_ShouldBeValid() {
		User user = createValidUser();
		user.setLogin("username");

		try {
			userController.createUser(user);
		} catch (ValidationException e) {
			fail("Не должно было выбросить ValidationException для логина без пробелов");
		}
	}

	private Film createValidFilm() {
		Film film = new Film();
		film.setName("Test Film");
		film.setDescription("Test");
		film.setReleaseDate(LocalDate.of(2020, 1, 1));
		film.setDuration(120);
		return film;
	}

	private User createValidUser() {
		User user = new User();
		user.setEmail("test@example.com");
		user.setLogin("testuser");
		user.setName("Test User");
		user.setBirthday(LocalDate.of(1990, 1, 1));
		return user;
	}
}
