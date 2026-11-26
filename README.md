# Лабораторна робота 10. Обробка винятків, AOP та тестування

Каталог книг з відгуками. На прикладі видалення коментаря реалізовано валідацію з власними винятками в core, глобальну обробку винятків у web, AOP-логування сервісного шару та юніт- й інтеграційні тести. Основа — лабораторна 9 (Spring Security).

## Версії

- JDK 17, Maven 3.8.4
- Spring Boot 3.3.4 (Spring AOP + AspectJ-анотації, JUnit 5, Mockito 5)

## Як запустити

```
mvn clean install
mvn -pl web spring-boot:run
```

Тестові акаунти — як у ЛР9: `admin` / `admin` (ADMIN), `Student` / `password` (USER).

## Що реалізовано

### core
- Винятки (`core.exception`):
  - `InvalidCommentDeleteException` — ідентифікатори ≤ 0 або відсутній `createdAt` (400)
  - `CommentTooOldException` — коментар старший за 24 години (400)
  - `BookNotFoundException` — книгу не знайдено (404)
  - `InvalidCommentTextException` — заборонені слова в тексті (400, розширення)
- `CommentService.delete(long bookId, long commentId, Instant createdAt)` — перевірки, потім `repo.delete(bookId, commentId)`
- `CommentService.validateText(String)` — додатковий метод з валідацією (заборонені слова: spam, казино, ставки)

### web
- `error/GlobalApiExceptionHandler` (`@RestControllerAdvice`, найвищий пріоритет) — винятки core → 400/404 + `{"error": "..."}`; для звичайних переходів браузера — redirect назад з повідомленням
- `error/MvcExceptionHandler` (`@ControllerAdvice` для HTML) — GET: сторінка помилки; POST: `redirect:/error-page` з повідомленням
- `controller/CommentsController` — `POST /comments/delete` з параметрами `bookId`, `commentId`, `createdAt` викликає `commentService.delete(...)`
- `config/ServiceConfig` — реєстрація сервісів core як бінів
- `aop/CommentServiceLoggingAspect` — `@Around` для `CommentService.delete(..)`: параметри, час, помилки
- `aop/ServiceTimingAspect` — час виконання всіх методів `sumdu.edu.ua.core.service..*(..)` (DEBUG, повільні — WARN)

## Перевірка вручну

Під admin на сторінці книги «Кобзар» натиснути «Видалити»:
- у свіжого відгуку — відгук видаляється, у консолі `Calling CommentService.delete(...)` і `... finished in N ms`;
- у відгуку Андрія (2 доби тому) — повідомлення про 24 години, у консолі `... failed in N ms: CommentTooOldException`.

Через Postman / requests.http:

```
POST http://localhost:8080/comments/delete?bookId=1&commentId=2&createdAt=2024-01-01T00:00:00Z
→ 400 {"error": "Коментар створено більше ніж 24 години тому і не може бути видалений"}

POST http://localhost:8080/comments/delete?bookId=0&commentId=2&createdAt=2024-01-01T00:00:00Z
→ 400 {"error": "Некоректний ідентифікатор книги або коментаря"}
```

(сторінкові запити потребують входу і CSRF-токена, тому зручніше перевіряти через браузер або тести; REST API `DELETE /api/comments/2?bookId=1` з Basic admin/admin повертає той самий JSON)

## Тести

- core: `CommentServiceTest` (Mockito: `mock`, `assertThrows`, `verifyNoInteractions`, `verify`), `CommentServiceFakePortsTest`, `UserServiceTest`
- web: `CommentDeleteExceptionIT` (`@SpringBootTest(classes = AppInit.class)`, `@MockBean CommentService`), `CommentDeleteFlowIT` (справжній сервіс, перевірка логів аспекту), `PagesMvcTest`, `ApiMvcTest`, `SecurityTest`, ArchUnit
- Surefire налаштовано запускати також класи `*IT`
