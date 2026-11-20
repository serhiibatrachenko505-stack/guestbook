package sumdu.edu.ua.core.exception;

/** Операція суперечить бізнес-правилу (web перетворює на 409). */
public class ConflictException extends DomainException {
    public ConflictException(String message) {
        super(message);
    }
}
