# Лабораторна робота 3. Javalin

Каталог книг з відгуками. У цій лабораторній веб-рівень переписаний з Servlet API на мікрофреймворк Javalin. Модулі core і persistence залишились такими самими, як у лабораторній 2, змінився тільки модуль web.

Гілка: `lab3-javalin`

## Версії

- JDK 17
- Maven 3.8.4
- Javalin 6.2.0 (всередині вбудований Jetty 11)
- H2 2.2.224
- Jackson 2.17.2
- ArchUnit 1.3.0, JUnit 5.10.2

## Модулі

- `core` — доменні класи, порти і сервіси з бізнес-правилами (без змін)
- `persistence` — JDBC-репозиторії і робота з H2 (без змін)
- `web` — застосунок на Javalin:
  - `JavalinBookApp` — клас з методом main(), створює Javalin, додає middleware, маршрути і обробники помилок, запускає сервер
  - `controller/BooksController`, `controller/CommentsController` — маршрути
  - `error/ErrorHandlers` — централізована обробка винятків через app.exception()
  - `resources/public` — HTML-сторінки, які звертаються до API через fetch()

## Як запустити

```
mvn clean install
mvn -pl web exec:java
```

Після запуску відкрити http://localhost:8080/

Зупинити: Ctrl+C

## База даних

- URL: `jdbc:h2:file:./data/library;AUTO_SERVER=TRUE`
- Файл: `data/library.mv.db` у корені проєкту

## Маршрути

| Метод | URL | Опис | Відповідь |
|---|---|---|---|
| GET | `/api/books?q=&page=&size=&sort=` | список книг з пошуком, сортуванням і пагінацією | 200 |
| GET | `/api/books/{id}` | одна книга | 200 / 404 |
| POST | `/api/books` | додати книгу (JSON: title, author, pubYear) | 201 / 400 |
| GET | `/api/books/{id}/comments?author=&since=` | відгуки до книги | 200 / 404 |
| POST | `/api/comments` | додати відгук (JSON: bookId, author, text) | 201 / 400 / 404 |
| DELETE | `/api/books/{bookId}/comments/{commentId}` | видалити відгук | 204 / 404 / 409 |

Готові запити для перевірки лежать у файлі `requests.http` (їх можна відкрити в IntelliJ IDEA або VS Code REST Client, або повторити в Postman).

## Middleware і помилки

- `app.before()` пише в лог кожен запит, наприклад `INFO GET /api/books`
- `app.exception()` перетворює винятки на JSON-відповіді: 400 (неправильні дані), 404 (не знайдено), 409 (відгук старший за 24 години), 500 (інша помилка)

Приклад помилки:

```
{"timestamp":"...","status":404,"error":"Not Found","message":"Book 999 not found","path":"/api/books/999"}
```

## Тести

`mvn test` запускає ArchUnit-тести у всіх модулях. У web додатково перевіряється, що Javalin використовується тільки у веб-рівні, а контролери лежать у пакеті web.controller.
