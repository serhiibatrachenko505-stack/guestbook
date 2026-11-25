# Лабораторна робота 6. Thymeleaf

Каталог книг з відгуками з серверним рендерингом сторінок через Thymeleaf. Основа — лабораторна 5 (Spring MVC). Контролери сторінок повертають імена шаблонів і передають дані через `Model`; REST API з лабораторної 5 залишився за адресою `/api/...`. Модулі core і persistence не змінювались.

## Версії

- JDK 17
- Maven 3.8.4
- Spring Boot 3.3.4 (Spring MVC 6.1, Thymeleaf 3.1)
- H2 2.2.224

## Як запустити

```
mvn clean install
mvn -pl web spring-boot:run
```

Відкрити http://localhost:8080/books

## Сторінки

| URL | Шаблон | Що робить |
|---|---|---|
| `GET /books` | `books.html` | список книг (`th:each`), пошук, сортування, сторінки, «Немає книг» (`th:if`/`th:unless`) |
| `GET /books/add` | `book-form.html` | форма додавання книги (`th:object`, `th:field`, `th:errors`) |
| `POST /books/add` | — | збереження, `redirect:/books` |
| `GET /books/{id}` | `book.html` | книга, відгуки, форма відгуку, кнопки видалення |
| `POST /comments` | — | додати відгук |
| `POST /comments/delete` | — | видалити відгук (лише протягом 24 год) |
| помилки | `error.html` | 404 / 400 / невідомі адреси |

Спільні частини (`<head>`, шапка з навігацією і перемикачем мови, підвал) — у `templates/fragments/header.html`, підключаються через `th:replace="~{fragments/header :: header}"`.

## Локалізація

- `messages.properties` — українська (за замовчуванням)
- `messages_en.properties` — англійська

Мова визначається так: параметр `?lang=en` / `?lang=uk` (запам'ятовується в cookie) → мова браузера (`Accept-Language`) → українська.

Файли `messages*.properties` збережені в UTF-8. Якщо редагуєте їх в IntelliJ, увімкніть Settings → Editor → File Encodings → Default encoding for properties files: UTF-8.

## REST API

`GET /api/books`, `GET /api/books/{id}`, `POST /api/books`, `GET /api/comments?bookId=`, `POST /api/comments`, `DELETE /api/comments/{id}?bookId=`, `GET /api/info`. Приклади — у `requests.http`.

## Тести

- `PagesMvcTest` — шаблони, Model, форма (успішна і з помилками), «Немає книг», перемикання мови через `?lang` і `Accept-Language`
- `ApiMvcTest` — REST API
- `ApplicationContextTest` — DispatcherServlet, ThymeleafViewResolver, CookieLocaleResolver, переклади
- ArchUnit — архітектурні правила
