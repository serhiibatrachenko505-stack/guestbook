# Лабораторна робота 2. Трирівнева архітектура

Каталог книг з відгуками. Проєкт розбитий на три Maven-модулі: core (доменна логіка), persistence (робота з БД через JDBC/H2) і web (сервлети). Правила залежностей між модулями перевіряються тестами ArchUnit.

## Версії

- JDK 17
- Maven 3.8.4
- Jetty 11.0.24
- H2 2.2.224
- ArchUnit 1.3.0, JUnit 5.10.2

## Модулі

- `core` — доменні класи (Book, Comment, Page, PageRequest), порти (CatalogRepositoryPort, CommentRepositoryPort), сервіси з бізнес-правилами (BookService, CommentService) і доменні винятки. Не залежить ні від чого.
- `persistence` — реалізація портів через JDBC (JdbcBookRepository, JdbcCommentRepository), підключення до БД (Db) і створення схеми (DbInit, schema.sql, data.sql). Залежить від core.
- `web` — сервлет BooksServlet, фільтр помилок ErrorFilter, HTML-сторінки. Пакет `config` (AppInit, Beans) збирає всі залежності при старті. Сервлети працюють тільки з сервісами з core.

## Як запустити

```
mvn clean install
mvn -pl web jetty:run
```

Перша команда збирає всі модулі і запускає тести (разом з ArchUnit), друга запускає Jetty.
Після запуску відкрити http://localhost:8080/

Зупинити сервер: Ctrl+C

## База даних

- URL: `jdbc:h2:file:./data/library;AUTO_SERVER=TRUE`
- Користувач `sa`, пароль порожній
- Файл: `data/library.mv.db` у корені проєкту
- Таблиці створюються самі при старті, при першому запуску додаються тестові книги і два відгуки (один з них старий, щоб перевірити правило 24 годин).

## Ендпоїнти

- `GET /books?q=&page=&size=&sort=` — список книг з пошуком і пагінацією. sort: `id`, `title`, `author`, `year`, можна з напрямком, наприклад `year,desc`
- `POST /books` — додати книгу (JSON: title, author, pubYear) → 201
- `GET /books/{id}` — книга і її відгуки
- `GET /books/{id}/comments?author=&since=&page=&size=` — відгуки до книги
- `POST /books/{id}/comments` — додати відгук (JSON: author, text) → 201
- `DELETE /books/{id}/comments/{commentId}` — видалити відгук → 204 (або 409, якщо відгуку більше 24 годин)

## Коди відповідей

- 200 / 201 / 204 — успіх
- 400 — неправильні дані або параметри
- 404 — книгу чи відгук не знайдено
- 405 — метод не підтримується
- 409 — відгук старший за 24 години, видалити не можна
- 500 — помилка сервера

Усі помилки повертаються в однаковому JSON-форматі:

```
{"timestamp":"...","status":409,"error":"Conflict","message":"Comment can be deleted only within 24 hours after creation","path":"/books/1/comments/2"}
```

## Логування

- INFO — створення і видалення відгуків (id, книга, автор, довжина тексту)
- WARN — помилки 4xx
- ERROR — помилки 5xx (зі stacktrace)

## Тести

`mvn test` запускає тести ArchUnit у кожному модулі і тест бізнес-правил CommentService. Перевіряється:

- web не залежить від persistence
- core не залежить від Servlet API і JDBC
- сервлети лежать тільки у web
- репозиторії лежать тільки у persistence
- усі класи core мають публічні конструктори
