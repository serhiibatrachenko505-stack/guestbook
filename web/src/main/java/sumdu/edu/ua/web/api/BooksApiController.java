package sumdu.edu.ua.web.api;

import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sumdu.edu.ua.config.LibraryProperties;
import sumdu.edu.ua.web.SortParam;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.service.BookService;
import sumdu.edu.ua.core.service.CommentService;
import sumdu.edu.ua.web.dto.BookDetails;
import sumdu.edu.ua.web.dto.BookRequest;

/**
 * REST API каталогу (JSON). HTML-сторінки віддає BooksController з пакета web.controller.
 */
@RestController
@RequestMapping("/api/books")
public class BooksApiController {
    private static final Logger log = LoggerFactory.getLogger(BooksApiController.class);

    private final BookService books;
    private final CommentService comments;
    private final int defaultPageSize;

    public BooksApiController(BookService books, CommentService comments, LibraryProperties props) {
        this.books = books;
        this.comments = comments;
        this.defaultPageSize = props.books().defaultPageSize();
    }

    /** GET /api/books?q=&page=&size=&sort= — список книг. */
    @GetMapping
    public ResponseEntity<Page<Book>> getBooks(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", required = false) Integer size,
            @RequestParam(name = "sort", required = false) String sort) {
        int pageSize = size != null ? size : defaultPageSize;
        PageRequest request = SortParam.toPageRequest(page, pageSize, sort);
        return ResponseEntity.ok(books.search(q, request));
    }

    /** GET /api/books/{id} — книга з відгуками. */
    @GetMapping("/{id}")
    public ResponseEntity<BookDetails> getBook(@PathVariable("id") long id) {
        Book book = books.get(id);   // NotFoundException -> 404 (GlobalExceptionHandler)
        var bookComments = comments.list(id, null, null, new PageRequest(0, 20));
        return ResponseEntity.ok(new BookDetails(book, bookComments));
    }

    /** POST /api/books — додати книгу, 201 Created. */
    @PostMapping
    public ResponseEntity<Book> addBook(@RequestBody BookRequest req) {
        Book saved = books.add(req.title(), req.author(), req.pubYear());
        log.info("Book created: id={}, title='{}'", saved.getId(), saved.getTitle());
        return ResponseEntity.created(URI.create("/api/books/" + saved.getId())).body(saved);
    }
}
