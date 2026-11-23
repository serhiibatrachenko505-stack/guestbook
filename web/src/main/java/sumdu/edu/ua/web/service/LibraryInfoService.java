package sumdu.edu.ua.web.service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import sumdu.edu.ua.config.LibraryProperties;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.service.BookService;
import sumdu.edu.ua.core.service.CommentService;

/**
 * Сервіс рівня web, позначений @Service.
 * Збирає довідкову інформацію про застосунок з параметрів (LibraryProperties) і сервісів core.
 * Залежності — через конструктор.
 */
@Service
public class LibraryInfoService {
    private final BookService books;
    private final CommentService comments;
    private final LibraryProperties props;
    private final Instant startedAt = Instant.now();

    public LibraryInfoService(BookService books, CommentService comments, LibraryProperties props) {
        this.books = books;
        this.comments = comments;
        this.props = props;
    }

    public Map<String, Object> info() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("title", props.title());
        info.put("version", props.version());
        info.put("booksTotal", books.search(null, new PageRequest(0, 1)).getTotal());
        info.put("defaultPageSize", props.books().defaultPageSize());
        info.put("deleteWindowHours", comments.getDeleteWindow().toHours());
        info.put("startedAt", startedAt);
        return info;
    }
}
