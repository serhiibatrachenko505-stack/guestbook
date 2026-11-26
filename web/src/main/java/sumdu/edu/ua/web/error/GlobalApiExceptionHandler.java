package sumdu.edu.ua.web.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.RequestContextUtils;
import sumdu.edu.ua.core.exception.BookNotFoundException;
import sumdu.edu.ua.core.exception.CommentTooOldException;
import sumdu.edu.ua.core.exception.InvalidCommentDeleteException;
import sumdu.edu.ua.core.exception.InvalidCommentTextException;

/**
 * Глобальний обробник винятків сервісного шару для всіх контролерів:
 *   InvalidCommentDeleteException -> 400 {"error": "..."}
 *   CommentTooOldException        -> 400 {"error": "..."}
 *   InvalidCommentTextException   -> 400 {"error": "..."}
 *   BookNotFoundException         -> 404 {"error": "..."}
 *
 * Має найвищий пріоритет серед @ControllerAdvice, тому ці винятки обробляються саме тут.
 * Якщо запит надіслав браузер зі сторінки (Accept: text/html), замість JSON виконується
 * перенаправлення назад зі flash-повідомленням — щоб користувач не бачив «сирий» JSON.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalApiExceptionHandler.class);

    @ExceptionHandler(InvalidCommentDeleteException.class)
    public ResponseEntity<Map<String, String>> handleInvalidDelete(
            InvalidCommentDeleteException ex, HttpServletRequest req, HttpServletResponse resp) {
        log.info("Invalid comment delete request: {}", ex.getMessage());
        return respond(HttpStatus.BAD_REQUEST, ex.getMessage(), "error.invalidDelete", req, resp);
    }

    @ExceptionHandler(CommentTooOldException.class)
    public ResponseEntity<Map<String, String>> handleTooOld(
            CommentTooOldException ex, HttpServletRequest req, HttpServletResponse resp) {
        log.info("Comment too old to delete: {}", ex.getMessage());
        return respond(HttpStatus.BAD_REQUEST, ex.getMessage(), "error.commentTooOld", req, resp);
    }

    @ExceptionHandler(InvalidCommentTextException.class)
    public ResponseEntity<Map<String, String>> handleInvalidText(
            InvalidCommentTextException ex, HttpServletRequest req, HttpServletResponse resp) {
        log.info("Invalid comment text: {}", ex.getMessage());
        return respond(HttpStatus.BAD_REQUEST, ex.getMessage(), "error.forbiddenWords", req, resp);
    }

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleBookNotFound(
            BookNotFoundException ex, HttpServletRequest req, HttpServletResponse resp) {
        log.warn("Book not found: {}", ex.getMessage());
        return respond(HttpStatus.NOT_FOUND, ex.getMessage(), "error.notFound", req, resp);
    }

    // -----------------------------------------------------------------------------------------

    private ResponseEntity<Map<String, String>> respond(HttpStatus status, String message, String messageKey,
                                                        HttpServletRequest req, HttpServletResponse resp) {
        if (fromBrowserPage(req)) {
            String back = backUrl(req);
            FlashMap flash = RequestContextUtils.getOutputFlashMap(req);
            flash.put("error", messageKey);
            RequestContextUtils.saveOutputFlashMap(back, req, resp);
            return ResponseEntity.status(HttpStatus.SEE_OTHER).location(URI.create(back)).build();
        }
        return ResponseEntity.status(status).body(Map.of("error", message));
    }

    /** Звичайна навігація браузера (не fetch / Postman / REST API). */
    private static boolean fromBrowserPage(HttpServletRequest req) {
        String accept = req.getHeader("Accept");
        return accept != null && accept.contains("text/html") && !req.getRequestURI().startsWith("/api/");
    }

    private static String backUrl(HttpServletRequest req) {
        String referer = req.getHeader("Referer");
        return (referer == null || referer.isBlank()) ? "/books" : referer;
    }
}
