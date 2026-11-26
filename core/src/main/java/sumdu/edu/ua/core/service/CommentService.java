package sumdu.edu.ua.core.service;

import jakarta.transaction.Transactional;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.domain.User;
import sumdu.edu.ua.core.exception.ConflictException;
import sumdu.edu.ua.core.exception.NotFoundException;
import sumdu.edu.ua.core.port.CatalogRepositoryPort;
import sumdu.edu.ua.core.port.CommentRepositoryPort;

/**
 * Бізнес-логіка відгуків: валідація, прив'язка до користувача,
 * правило видалення протягом 24 годин (за замовчуванням).
 * Транзакції позначено стандартною анотацією jakarta.transaction.Transactional —
 * Spring її підтримує, а core при цьому не залежить від Spring.
 */
public class CommentService {
    public static final int MAX_AUTHOR = 64;
    public static final int MAX_TEXT = 1000;
    /** Відгук можна видалити лише протягом цього часу після створення. */
    public static final Duration DELETE_WINDOW = Duration.ofHours(24);

    private final CommentRepositoryPort comments;
    private final CatalogRepositoryPort books;
    private final UserService users;
    private final Clock clock;
    private final Duration deleteWindow;

    public CommentService(CommentRepositoryPort comments, CatalogRepositoryPort books,
                          UserService users, Clock clock, Duration deleteWindow) {
        this.comments = comments;
        this.books = books;
        this.users = users;
        this.clock = clock;
        this.deleteWindow = deleteWindow;
    }

    public CommentService(CommentRepositoryPort comments, CatalogRepositoryPort books,
                          UserService users, Clock clock) {
        this(comments, books, users, clock, DELETE_WINDOW);
    }

    /**
     * Додає відгук. Автор відгуку стає користувачем системи:
     * існуючий користувач з таким іменем або новий (роль USER).
     */
    @Transactional
    public Comment add(long bookId, String author, String text) {
        requireBook(bookId);
        String a = BookService.required(author, "author", MAX_AUTHOR);
        String t = BookService.required(text, "text", MAX_TEXT);
        User user = users.findOrCreate(a);
        return comments.add(bookId, user.getId(), a, t);
    }

    public Page<Comment> list(long bookId, String author, Instant since, PageRequest request) {
        requireBook(bookId);
        PageRules.check(request.getPage(), request.getSize());
        String a = (author == null || author.isBlank()) ? null : author.trim();
        return comments.list(bookId, a, since, request);
    }

    /** Усі відгуки користувача, нові першими. */
    public List<Comment> getCommentsByUser(long userId) {
        users.findById(userId);                       // NotFoundException, якщо користувача немає
        return comments.findByUser(userId);
    }

    /**
     * Видаляє відгук, якщо з моменту створення минуло не більше deleteWindow (типово 24 години).
     * @return видалений відгук (для логування)
     */
    @Transactional
    public Comment delete(long bookId, long commentId) {
        PageRules.checkId(bookId, "bookId");
        PageRules.checkId(commentId, "commentId");
        Comment comment = comments.findById(bookId, commentId)
                .orElseThrow(() -> new NotFoundException(
                        "Comment " + commentId + " for book " + bookId + " not found"));

        Duration age = Duration.between(comment.getCreatedAt(), Instant.now(clock));
        if (age.compareTo(deleteWindow) > 0) {
            throw new ConflictException("Comment can be deleted only within "
                    + deleteWindow.toHours() + " hours after creation");
        }

        comments.delete(bookId, commentId);
        return comment;
    }

    public Duration getDeleteWindow() {
        return deleteWindow;
    }

    private void requireBook(long bookId) {
        PageRules.checkId(bookId, "bookId");
        if (books.findById(bookId).isEmpty()) {
            throw new NotFoundException("Book " + bookId + " not found");
        }
    }
}
