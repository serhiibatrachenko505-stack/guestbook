package sumdu.edu.ua.core.exception;

/** Некоректні вхідні дані (web перетворює на 400). */
public class ValidationException extends DomainException {
    public ValidationException(String message) {
        super(message);
    }
}
