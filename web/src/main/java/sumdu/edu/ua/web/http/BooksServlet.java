package sumdu.edu.ua.web.http;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import sumdu.edu.ua.config.LibraryProperties;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.service.BookService;
import sumdu.edu.ua.core.service.CommentService;
import sumdu.edu.ua.web.error.HttpError;

/**
 * HTTP-контракти каталогу:
 * <pre>
 * GET    /books?q=&page=&size=&sort=        список книг (пошук, пагінація, сортування)
 * POST   /books                             додати книгу (JSON)          -> 201
 * GET    /books/{id}                        сторінка книги + перші відгуки
 * GET    /books/{id}/comments?author=&since=&page=&size=
 * POST   /books/{id}/comments               додати відгук (JSON)         -> 201
 * DELETE /books/{id}/comments/{commentId}   видалити відгук (24 години)  -> 204 / 409
 * </pre>
 * Сервлет знає лише сервіси з core і нічого не знає про JDBC.
 * Є бін Spring: залежності приходять через конструктор (constructor injection).
 */
@Component
public class BooksServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(BooksServlet.class);

    private final BookService books;
    private final CommentService comments;
    private final int defaultPageSize;

    public BooksServlet(BookService books, CommentService comments, LibraryProperties props) {
        this.books = books;
        this.comments = comments;
        this.defaultPageSize = props.books().defaultPageSize();
        log.info("BooksServlet created via constructor injection (default page size = {})", defaultPageSize);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String[] p = path(req);
        switch (p.length) {
            case 0 -> listBooks(req, resp);
            case 1 -> bookPage(Params.id(p[0], "id"), resp);
            case 2 -> {
                requireComments(p[1]);
                listComments(Params.id(p[0], "id"), req, resp);
            }
            default -> throw HttpError.notFound("Route not found");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String[] p = path(req);
        if (p.length == 0) {
            addBook(req, resp);
        } else if (p.length == 2) {
            requireComments(p[1]);
            addComment(Params.id(p[0], "id"), req, resp);
        } else {
            throw HttpError.methodNotAllowed("POST");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        String[] p = path(req);
        if (p.length != 3) {
            throw HttpError.methodNotAllowed("DELETE");
        }
        requireComments(p[1]);
        long bookId = Params.id(p[0], "id");
        long commentId = Params.id(p[2], "commentId");

        Comment deleted = comments.delete(bookId, commentId);
        log.info("Comment deleted: id={}, bookId={}, author='{}'",
                deleted.getId(), deleted.getBookId(), deleted.getAuthor());
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    // ---------- handlers ----------

    private void listBooks(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PageRequest page = Params.pageRequest(req, defaultPageSize);
        Json.write(resp, 200, books.search(req.getParameter("q"), page));
    }

    private void bookPage(long id, HttpServletResponse resp) throws IOException {
        Book book = books.get(id);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("book", book);
        body.put("comments", comments.list(id, null, null, new PageRequest(0, 20)));
        Json.write(resp, 200, body);
    }

    private void listComments(long bookId, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PageRequest page = Params.pageRequest(req, 20);
        Json.write(resp, 200, comments.list(bookId, req.getParameter("author"),
                Params.instantParam(req, "since"), page));
    }

    private void addBook(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        BookForm form = Json.read(req, BookForm.class);
        Book saved = books.add(form.title(), form.author(), form.pubYear());
        log.info("Book created: id={}, title='{}'", saved.getId(), saved.getTitle());
        resp.setHeader("Location", req.getContextPath() + "/books/" + saved.getId());
        Json.write(resp, HttpServletResponse.SC_CREATED, saved);
    }

    private void addComment(long bookId, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CommentForm form = Json.read(req, CommentForm.class);
        Comment saved = comments.add(bookId, form.author(), form.text());
        log.info("Comment created: id={}, bookId={}, author='{}', length={}",
                saved.getId(), bookId, saved.getAuthor(), saved.getText().length());
        Json.write(resp, HttpServletResponse.SC_CREATED, saved);
    }

    // ---------- routing helpers ----------

    /** "/5/comments/7" -> ["5", "comments", "7"]; null або "/" -> []. */
    private static String[] path(HttpServletRequest req) {
        String info = req.getPathInfo();
        if (info == null || info.equals("/") || info.isBlank()) {
            return new String[0];
        }
        String trimmed = info.startsWith("/") ? info.substring(1) : info;
        if (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed.split("/");
    }

    private static void requireComments(String segment) {
        if (!"comments".equals(segment)) {
            throw HttpError.notFound("Route not found");
        }
    }
}
