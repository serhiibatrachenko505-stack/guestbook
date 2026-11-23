package sumdu.edu.ua.web.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sumdu.edu.ua.core.exception.ConflictException;
import sumdu.edu.ua.core.exception.NotFoundException;
import sumdu.edu.ua.core.exception.ValidationException;

/**
 * Централізована обробка винятків через app.exception().
 * Доменні винятки з core перетворюються на HTTP-коди; формат відповіді — ErrorBody.
 * Логування: WARN для 4xx, ERROR для 5xx.
 */
public final class ErrorHandlers {
    private static final Logger log = LoggerFactory.getLogger(ErrorHandlers.class);

    private ErrorHandlers() {}

    public static void register(Javalin app) {
        app.exception(ValidationException.class, (e, ctx) -> respond(ctx, 400, e.getMessage(), null));
        app.exception(NotFoundException.class, (e, ctx) -> respond(ctx, 404, e.getMessage(), null));
        app.exception(ConflictException.class, (e, ctx) -> respond(ctx, 409, e.getMessage(), null));

        // некоректний JSON у тілі запиту (ctx.bodyAsClass)
        app.exception(JsonProcessingException.class, (e, ctx) -> respond(ctx, 400, "Malformed JSON body", null));

        // помилки валідаторів Javalin (pathParamAsClass / queryParamAsClass)
        app.exception(io.javalin.validation.ValidationException.class,
                (e, ctx) -> respond(ctx, 400, "Invalid parameter(s): " + e.getErrors().keySet(), null));

        // усе інше — 500
        app.exception(Exception.class, (e, ctx) -> respond(ctx, 500, "Internal server error", e));
    }

    private static void respond(Context ctx, int status, String message, Exception cause) {
        String where = ctx.method() + " " + ctx.path();
        if (status >= 500) {
            log.error("{} -> {} {}", where, status, message, cause);
        } else {
            log.warn("{} -> {} {}", where, status, message);
        }
        ctx.status(status).json(new ErrorBody(Instant.now(), status, reason(status), message, ctx.path()));
    }

    private static String reason(int status) {
        return switch (status) {
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 409 -> "Conflict";
            default -> "Internal Server Error";
        };
    }
}
