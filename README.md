# Лабораторна робота 4. Spring Core & Boot

Каталог книг з відгуками, переведений на Spring Boot. Основа — лабораторна 2 (модулі core / persistence / web, сервлети). Тепер об'єкти створює і зв'язує між собою контейнер Spring (IoC), а параметри задаються в `application.yaml`.

Гілка: `lab4-spring`

## Версії

- JDK 17
- Maven 3.8.4
- Spring Boot 3.3.4 (вбудований Tomcat 10.1)
- H2 2.2.224
- ArchUnit 1.3.0, JUnit 5

## Як запустити

```
mvn clean install
mvn -pl web spring-boot:run
```

Або після збирання: `java -jar web/target/web-1.0.0-SNAPSHOT.jar`, або кнопкою Run у класі `AppInit` в IntelliJ.

Після запуску відкрити http://localhost:8080/

## Біни

| Бін | Як створюється | Модуль |
|---|---|---|
| `JdbcBookRepository`, `JdbcCommentRepository` | `@Repository`, знаходяться сканером | persistence |
| `SchemaInitializer` | `@Component`, `@PostConstruct` створює схему БД | persistence |
| `BookService`, `CommentService` | `@Bean` у `AppConfig` (core без анотацій Spring) | web/config |
| `Clock` | кастомний `@Bean` у `AppConfig` | web/config |
| `LibraryProperties` | `@ConfigurationProperties(prefix = "library")` | web/config |
| `LibraryInfoService` | `@Service` | web |
| `BooksServlet` | `@Component`, ін'єкція через конструктор | web |
| `InfoServlet` | `@Component`, ін'єкція через поле (`@Autowired`, `@Value`) | web |
| `ServletConfig` | `@Configuration`, реєструє сервлети і фільтр замість web.xml | web/config |
| `BeansReport` | `@Component`, після старту виводить список бінів у лог | web/config |

## Параметри (application.yaml)

```yaml
spring.application.name: library-app
server.port: 8080
library:
  title: "Каталог книг"
  version: "4.0"
  books.default-page-size: 10
  comments.delete-window-hours: 24
```

Наприклад, якщо змінити `delete-window-hours` на 48, правило видалення відгуків зміниться без зміни коду.

## База даних

- URL: `jdbc:h2:file:./data/library;AUTO_SERVER=TRUE`
- Файл: `data/library.mv.db` у корені проєкту

## Ендпоїнти

- `GET /` — сторінка каталогу
- `GET /info` — інформація про застосунок (з application.yaml)
- `GET /books?q=&page=&size=&sort=` — список книг
- `GET /books/{id}` — книга і відгуки
- `POST /books` — додати книгу
- `POST /books/{id}/comments` — додати відгук
- `DELETE /books/{id}/comments/{commentId}` — видалити відгук (204 / 409)

## Тести

`mvn test`:
- ArchUnit у кожному модулі (core не залежить від Spring, Servlet API і JDBC; web не залежить від persistence; класи `@Configuration` лежать у пакеті config)
- `ApplicationContextTest` (`@SpringBootTest`) — піднімає контекст і перевіряє, що всі біни створені, зв'язані між собою і отримали параметри з application.yaml
