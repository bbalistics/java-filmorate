# java-filmorate
Template repository for Filmorate project.

## Схема базы данных
https://github.com/bbalistics/java-filmorate/tree/main/diagram.png

---

##  Примеры SQL-запросов

### 1. Получить топ-5 самых популярных фильмов по количеству лайков

```sql
SELECT f.id, f.name, f.description, COUNT(fl.user_id) AS likes
FROM films f
LEFT JOIN film_likes fl ON f.id = fl.film_id
GROUP BY f.id
ORDER BY likes DESC
LIMIT 5;
```

### 2. Все друзья пользователя с id = 1

```sql
SELECT u.id, u.email, u.login, u.name, u.birthday
FROM users u
INNER JOIN friendships fs ON u.id = fs.friend_id
WHERE fs.user_id = 1;
```
