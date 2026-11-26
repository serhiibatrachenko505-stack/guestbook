package sumdu.edu.ua.core.exception;

/**
 * Коментар створено раніше, ніж дозволяє правило видалення (типово 24 години).
 * Web перетворює на 400 Bad Request.
 */
public class CommentTooOldException extends DomainException {
    public CommentTooOldException(String message) {
        super(message);
    }
}
