package sumdu.edu.ua.web.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sumdu.edu.ua.core.exception.ConflictException;
import sumdu.edu.ua.core.exception.NotFoundException;
import sumdu.edu.ua.core.exception.ValidationException;
import sumdu.edu.ua.web.http.Json;

/**
 * Перехоплює всі винятки та sendError() і повертає уніфікований JSON замість stacktrace.
 * Логування: WARN для 4xx, ERROR для 5xx.
 */
public class ErrorFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(ErrorFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        req.setCharacterEncoding("UTF-8");

        try {
            chain.doFilter(req, new JsonErrorResponse(req, resp));
        } catch (Throwable ex) {
            Throwable cause = unwrap(ex);
            int status = statusOf(cause);
            String message = status >= 500 ? "Internal server error" : messageOf(cause);
            writeError(req, resp, status, message, cause);
        }
    }

    private static Throwable unwrap(Throwable ex) {
        Throwable t = ex;
        while (t instanceof ServletException && t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }

    private static int statusOf(Throwable t) {
        if (t instanceof ValidationException) return 400;
        if (t instanceof JsonProcessingException) return 400;
        if (t instanceof NotFoundException) return 404;
        if (t instanceof ConflictException) return 409;
        if (t instanceof HttpError e) return e.getStatus();
        return 500;
    }

    private static String messageOf(Throwable t) {
        if (t instanceof JsonProcessingException) return "Malformed JSON body";
        return t.getMessage();
    }

    static void writeError(HttpServletRequest req, HttpServletResponse resp,
                           int status, String message, Throwable cause) throws IOException {
        String where = req.getMethod() + " " + req.getRequestURI();
        if (status >= 500) {
            log.error("{} -> {} {}", where, status, message, cause);
        } else {
            log.warn("{} -> {} {}", where, status, message);
        }

        if (resp.isCommitted()) {
            return;
        }
        resp.reset();
        String reason = reasonOf(status);
        Json.write(resp, status, new ErrorBody(Instant.now(), status, reason, message, req.getRequestURI()));
    }

    private static String reasonOf(int status) {
        return switch (status) {
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 409 -> "Conflict";
            case 500 -> "Internal Server Error";
            default -> "Error";
        };
    }

    /** Перетворює виклики sendError() (наприклад, 405 від HttpServlet) на JSON. */
    private static final class JsonErrorResponse extends HttpServletResponseWrapper {
        private final HttpServletRequest req;
        private final HttpServletResponse resp;

        JsonErrorResponse(HttpServletRequest req, HttpServletResponse resp) {
            super(resp);
            this.req = req;
            this.resp = resp;
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            writeError(req, resp, sc, msg, null);
        }

        @Override
        public void sendError(int sc) throws IOException {
            writeError(req, resp, sc, reasonOf(sc), null);
        }
    }
}
