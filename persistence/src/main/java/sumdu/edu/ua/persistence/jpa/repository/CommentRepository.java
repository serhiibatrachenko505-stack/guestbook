package sumdu.edu.ua.persistence.jpa.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import sumdu.edu.ua.persistence.jpa.entity.CommentEntity;

/**
 * Відгуки. Методи з назвами findBy... Spring Data перетворює на SQL автоматично,
 * а JpaSpecificationExecutor дозволяє будувати запит з необов'язковими фільтрами.
 */
public interface CommentRepository
        extends JpaRepository<CommentEntity, Long>, JpaSpecificationExecutor<CommentEntity> {

    /** Відгуки користувача: ... where user_id = ? order by created_at desc. */
    List<CommentEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<CommentEntity> findByIdAndBookId(Long id, Long bookId);
}
