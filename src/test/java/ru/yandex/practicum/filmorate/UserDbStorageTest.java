package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.db.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        //Очищаем таблицы перед каждым тестом
        jdbcTemplate.update("DELETE FROM friendships");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void addUser_shouldSaveUserAndGenerateId() {
        User user = User.builder()
                .email("test@yandex.ru")
                .login("testuser")
                .name("Test Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User saved = userStorage.addUser(user);

        assertThat(saved.getId()).isPositive();
        assertThat(saved.getEmail()).isEqualTo("test@yandex.ru");
        assertThat(saved.getLogin()).isEqualTo("testuser");
        assertThat(saved.getName()).isEqualTo("Test Name");

        //Проверяем, что запись есть в БД
        Optional<User> fromDb = userStorage.getUserById(saved.getId());
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getEmail()).isEqualTo("test@yandex.ru");
    }

    @Test
    void updateUser_shouldUpdateExistingUser() {
        User user = userStorage.addUser(User.builder()
                .email("old@yandex.ru")
                .login("oldlogin")
                .name("Old Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        user.setEmail("new@yandex.ru");
        user.setLogin("newlogin");
        user.setName("New Name");

        User updated = userStorage.updateUser(user);

        assertThat(updated.getEmail()).isEqualTo("new@yandex.ru");
        assertThat(updated.getLogin()).isEqualTo("newlogin");
        assertThat(updated.getName()).isEqualTo("New Name");

        Optional<User> fromDb = userStorage.getUserById(user.getId());
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getEmail()).isEqualTo("new@yandex.ru");
    }

    @Test
    void updateUser_nonExistingUser_shouldThrowException() {
        User user = User.builder()
                .id(999)
                .email("no@yandex.ru")
                .login("nobody")
                .name("No One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        assertThatThrownBy(() -> userStorage.updateUser(user))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void getUserById_existingUser_shouldReturnUser() {
        User user = userStorage.addUser(User.builder()
                .email("test@yandex.ru")
                .login("testuser")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        Optional<User> result = userStorage.getUserById(user.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@yandex.ru");
    }

    @Test
    void getUserById_nonExistingUser_shouldReturnEmpty() {
        Optional<User> result = userStorage.getUserById(999);

        assertThat(result).isEmpty();
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        userStorage.addUser(User.builder()
                .email("a@yandex.ru")
                .login("a")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());
        userStorage.addUser(User.builder()
                .email("b@yandex.ru")
                .login("b")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        List<User> all = userStorage.getAllUsers();

        assertThat(all).hasSize(2);
    }

    @Test
    void getAllUsers_noUsers_shouldReturnEmptyList() {
        List<User> all = userStorage.getAllUsers();

        assertThat(all).isEmpty();
    }

    @Test
    void containsUser_existingUser_shouldReturnTrue() {
        User user = userStorage.addUser(User.builder()
                .email("test@yandex.ru")
                .login("testuser")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        boolean contains = userStorage.containsUser(user.getId());

        assertThat(contains).isTrue();
    }

    @Test
    void containsUser_nonExistingUser_shouldReturnFalse() {
        boolean contains = userStorage.containsUser(999);

        assertThat(contains).isFalse();
    }

    @Test
    void addFriend_and_getFriends_shouldReturnFriendList() {
        User user1 = userStorage.addUser(createUser("u1@yandex.ru", "u1"));
        User user2 = userStorage.addUser(createUser("u2@yandex.ru", "u2"));

        userStorage.addFriend(user1.getId(), user2.getId());

        Set<User> friends = userStorage.getFriends(user1.getId());

        assertThat(friends).hasSize(1);
        assertThat(friends).extracting("id").containsExactly(user2.getId());
    }

    @Test
    void addFriend_twice_shouldNotDuplicate() {
        User user1 = userStorage.addUser(createUser("u1@yandex.ru", "u1"));
        User user2 = userStorage.addUser(createUser("u2@yandex.ru", "u2"));

        userStorage.addFriend(user1.getId(), user2.getId());
        userStorage.addFriend(user1.getId(), user2.getId()); //Повтор

        Set<User> friends = userStorage.getFriends(user1.getId());

        assertThat(friends).hasSize(1);
    }

    @Test
    void removeFriend_shouldRemoveFriend() {
        User user1 = userStorage.addUser(createUser("u1@yandex.ru", "u1"));
        User user2 = userStorage.addUser(createUser("u2@yandex.ru", "u2"));

        userStorage.addFriend(user1.getId(), user2.getId());
        userStorage.removeFriend(user1.getId(), user2.getId());

        Set<User> friends = userStorage.getFriends(user1.getId());

        assertThat(friends).isEmpty();
    }

    @Test
    void removeFriend_nonExisting_shouldNotFail() {
        User user1 = userStorage.addUser(createUser("u1@yandex.ru", "u1"));
        User user2 = userStorage.addUser(createUser("u2@yandex.ru", "u2"));

        //Удаляем несуществующую дружбу
        userStorage.removeFriend(user1.getId(), user2.getId());
    }

    @Test
    void getCommonFriends_shouldReturnCommonFriends() {
        User user1 = userStorage.addUser(createUser("u1@yandex.ru", "u1"));
        User user2 = userStorage.addUser(createUser("u2@yandex.ru", "u2"));
        User user3 = userStorage.addUser(createUser("u3@yandex.ru", "u3")); // общий друг

        userStorage.addFriend(user1.getId(), user3.getId());
        userStorage.addFriend(user2.getId(), user3.getId());

        Set<User> common = userStorage.getCommonFriends(user1.getId(), user2.getId());

        assertThat(common).hasSize(1);
        assertThat(common).extracting("id").containsExactly(user3.getId());
    }

    @Test
    void getCommonFriends_noCommon_shouldReturnEmpty() {
        User user1 = userStorage.addUser(createUser("u1@yandex.ru", "u1"));
        User user2 = userStorage.addUser(createUser("u2@yandex.ru", "u2"));
        User user3 = userStorage.addUser(createUser("u3@yandex.ru", "u3"));
        User user4 = userStorage.addUser(createUser("u4@yandex.ru", "u4"));

        userStorage.addFriend(user1.getId(), user3.getId());
        userStorage.addFriend(user2.getId(), user4.getId());

        Set<User> common = userStorage.getCommonFriends(user1.getId(), user2.getId());

        assertThat(common).isEmpty();
    }

    //Вспомогательный метод
    private User createUser(String email, String login) {
        return User.builder()
                .email(email)
                .login(login)
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }
}
