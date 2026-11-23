package sumdu.edu.ua.core.service;

import sumdu.edu.ua.core.exception.ValidationException;

/** Спільні правила для параметрів пагінації. */
public class PageRules {
    public static final int MAX_PAGE_SIZE = 100;

    public PageRules() {
    }

    /** Ідентифікатор сутності має бути додатним числом. */
    public static void checkId(long id, String name) {
        if (id <= 0) {
            throw new ValidationException(name + " must be a positive number");
        }
    }

    public static void check(int page, int size) {
        if (page < 0) {
            throw new ValidationException("page must be >= 0");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new ValidationException("size must be between 1 and " + MAX_PAGE_SIZE);
        }
    }
}
