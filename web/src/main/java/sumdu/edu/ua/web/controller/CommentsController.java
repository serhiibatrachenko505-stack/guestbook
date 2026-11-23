package sumdu.edu.ua.web.controller;

import io.javalin.Javalin;
import io.javalin.http.Context;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.exception.ValidationException;
import sumdu.edu.ua.core.service.CommentService;

/**
 * Маршрути відгуків:
 * GET    /api/books/{id}/comments?author=&since=&page=&size=
 * POST   /api/comments                                  {"bookId":1,"author":"..","text":".."} -> 201
 * DELETE /api/books/{bookId}/comments/{commentId}       -> 204 (або 409, якщо старше 24 год)
 */
public class CommentsController {
    private static final Logger log = LoggerFactory.getLogger(CommentsController.class);

    private final CommentService comments;

    public CommentsController(CommentService comments) {
        this.comments = comments;
    }

    public void registerRoutes(Javalin app) {
        app.get("/api/books/{id}/comments", this::listComments);
        app.post("/api/comments", this::addComment);
        app.delete("/api/books/{bookId}/comments/{commentId}", this::deleteComment);
    }

    private void listComments(Context ctx) {
        long bookId = ctx.pathParamAsClass("id", Long.class).get();
        String author = ctx.queryParam("author");
        Instant since = parseSince(ctx.queryParam("since"));
        ctx.json(comments.list(bookId, author, since, Params.pageRequest(ctx, 20)));
    }

    private void addComment(Context ctx) {
        CommentRequest req = ctx.bodyAsClass(CommentRequest.class);
        if (req.bookId() == null) {
            throw new ValidationException("bookId is required");
        }
        Comment saved = comments.add(req.bookId(), req.author(), req.text());
        log.info("Comment created: id={}, bookId={}, author='{}', length={}",
                saved.getId(), saved.getBookId(), saved.getAuthor(), saved.getText().length());
        ctx.status(201).json(saved);
    }

    private void deleteComment(Context ctx) {
        long bookId = ctx.pathParamAsClass("bookId", Long.class).get();
        long commentId = ctx.pathParamAsClass("commentId", Long.class).get();

        Comment deleted = comments.delete(bookId, commentId);   // правило 24 годин — у core
        log.info("Comment deleted: id={}, bookId={}, author='{}'",
                deleted.getId(), deleted.getBookId(), deleted.getAuthor());
        ctx.status(204);
    }

    private static Instant parseSince(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new ValidationException("since must be ISO-8601 instant, e.g. 2026-11-20T10:00:00Z");
        }
    }

    /** Тіло POST /api/comments. */
    public record CommentRequest(Long bookId, String author, String text) {
    }
}
