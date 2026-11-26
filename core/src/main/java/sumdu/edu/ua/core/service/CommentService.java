package sumdu.edu.ua.core.service;

import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.domain.User;
import sumdu.edu.ua.core.exception.BookNotFoundException;
import sumdu.edu.ua.core.exception.CommentTooOldException;
import sumdu.edu.ua.core.exception.InvalidCommentDeleteException;
import sumdu.edu.ua.core.exception.InvalidCommentTextException;
import sumdu.edu.ua.core.exception.NotFoundException;
import sumdu.edu.ua.core.port.CatalogRepositoryPort;
import sumdu.edu.ua.core.port.CommentRepositoryPort;

/**
 * Бізнес-логіка відгуків. Принцип «спочатку перевірка — потім виконання»:
 * порт репозиторію викликається лише після успішної валідації, а порушення правил
 * повідомляються власними винятками (пакет core.exception).
 */
public class CommentService {
    public static final int MAX_AUTHOR = 64;
    public static final int MAX_TEXT = 1000;
    /** Відгук можна видалити лише протягом цього часу після створення. */
    public static final Duration DELETE_WINDOW = Duration.ofHours(24);
    /** Слова, заборонені в тексті відгуку (перевірка без урахування регістру). */
    public static final Set<String> FORBIDDEN_WORDS = Set.of("spam", "казино", "ставки");

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

    /** Мінімальний конструктор (лише порт відгуків) — для юніт-тестів методу delete. */
    public CommentService(CommentRepositoryPort comments) {
        this(comments, null, null, Clock.systemUTC(), DELETE_WINDOW);
    }

    // ------------------------------------------------------------------ додавання

    /**
     * Додає відгук. Автор відгуку стає користувачем системи:
     * існуючий користувач з таким іменем або новий (роль USER).
     */
    @Transactional
    public Comment add(long bookId, String author, String text) {
        String a = BookService.required(author, "author", MAX_AUTHOR);
        String t = validateText(text);                 // спершу дешеві перевірки, потім БД
        requireBook(bookId);
        User user = users.findOrCreate(a);
        return comments.add(bookId, user.getId(), a, t);
    }

    /**
     * Перевірка тексту відгуку: обов'язковий, не довший за 1000 символів,
     * без заборонених слів (InvalidCommentTextException).
     */
    public String validateText(String text) {
        String t = BookService.required(text, "text", MAX_TEXT);
        String lower = t.toLowerCase(Locale.ROOT);
        for (String word : FORBIDDEN_WORDS) {
            if (lower.contains(word)) {
                throw new InvalidCommentTextException("Текст коментаря містить заборонене слово: " + word);
            }
        }
        return t;
    }

    // ------------------------------------------------------------------ читання

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

    // ------------------------------------------------------------------ видалення

    /**
     * Видалення коментаря з перевірками:
     *  1) bookId > 0 і commentId > 0, інакше InvalidCommentDeleteException;
     *  2) createdAt задано, інакше InvalidCommentDeleteException;
     *  3) коментар не старший за deleteWindow (24 год), інакше CommentTooOldException;
     *  лише після цього викликається порт репозиторію.
     */
    @Transactional
    @RolesAllowed("ADMIN")
    public void delete(long bookId, long commentId, Instant createdAt) {
        if (bookId <= 0 || commentId <= 0) {
            throw new InvalidCommentDeleteException("Некоректний ідентифікатор книги або коментаря");
        }
        if (createdAt == null) {
            throw new InvalidCommentDeleteException("Не вказано дату створення коментаря");
        }
        Duration age = Duration.between(createdAt, Instant.now(clock));
        if (age.compareTo(deleteWindow) > 0) {
            throw new CommentTooOldException("Коментар створено більше ніж " + deleteWindow.toHours()
                    + " години тому і не може бути видалений");
        }
        comments.delete(bookId, commentId);
    }

    /**
     * Видалення за ідентифікаторами (REST API): дата створення береться з БД,
     * а не від клієнта, далі — ті самі перевірки, що й у delete(bookId, commentId, createdAt).
     * @return видалений відгук (для логування)
     */
    @Transactional
    @RolesAllowed("ADMIN")
    public Comment delete(long bookId, long commentId) {
        if (bookId <= 0 || commentId <= 0) {
            throw new InvalidCommentDeleteException("Некоректний ідентифікатор книги або коментаря");
        }
        Comment comment = comments.findById(bookId, commentId)
                .orElseThrow(() -> new NotFoundException(
                        "Comment " + commentId + " for book " + bookId + " not found"));
        delete(bookId, commentId, comment.getCreatedAt());
        return comment;
    }

    public Duration getDeleteWindow() {
        return deleteWindow;
    }

    private void requireBook(long bookId) {
        PageRules.checkId(bookId, "bookId");
        if (books.findById(bookId).isEmpty()) {
            throw new BookNotFoundException("Книгу з id=" + bookId + " не знайдено");
        }
    }
}
