package sumdu.edu.ua.web.controller;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.service.BookService;

/**
 * Маршрути каталогу:
 * GET  /api/books?q=&page=&size=&sort=   список книг
 * GET  /api/books/{id}                   одна книга
 * POST /api/books                        додати книгу -> 201
 */
public class BooksController {
    private static final Logger log = LoggerFactory.getLogger(BooksController.class);

    private final BookService books;

    public BooksController(BookService books) {
        this.books = books;
    }

    public void registerRoutes(Javalin app) {
        app.get("/api/books", this::getBooks);
        app.get("/api/books/{id}", this::getBook);
        app.post("/api/books", this::addBook);
    }

    private void getBooks(Context ctx) {
        String q = ctx.queryParam("q");                               // query-параметр (може бути null)
        PageRequest page = Params.pageRequest(ctx, 10);
        ctx.json(books.search(q, page));                              // 200 + JSON
    }

    private void getBook(Context ctx) {
        long id = ctx.pathParamAsClass("id", Long.class).get();      // path-параметр з перевіркою типу
        Book book = books.get(id);                                    // NotFoundException -> 404
        ctx.json(book);
    }

    private void addBook(Context ctx) {
        BookRequest req = ctx.bodyAsClass(BookRequest.class);         // JSON -> Java-об'єкт
        Book saved = books.add(req.title(), req.author(), req.pubYear());
        log.info("Book created: id={}, title='{}'", saved.getId(), saved.getTitle());
        ctx.header("Location", "/api/books/" + saved.getId());
        ctx.status(201).json(saved);
    }

    /** Тіло POST /api/books. */
    public record BookRequest(String title, String author, Integer pubYear) {
    }
}
