# Лабораторна робота 8. JPA, Hibernate, Flyway

Каталог книг з відгуками. Доступ до даних переведено з JDBC на JPA/Hibernate і Spring Data JPA, схемою БД керує Flyway. Додано сутність користувача та сторінку з усіма коментарями користувача. Основа — лабораторна 7 (Thymeleaf + листи FreeMarker).

## Версії

- JDK 17, Maven 3.8.4
- Spring Boot 3.3.4 (Hibernate 6.5, Spring Data JPA 3.3, Flyway 10)
- H2 2.2.224

## Як запустити

```
mvn clean install
mvn -pl web spring-boot:run
```

- застосунок: http://localhost:8080/books
- користувачі: http://localhost:8080/users
- H2 Console: http://localhost:8080/h2-console
  (JDBC URL `jdbc:h2:file:./data/library-jpa;AUTO_SERVER=TRUE`, користувач `sa`, пароль порожній)

Для листів (ЛР7) потрібна змінна середовища `MAIL_APP_PASSWORD`; без неї книга зберігається, а в лог пишеться попередження.

## Модулі

- **core** — доменні моделі `Book`, `Comment`, `User`; порти `CatalogRepositoryPort`, `CommentRepositoryPort`, `UserRepositoryPort`; сервіси `BookService`, `CommentService`, `UserService` (транзакції — `jakarta.transaction.Transactional`). Не залежить від Spring, JPA і JDBC.
- **persistence** — JPA:
  - `jpa/entity` — `BookEntity`, `UserEntity`, `CommentEntity`
  - `jpa/repository` — `BookRepository`, `UserRepository`, `CommentRepository` (Spring Data `JpaRepository`)
  - `jpa/adapter` — адаптери, що реалізують порти core через репозиторії
  - `resources/db/migration` — `V1__init.sql` (схема), `V2__seed_data.sql` (тестові дані)
- **web** — Spring MVC + Thymeleaf; `UserController` (`/users`, `/users/{id}/comments`); `config/JpaConfig` — де шукати сутності й репозиторії.

## Зв'язки

```
books 1 ──< comments >── 1 users
```

- `BookEntity.comments` — `@OneToMany(mappedBy = "book", cascade = ALL, orphanRemoval = true, fetch = LAZY)`
- `UserEntity.comments` — `@OneToMany(mappedBy = "user", fetch = LAZY)`
- `CommentEntity.book` / `CommentEntity.user` — `@ManyToOne(fetch = LAZY)` + `@JoinColumn`

У БД: `comments.book_id → books.id ON DELETE CASCADE`, `comments.user_id → users.id ON DELETE SET NULL`.

## Сторінки

| URL | Опис |
|---|---|
| `/books` | список книг |
| `/books/{id}` | книга з відгуками; ім'я автора веде на сторінку його коментарів |
| `/users` | користувачі та кількість їхніх відгуків |
| `/users/{id}/comments` | усі коментарі користувача з посиланнями на книги |

Автор нового відгуку автоматично стає користувачем (знаходиться за іменем або створюється з роллю USER).

## Тести

- core: правила сервісів (in-memory порти), ArchUnit
- persistence: `JpaRepositoriesTest` (`@DataJpaTest`, Flyway на H2 в пам'яті), ArchUnit (немає `java.sql` у жодному модулі, сутності лише в persistence)
- web: сторінки, REST API, контекст (JPA, Flyway, адаптери), ArchUnit
