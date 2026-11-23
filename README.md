# Лабораторна робота 5. Spring MVC

Каталог книг з відгуками на Spring MVC. Основа — лабораторна 4 (Spring Boot). Сервлети замінено на контролери `@RestController`, маршрутизацію виконує DispatcherServlet, відповіді повертаються у форматі JSON. Модульна структура core / persistence / web збережена.

## Версії

- JDK 17
- Maven 3.8.4
- Spring Boot 3.3.4 (Spring MVC 6.1, вбудований Tomcat 10.1)
- H2 2.2.224
- ArchUnit 1.3.0, JUnit 5

## Як запустити

```
mvn clean install
mvn -pl web spring-boot:run
```

Або кнопкою Run у класі `AppInit` в IntelliJ. Після запуску відкрити http://localhost:8080/

## Структура web

- `config/WebConfig` — конфігурація Spring MVC з `@EnableWebMvc` (JSON, перехоплювач запитів, статичні сторінки)
- `config/AppConfig` — біни сервісів core (`@Bean`)
- `web/controller/BooksController`, `CommentsController`, `InfoController` — REST-контролери
- `web/dto` — об'єкти запитів і відповідей (`BookRequest`, `CommentRequest`, `BookDetails`)
- `web/error/GlobalExceptionHandler` — обробка помилок (`@RestControllerAdvice`)
- `web/RequestLoggingInterceptor` — логування: який метод контролера обробляє запит

## Ендпоїнти

| Метод | URL | Опис | Відповідь |
|---|---|---|---|
| GET | `/books?q=&page=&size=&sort=` | список книг | 200 |
| GET | `/books/{id}` | книга з відгуками | 200 / 400 / 404 |
| POST | `/books` | додати книгу (JSON: title, author, pubYear) | 201 / 400 |
| GET | `/comments?bookId=` | відгуки до книги | 200 / 404 |
| POST | `/comments` | додати відгук (JSON: bookId, author, text) | 201 / 400 / 404 |
| DELETE | `/comments/{id}?bookId=` | видалити відгук | 204 / 404 / 409 |
| GET | `/info` | інформація про застосунок | 200 |

Ті самі контролери доступні також за `/api/books` і `/api/comments`.

Готові запити — у файлі `requests.http` (IntelliJ HTTP Client / VS Code REST Client) або в Postman.

## Помилки

Усі помилки повертаються у форматі:

```
{"timestamp":"...","status":404,"error":"Not Found","message":"Book 999 not found","path":"/books/999"}
```

400 — неправильні дані, 404 — не знайдено, 405 — метод не підтримується, 409 — відгук старший за 24 години, 500 — внутрішня помилка.

## Тести

`mvn test`:
- `ControllersMvcTest` — запити через MockMvc (GET /books, GET /books/1, POST /comments, 400, 404, 409)
- `ApplicationContextTest` — DispatcherServlet зареєстровано, контролери і сервіси створені
- ArchUnit — контролери лежать у web.controller, сервлетів немає, web не залежить від persistence, core не залежить від Spring
