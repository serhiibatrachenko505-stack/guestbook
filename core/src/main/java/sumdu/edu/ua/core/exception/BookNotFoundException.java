package sumdu.edu.ua.core.exception;

/** Книгу не знайдено. Web перетворює на 404 Not Found. */
public class BookNotFoundException extends NotFoundException {
    public BookNotFoundException(String message) {
        super(message);
    }
}
