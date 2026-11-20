package sumdu.edu.ua.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.exception.ConflictException;
import sumdu.edu.ua.core.exception.NotFoundException;
import sumdu.edu.ua.core.exception.ValidationException;
import sumdu.edu.ua.core.port.CatalogRepositoryPort;
import sumdu.edu.ua.core.port.CommentRepositoryPort;

/** Перевірка бізнес-правил CommentService без БД (порти підмінені in-memory реалізаціями). */
class CommentServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-20T12:00:00Z");

    private FakeComments comments;
    private CommentService service;

    @BeforeEach
    void setUp() {
        comments = new FakeComments();
        service = new CommentService(comments, new FakeBooks(), Clock.fixed(NOW, ZoneOffset.UTC));
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
    }

    static class FakeComments implements CommentRepositoryPort {
        final List<Comment> store = new ArrayList<>();

        public Comment add(long bookId, String author, String text) {
            Comment c = new Comment(store.size() + 1, bookId, author, text, NOW);
            store.add(c);
            return c;
        }
        public Optional<Comment> findById(long bookId, long commentId) {
            return store.stream().filter(c -> c.getId() == commentId && c.getBookId() == bookId).findFirst();
        }
        public Page<Comment> list(long bookId, String author, Instant since, PageRequest request) {
            return new Page<>(store, request, store.size());
        }
        public boolean delete(long bookId, long commentId) {
            return store.removeIf(c -> c.getId() == commentId && c.getBookId() == bookId);
        }
    }
}
