package sumdu.edu.ua.core.exception;

/** Сутність не знайдено (web перетворює на 404). */
public class NotFoundException extends DomainException {
    public NotFoundException(String message) {
        super(message);
    }
}
