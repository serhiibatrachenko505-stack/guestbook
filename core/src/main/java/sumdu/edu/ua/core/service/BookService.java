package sumdu.edu.ua.core.service;

import java.time.Year;
import java.util.Set;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.exception.NotFoundException;
import sumdu.edu.ua.core.exception.ValidationException;
import sumdu.edu.ua.core.port.CatalogRepositoryPort;

/** Бізнес-логіка каталогу: валідація, пошук, пагінація. */
public class BookService {
    /** Дозволені поля сортування (логічні імена, не колонки БД). */
    public static final Set<String> SORT_FIELDS = Set.of("id", "title", "author", "year");
    public static final int MAX_TITLE = 255;
    public static final int MAX_AUTHOR = 255;

    private final CatalogRepositoryPort books;

    public BookService(CatalogRepositoryPort books) {
        this.books = books;
    }

    public Page<Book> search(String query, PageRequest request) {
        PageRules.check(request.getPage(), request.getSize());
        if (!SORT_FIELDS.contains(request.getSortBy())) {
            throw new ValidationException("sort must be one of " + SORT_FIELDS);
        }
        String q = (query == null || query.isBlank()) ? null : query.trim();
        return books.search(q, request);
    }

    public Book get(long id) {
        PageRules.checkId(id, "id");
        return books.findById(id)
                .orElseThrow(() -> new NotFoundException("Book " + id + " not found"));
    }

    public Book add(String title, String author, Integer pubYear) {
        String t = required(title, "title", MAX_TITLE);
        String a = required(author, "author", MAX_AUTHOR);
        int currentYear = Year.now().getValue();
        if (pubYear == null || pubYear < 1 || pubYear > currentYear) {
            throw new ValidationException("pubYear must be between 1 and " + currentYear);
        }
        return books.add(t, a, pubYear);
    }

    static String required(String value, String field, int max) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(field + " is required");
        }
        String v = value.trim();
        if (v.length() > max) {
            throw new ValidationException(field + " must be <= " + max + " chars");
        }
        return v;
    }
}
