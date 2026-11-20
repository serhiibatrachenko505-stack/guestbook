package sumdu.edu.ua.web.error;

import java.time.Instant;

/** Уніфікований JSON-формат помилки. */
public record ErrorBody(Instant timestamp, int status, String error, String message, String path) {
}
