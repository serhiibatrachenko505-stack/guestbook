package sumdu.edu.ua.core.exception;

/** Текст коментаря порушує правила (заборонені слова). Web перетворює на 400 Bad Request. */
public class InvalidCommentTextException extends ValidationException {
    public InvalidCommentTextException(String message) {
        super(message);
    }
}
