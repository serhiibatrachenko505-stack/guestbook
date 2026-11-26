package sumdu.edu.ua.core.exception;

/**
 * Некоректні дані для видалення коментаря: ідентифікатори <= 0 або відсутня дата створення.
 * Web перетворює на 400 Bad Request.
 */
public class InvalidCommentDeleteException extends ValidationException {
    public InvalidCommentDeleteException(String message) {
        super(message);
    }
}
