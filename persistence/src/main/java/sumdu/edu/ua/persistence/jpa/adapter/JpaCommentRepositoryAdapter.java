package sumdu.edu.ua.persistence.jpa.adapter;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.port.CommentRepositoryPort;
import sumdu.edu.ua.persistence.jpa.entity.CommentEntity;
import sumdu.edu.ua.persistence.jpa.repository.BookRepository;
import sumdu.edu.ua.persistence.jpa.repository.CommentRepository;
import sumdu.edu.ua.persistence.jpa.repository.UserRepository;

/** Реалізація порту відгуків через Spring Data JPA (замість JdbcCommentRepository). */
@Component
@Transactional(readOnly = true)
public class JpaCommentRepositoryAdapter implements CommentRepositoryPort {

    private final CommentRepository comments;
    private final BookRepository books;
    private final UserRepository users;

    public JpaCommentRepositoryAdapter(CommentRepository comments, BookRepository books, UserRepository users) {
        this.comments = comments;
        this.books = books;
        this.users = users;
    }

    @Override
    @Transactional
    public Comment add(long bookId, Long userId, String author, String text) {
        // getReferenceById не робить SELECT: потрібен лише зовнішній ключ
        var book = books.getReferenceById(bookId);
        var user = userId == null ? null : users.getReferenceById(userId);
        CommentEntity saved = comments.save(new CommentEntity(book, user, author, text));
        return Mappers.toDomain(saved);
    }

    @Override
    public Optional<Comment> findById(long bookId, long commentId) {
        return comments.findByIdAndBookId(commentId, bookId).map(Mappers::toDomain);
    }

    @Override
    public Page<Comment> list(long bookId, String author, Instant since, PageRequest request) {
        Specification<CommentEntity> spec = (root, q, cb) -> cb.equal(root.get("book").get("id"), bookId);
        if (author != null) {
            spec = spec.and((root, q, cb) ->
                    cb.like(cb.lower(root.<String>get("author")), "%" + author.toLowerCase() + "%"));
        }
        if (since != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.<Instant>get("createdAt"), since));
        }
        var pageable = org.springframework.data.domain.PageRequest.of(request.getPage(), request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        var page = comments.findAll(spec, pageable);
        return new Page<>(page.map(Mappers::toDomain).getContent(), request, page.getTotalElements());
    }

    @Override
    public List<Comment> findByUser(long userId) {
        return comments.findByUserIdOrderByCreatedAtDesc(userId).stream().map(Mappers::toDomain).toList();
    }

    @Override
    @Transactional
    public boolean delete(long bookId, long commentId) {
        return comments.findByIdAndBookId(commentId, bookId)
                .map(c -> {
                    comments.delete(c);
                    return true;
                })
                .orElse(false);
    }
}
