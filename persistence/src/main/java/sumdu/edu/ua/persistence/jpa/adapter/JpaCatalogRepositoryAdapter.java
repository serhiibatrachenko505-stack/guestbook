package sumdu.edu.ua.persistence.jpa.adapter;

import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.port.CatalogRepositoryPort;
import sumdu.edu.ua.persistence.jpa.entity.BookEntity;
import sumdu.edu.ua.persistence.jpa.repository.BookRepository;

/** Реалізація порту каталогу через Spring Data JPA (замість JdbcBookRepository). */
@Component
@Transactional(readOnly = true)
public class JpaCatalogRepositoryAdapter implements CatalogRepositoryPort {

    /** Логічні поля сортування core -> поля сутності BookEntity. */
    private static final Map<String, String> SORT_FIELDS = Map.of(
            "id", "id", "title", "title", "author", "author", "year", "pubYear");

    private final BookRepository repo;

    public JpaCatalogRepositoryAdapter(BookRepository repo) {
        this.repo = repo;
    }

    @Override
    public Page<Book> search(String query, PageRequest request) {
        Sort.Direction dir = request.isDescending() ? Sort.Direction.DESC : Sort.Direction.ASC;
        String field = SORT_FIELDS.getOrDefault(request.getSortBy(), "id");
        var pageable = org.springframework.data.domain.PageRequest.of(
                request.getPage(), request.getSize(), Sort.by(dir, field).and(Sort.by(dir, "id")));

        var page = (query == null || query.isBlank())
                ? repo.findAll(pageable)
                : repo.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(query, query, pageable);

        return new Page<>(page.map(Mappers::toDomain).getContent(), request, page.getTotalElements());
    }

    @Override
    public Optional<Book> findById(long id) {
        return repo.findById(id).map(Mappers::toDomain);
    }

    @Override
    @Transactional
    public boolean delete(long id) {
        if (!repo.existsById(id)) {
            return false;
        }
        repo.deleteById(id);          // відгуки видаляються каскадом (CascadeType.ALL + ON DELETE CASCADE)
        return true;
    }

    @Override
    @Transactional
    public Book add(String title, String author, int pubYear) {
        BookEntity saved = repo.save(new BookEntity(title, author, pubYear));
        return Mappers.toDomain(saved);
    }
}
