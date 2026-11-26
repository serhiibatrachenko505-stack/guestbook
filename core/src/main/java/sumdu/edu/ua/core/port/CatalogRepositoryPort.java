package sumdu.edu.ua.core.port;

import java.util.Optional;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;

/** Контракт доступу до каталогу книг. Реалізація — у модулі persistence. */
public interface CatalogRepositoryPort {

    Page<Book> search(String query, PageRequest request);

    Optional<Book> findById(long id);

    Book add(String title, String author, int pubYear);

    boolean delete(long id);
}
