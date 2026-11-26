package sumdu.edu.ua.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.exception.BookNotFoundException;
import sumdu.edu.ua.core.exception.CommentTooOldException;
import sumdu.edu.ua.core.exception.InvalidCommentDeleteException;
import sumdu.edu.ua.core.exception.InvalidCommentTextException;
import sumdu.edu.ua.core.port.CatalogRepositoryPort;
import sumdu.edu.ua.core.port.CommentRepositoryPort;

/**
 * Юніт-тести CommentService (JUnit 5 + Mockito).
 * Порт репозиторію — мок, тому перевіряється лише логіка сервісу, без БД.
 */
class CommentServiceTest {

    // ------------------------------------------------------------ delete(bookId, commentId, createdAt)

    @Test
    void delete_withNegativeIds_throwsInvalidCommentDeleteException() {
        CommentRepositoryPort repo = mock(CommentRepositoryPort.class);
        CommentService service = new CommentService(repo);

        assertThrows(InvalidCommentDeleteException.class,
                () -> service.delete(-1, 0, Instant.now()));

        verifyNoInteractions(repo);
    }

    @Test
    void delete_withZeroCommentId_throwsInvalidCommentDeleteException() {
        CommentRepositoryPort repo = mock(CommentRepositoryPort.class);
        CommentService service = new CommentService(repo);

        assertThrows(InvalidCommentDeleteException.class,
                () -> service.delete(1, 0, Instant.now()));

        verifyNoInteractions(repo);
    }

    @Test
    void delete_withoutCreatedAt_throwsInvalidCommentDeleteException() {
        CommentRepositoryPort repo = mock(CommentRepositoryPort.class);
        CommentService service = new CommentService(repo);

        var ex = assertThrows(InvalidCommentDeleteException.class,
                () -> service.delete(1, 1, null));

        assertEquals("Не вказано дату створення коментаря", ex.getMessage());
        verifyNoInteractions(repo);
    }

    @Test
    void delete_tooOldComment_throwsCommentTooOldException() {
        CommentRepositoryPort repo = mock(CommentRepositoryPort.class);
        CommentService service = new CommentService(repo);

        Instant twoDaysAgo = Instant.now().minusSeconds(48 * 3600);

        var ex = assertThrows(CommentTooOldException.class,
                () -> service.delete(1, 1, twoDaysAgo));

        assertEquals("Коментар створено більше ніж 24 години тому і не може бути видалений", ex.getMessage());
        verifyNoInteractions(repo);
    }

    @Test
    void delete_validData_callsRepositoryDelete() {
        CommentRepositoryPort repo = mock(CommentRepositoryPort.class);
        CommentService service = new CommentService(repo);

        Instant oneHourAgo = Instant.now().minusSeconds(3600);

        service.delete(10, 20, oneHourAgo);

        verify(repo).delete(10, 20);
    }

    @Test
    void delete_exactlyAtBoundary_isAllowed() {
        CommentRepositoryPort repo = mock(CommentRepositoryPort.class);
        Instant now = Instant.parse("2026-11-26T12:00:00Z");
        CommentService service = new CommentService(repo, null, null,
                Clock.fixed(now, ZoneOffset.UTC), Duration.ofHours(24));

        service.delete(1, 2, now.minus(Duration.ofHours(24)));     // рівно 24 год — ще можна

        verify(repo).delete(1, 2);
    }

    @Test
    void delete_byIds_takesCreatedAtFromRepository() {
        CommentRepositoryPort repo = mock(CommentRepositoryPort.class);
        CommentService service = new CommentService(repo);
        Comment old = new Comment(2, 1, "Андрій", "text", Instant.now().minus(Duration.ofDays(2)));
        when(repo.findById(1, 2)).thenReturn(Optional.of(old));

        assertThrows(CommentTooOldException.class, () -> service.delete(1, 2));

        verify(repo, never()).delete(anyLong(), anyLong());
    }

    // ------------------------------------------------------------ додатково: validateText / add (+20%)

    @Test
    void validateText_withForbiddenWord_throwsInvalidCommentTextException() {
        CommentService service = new CommentService(mock(CommentRepositoryPort.class));

        var ex = assertThrows(InvalidCommentTextException.class,
                () -> service.validateText("Найкраще КАЗИНО онлайн"));

        assertTrue(ex.getMessage().contains("казино"));
    }

    @Test
    void validateText_withNormalText_returnsTrimmedText() {
        CommentService service = new CommentService(mock(CommentRepositoryPort.class));

        assertEquals("Чудова книга", service.validateText("  Чудова книга  "));
    }

    @Test
    void add_withForbiddenWord_doesNotTouchRepositories() {
        CommentRepositoryPort repo = mock(CommentRepositoryPort.class);
        CatalogRepositoryPort books = mock(CatalogRepositoryPort.class);
        CommentService service = new CommentService(repo, books, null, Clock.systemUTC());

        assertThrows(InvalidCommentTextException.class, () -> service.add(1, "Ann", "buy spam here"));

        verifyNoInteractions(repo, books);
    }

    @Test
    void add_forUnknownBook_throwsBookNotFoundException() {
        CommentRepositoryPort repo = mock(CommentRepositoryPort.class);
        CatalogRepositoryPort books = mock(CatalogRepositoryPort.class);
        when(books.findById(99)).thenReturn(Optional.empty());
        CommentService service = new CommentService(repo, books, null, Clock.systemUTC());

        assertThrows(BookNotFoundException.class, () -> service.add(99, "Ann", "Good book"));

        verifyNoInteractions(repo);
    }
}
