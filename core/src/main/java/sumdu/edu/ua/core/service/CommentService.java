package sumdu.edu.ua.core.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.exception.ConflictException;
import sumdu.edu.ua.core.exception.NotFoundException;
import sumdu.edu.ua.core.port.CatalogRepositoryPort;
import sumdu.edu.ua.core.port.CommentRepositoryPort;

/** Бізнес-логіка відгуків: валідація і правило видалення протягом 24 годин. */
public class CommentService {
    public static final int MAX_AUTHOR = 64;
    public static final int MAX_TEXT = 1000;
    /** Відгук можна видалити лише протягом цього часу після створення. */
    public static final Duration DELETE_WINDOW = Duration.ofHours(24);

    private final CommentRepositoryPort comments;
    private final CatalogRepositoryPort books;
    private final Clock clock;

    public CommentService(CommentRepositoryPort comments, CatalogRepositoryPort books, Clock clock) {
        this.comments = comments;
        this.books = books;
        this.clock = clock;
    }

    public CommentService(CommentRepositoryPort comments, CatalogRepositoryPort books) {
        this(comments, books, Clock.systemUTC());
    }

    public Comment add(long bookId, String author, String text) {
        requireBook(bookId);
        String a = BookService.required(author, "author", MAX_AUTHOR);
        String t = BookService.required(text, "text", MAX_TEXT);
        return comments.add(bookId, a, t);
    }

    public Page<Comment> list(long bookId, String author, Instant since, PageRequest request) {
        requireBook(bookId);
        PageRules.check(request.getPage(), request.getSize());
        String a = (author == null || author.isBlank()) ? null : author.trim();
        return comments.list(bookId, a, since, request);
    }

    /**
     * Видаляє відгук, якщо з моменту створення минуло не більше 24 годин.
     * @return видалений відгук (для логування)
     */
    public Comment delete(long bookId, long commentId) {
        Comment comment = comments.findById(bookId, commentId)
                .orElseThrow(() -> new NotFoundException(
                        "Comment " + commentId + " for book " + bookId + " not found"));

        Duration age = Duration.between(comment.getCreatedAt(), Instant.now(clock));
        if (age.compareTo(DELETE_WINDOW) > 0) {
            throw new ConflictException(
                    "Comment can be deleted only within 24 hours after creation");
        }

        comments.delete(bookId, commentId);
        return comment;
    }

    private void requireBook(long bookId) {
        if (books.findById(bookId).isEmpty()) {
            throw new NotFoundException("Book " + bookId + " not found");
        }
    }
}
