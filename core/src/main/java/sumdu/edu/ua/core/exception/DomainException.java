package sumdu.edu.ua.core.exception;

/** Базовий виняток доменного шару. Не знає нічого про HTTP-коди. */
public abstract class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
