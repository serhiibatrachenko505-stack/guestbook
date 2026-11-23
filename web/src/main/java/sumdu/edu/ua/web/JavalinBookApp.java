package sumdu.edu.ua.web;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sumdu.edu.ua.config.Beans;
import sumdu.edu.ua.web.controller.BooksController;
import sumdu.edu.ua.web.controller.CommentsController;
import sumdu.edu.ua.web.error.ErrorHandlers;
import sumdu.edu.ua.web.json.Json;

/** Точка входу: створює Javalin, реєструє middleware, маршрути й обробники помилок. */
public class JavalinBookApp {
    private static final Logger log = LoggerFactory.getLogger(JavalinBookApp.class);
    private static final int PORT = 8080;

    public static void main(String[] args) {
        Beans.init(); // схема БД + репозиторії + сервіси (core/persistence без змін)

        Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.jsonMapper(new JavalinJackson(Json.mapper(), false));
            config.staticFiles.add("/public", Location.CLASSPATH); // index.html, book.html
        });

        // ---- middleware ----
        app.before(ctx -> log.info("{} {}", ctx.method(), ctx.path()));
        app.after(ctx -> log.debug("{} {} -> {}", ctx.method(), ctx.path(), ctx.statusCode()));

        // ---- маршрути ----
        new BooksController(Beans.bookService()).registerRoutes(app);
        new CommentsController(Beans.commentService()).registerRoutes(app);

        // ---- централізована обробка винятків ----
        ErrorHandlers.register(app);

        app.start(PORT);
        log.info("App started at: http://localhost:{}/", PORT);
    }
}
