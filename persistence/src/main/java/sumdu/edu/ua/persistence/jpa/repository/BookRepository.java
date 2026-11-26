package sumdu.edu.ua.persistence.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import sumdu.edu.ua.persistence.jpa.entity.BookEntity;

/** CRUD для книг генерує Spring Data; пошук — derived query з назви методу. */
public interface BookRepository extends JpaRepository<BookEntity, Long> {

    Page<BookEntity> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
            String title, String author, Pageable pageable);
}
