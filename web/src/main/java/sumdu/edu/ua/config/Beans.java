package sumdu.edu.ua.config;

import sumdu.edu.ua.core.port.CatalogRepositoryPort;
import sumdu.edu.ua.core.port.CommentRepositoryPort;
import sumdu.edu.ua.core.service.BookService;
import sumdu.edu.ua.core.service.CommentService;
import sumdu.edu.ua.persistence.jdbc.DbInit;
import sumdu.edu.ua.persistence.jdbc.JdbcBookRepository;
import sumdu.edu.ua.persistence.jdbc.JdbcCommentRepository;

/**
 * Фабрика залежностей (composition root).
 * Єдине місце у web-модулі, яке знає про конкретні JDBC-реалізації.
 * Сервлети отримують звідси лише сервіси з core.
 */
public final class Beans {
    private static BookService bookService;
    private static CommentService commentService;

    private Beans() {}

    public static synchronized void init() {
        DbInit.init();

        CatalogRepositoryPort bookRepo = new JdbcBookRepository();
        CommentRepositoryPort commentRepo = new JdbcCommentRepository();

        bookService = new BookService(bookRepo);
        commentService = new CommentService(commentRepo, bookRepo);
    }

    public static BookService bookService() {
        return bookService;
    }

    public static CommentService commentService() {
        return commentService;
    }
}
