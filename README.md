# Лабораторна робота 9. Spring Security

Каталог книг з відгуками, захищений Spring Security: вхід через форму, реєстрація з підтвердженням email, ролі USER і ADMIN, захищений REST API. Основа — лабораторна 8 (JPA, Hibernate, Flyway).

## Версії

- JDK 17, Maven 3.8.4
- Spring Boot 3.3.4 (Spring Security 6.3)
- H2 2.2.224

## Як запустити

```
mvn clean install
mvn -pl web spring-boot:run
```

Відкрити http://localhost:8080/ — гостя буде перенаправлено на сторінку входу.

## Тестові акаунти

| Логін | Пароль | Роль |
|---|---|---|
| admin | admin | ADMIN |
| Student | password | USER |
| Олена | password | USER |
| Андрій | password | USER |

Паролі тестових акаунтів зберігаються у V2 як `{noop}...`; після першого входу Spring Security автоматично перехешує їх у BCrypt (`{bcrypt}$2a$10$...`).

Новий акаунт: http://localhost:8080/register → лист із посиланням `/confirm?code=...` (якщо пошта не налаштована, посилання є в консолі: `Confirmation link for '...': ...`).

## Права доступу

| Ресурс | Гість | USER | ADMIN |
|---|---|---|---|
| `/login`, `/register`, `/confirm`, статичні файли | ✓ | ✓ | ✓ |
| `GET /books/**`, `GET /users/**` | → /login | ✓ | ✓ |
| `POST /comments` (додати відгук) | → /login | ✓ | ✓ |
| `/books/add`, `POST /books/{id}/delete`, `POST /comments/delete` | → /login | 403 | ✓ |
| `GET /api/**` | 401 | ✓ | ✓ |
| `POST/DELETE /api/**` | 401 | 403 | ✓ |
| `/h2-console` | → /login | 403 | ✓ |

REST API використовує HTTP Basic (у Postman: Authorization → Basic Auth). Приклади — у `requests.http`.

## Безпека в коді

- `config/SecurityConfig` — два ланцюжки фільтрів: `/api/**` (Basic, stateless, 401/403 у JSON) і сторінки (форма входу, сесія, CSRF, `/403`); `PasswordEncoder` (BCrypt); `@EnableMethodSecurity(jsr250Enabled = true)`
- `web/security/AppUserDetailsService` — власний `UserDetailsService` (користувачі з БД) і `UserDetailsPasswordService` (оновлення хешу)
- `web/security/SecurityAuditListener` — лог входів, невдалих спроб і виходів
- `core/service/BookService`, `CommentService` — `@RolesAllowed("ADMIN")` на видаленні та додаванні книг
- `web/controller/BooksController.deleteBook` — `@PreAuthorize("hasRole('ADMIN')")`
- `V3__security.sql` — email, enabled, confirmation_code

## Тести

- core: реєстрація, підтвердження, правила відгуків
- persistence: реєстрація/підтвердження в БД, каскадне видалення книги
- web: `SecurityTest` — гість (redirect / 401), USER (403 на адмін-діях, немає адмін-кнопок), ADMIN, вхід, оновлення пароля до BCrypt, реєстрація з підтвердженням, вихід
