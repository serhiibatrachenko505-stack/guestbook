package sumdu.edu.ua.web.error;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import sumdu.edu.ua.core.exception.ConflictException;
import sumdu.edu.ua.core.exception.NotFoundException;
import sumdu.edu.ua.core.exception.ValidationException;

/**
 * Централізована обробка помилок для всіх контролерів.
 * Доменні винятки з core і помилки Spring MVC -> HTTP-статус + JSON у форматі ErrorBody.
 * WARN для 4xx, ERROR для 5xx.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---- доменні винятки з core ----

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorBody> validation(ValidationException e, HttpServletRequest req) {
        return error(HttpStatus.BAD_REQUEST, e.getMessage(), req, null);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorBody> notFound(NotFoundException e, HttpServletRequest req) {
        return error(HttpStatus.NOT_FOUND, e.getMessage(), req, null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorBody> conflict(ConflictException e, HttpServletRequest req) {
        return error(HttpStatus.CONFLICT, e.getMessage(), req, null);
    }

    // ---- помилки Spring MVC ----

    /** Некоректний або відсутній JSON у @RequestBody. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorBody> badJson(HttpMessageNotReadableException e, HttpServletRequest req) {
        return error(HttpStatus.BAD_REQUEST, "Malformed JSON body", req, null);
    }

    /** /books/abc — параметр не того типу. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorBody> typeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest req) {
        return error(HttpStatus.BAD_REQUEST, e.getName() + " has invalid value '" + e.getValue() + "'", req, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorBody> missingParam(MissingServletRequestParameterException e, HttpServletRequest req) {
        return error(HttpStatus.BAD_REQUEST, "Parameter '" + e.getParameterName() + "' is required", req, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorBody> methodNotAllowed(HttpRequestMethodNotSupportedException e, HttpServletRequest req) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "Method " + e.getMethod() + " is not supported here", req, null);
    }

    /** Невідомий шлях (немає ні контролера, ні статичного файлу). */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorBody> noRoute(NoResourceFoundException e, HttpServletRequest req) {
        return error(HttpStatus.NOT_FOUND, "Route not found", req, null);
    }

    // ---- усе інше ----

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorBody> internal(Exception e, HttpServletRequest req) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", req, e);
    }

    private ResponseEntity<ErrorBody> error(HttpStatus status, String message,
                                            HttpServletRequest req, Exception cause) {
        String where = req.getMethod() + " " + req.getRequestURI();
        if (status.is5xxServerError()) {
            log.error("{} -> {} {}", where, status.value(), message, cause);
        } else {
            log.warn("{} -> {} {}", where, status.value(), message);
        }
        ErrorBody body = new ErrorBody(Instant.now(), status.value(), status.getReasonPhrase(),
                message, req.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
