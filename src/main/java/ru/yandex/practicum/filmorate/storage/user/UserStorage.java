package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserStorage {
    User addUser(User user);

    User updateUser(User user);

    Optional<User> getUserById(Integer id);

    List<User> getAllUsers();

    boolean containsUser(Integer id);

    void addFriend(int userId, int friendId);

    void removeFriend(Integer userId, Integer friendId);

    Set<User> getFriends(int userId);

    Set<User> getCommonFriends(int userId1, int userId2);
}