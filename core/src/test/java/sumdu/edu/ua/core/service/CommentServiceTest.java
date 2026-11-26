package sumdu.edu.ua.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.domain.User;
import sumdu.edu.ua.core.domain.UserCredentials;
import sumdu.edu.ua.core.exception.ConflictException;
import sumdu.edu.ua.core.exception.NotFoundException;
import sumdu.edu.ua.core.exception.ValidationException;
import sumdu.edu.ua.core.port.CatalogRepositoryPort;
import sumdu.edu.ua.core.port.CommentRepositoryPort;
import sumdu.edu.ua.core.port.UserRepositoryPort;

/** Перевірка бізнес-правил без БД (порти підмінені in-memory реалізаціями). */
class CommentServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-20T12:00:00Z");

    private FakeComments comments;
    private FakeUsers users;
    private CommentService service;

    @BeforeEach
    void setUp() {
        comments = new FakeComments();
        users = new FakeUsers();
        service = new CommentService(comments, new FakeBooks(), new UserService(users),
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void freshCommentCanBeDeleted() {
        comments.store.add(new Comment(1, 1, "Ann", "Hi", NOW.minus(Duration.ofHours(23))));
        service.delete(1, 1);
        assertTrue(comments.store.isEmpty());
    }

    @Test
    void commentOlderThan24HoursCannotBeDeleted() {
        comments.store.add(new Comment(1, 1, "Ann", "Hi", NOW.minus(Duration.ofHours(25))));
        assertThrows(ConflictException.class, () -> service.delete(1, 1));
        assertEquals(1, comments.store.size());
    }

    @Test
    void deletingMissingCommentGivesNotFound() {
        assertThrows(NotFoundException.class, () -> service.delete(1, 42));
    }

    @Test
    void emptyAuthorIsRejected() {
        assertThrows(ValidationException.class, () -> service.add(1, "  ", "text"));
    }

    @Test
    void tooLongTextIsRejected() {
        assertThrows(ValidationException.class, () -> service.add(1, "Ann", "x".repeat(1001)));
    }

    @Test
    void commentForUnknownBookGivesNotFound() {
        assertThrows(NotFoundException.class, () -> service.add(999, "Ann", "text"));
    }

    @Test
    void nonPositiveIdsAreRejected() {
        assertThrows(ValidationException.class, () -> service.add(0, "Ann", "text"));
        assertThrows(ValidationException.class, () -> service.delete(1, -5));
    }

    @Test
    void newAuthorBecomesUserAndCommentIsLinked() {
        Comment c = service.add(1, "Ann", "Great");
        assertNotNull(c.getUserId());
        assertEquals("Ann", users.store.get(0).getUsername());
        assertEquals(1, service.getCommentsByUser(c.getUserId()).size());
    }

    @Test
    void existingUserIsReused() {
        service.add(1, "Ann", "First");
        service.add(1, "Ann", "Second");
        assertEquals(1, users.store.size());
        assertEquals(2, service.getCommentsByUser(users.store.get(0).getId()).size());
    }

    @Test
    void commentsOfUnknownUserGiveNotFound() {
        assertThrows(NotFoundException.class, () -> service.getCommentsByUser(77));
    }

    // ---- in-memory підміни портів ----

    static class FakeBooks implements CatalogRepositoryPort {
        public Page<Book> search(String query, PageRequest request) {
            return new Page<>(List.of(), request, 0);
        }
        public Optional<Book> findById(long id) {
            return id == 1 ? Optional.of(new Book(1, "Kobzar", "Shevchenko", 1840)) : Optional.empty();
        }
        public Book add(String title, String author, int pubYear) {
            return new Book(2, title, author, pubYear);
        }
        public boolean delete(long id) {
            return id == 1;
        }
    }

    static class FakeUsers implements UserRepositoryPort {
        final List<User> store = new ArrayList<>();
        final Map<String, User> codes = new HashMap<>();
        final Map<String, String> hashes = new HashMap<>();

        public Optional<User> findById(long id) {
            return store.stream().filter(u -> u.getId() == id).findFirst();
        }
        public Optional<User> findByUsername(String username) {
            return store.stream().filter(u -> u.getUsername().equals(username)).findFirst();
        }
        public List<User> findAll() {
            return store;
        }
        public User create(String username, String role) {
            User u = new User(store.size() + 1, username, role);
            store.add(u);
            return u;
        }
        public Optional<UserCredentials> findCredentials(String username) {
            return Optional.empty();
        }
        public boolean existsByEmail(String email) {
            return store.stream().anyMatch(u -> email.equals(u.getEmail()));
        }
        public User register(String username, String email, String hash, String role, String code) {
            User u = new User(store.size() + 1, username, email, role, false);
            store.add(u);
            codes.put(code, u);
            hashes.put(username, hash);
            return u;
        }
        public Optional<User> confirm(String code) {
            User u = codes.remove(code);
            return Optional.ofNullable(u);
        }
        public void updatePassword(String username, String hash) {
            hashes.put(username, hash);
        }
    }

    static class FakeComments implements CommentRepositoryPort {
        final List<Comment> store = new ArrayList<>();

        public Comment add(long bookId, Long userId, String author, String text) {
            Comment c = new Comment(store.size() + 1, bookId, userId, author, text, NOW);
            store.add(c);
            return c;
        }
        public Optional<Comment> findById(long bookId, long commentId) {
            return store.stream().filter(c -> c.getId() == commentId && c.getBookId() == bookId).findFirst();
        }
        public Page<Comment> list(long bookId, String author, Instant since, PageRequest request) {
            return new Page<>(store, request, store.size());
        }
        public List<Comment> findByUser(long userId) {
            return store.stream().filter(c -> c.getUserId() != null && c.getUserId() == userId).toList();
        }
        public boolean delete(long bookId, long commentId) {
            return store.removeIf(c -> c.getId() == commentId && c.getBookId() == bookId);
        }
    }
}
