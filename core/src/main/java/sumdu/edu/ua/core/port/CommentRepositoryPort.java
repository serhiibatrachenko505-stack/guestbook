package sumdu.edu.ua.core.port;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;

/** Контракт доступу до відгуків. Реалізація — у модулі persistence (JPA). */
public interface CommentRepositoryPort {

    Comment add(long bookId, Long userId, String author, String text);

    Optional<Comment> findById(long bookId, long commentId);

    Page<Comment> list(long bookId, String author, Instant since, PageRequest request);

    List<Comment> findByUser(long userId);

    boolean delete(long bookId, long commentId);
}
