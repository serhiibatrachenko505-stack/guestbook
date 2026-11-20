package sumdu.edu.ua.web.http;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.web.error.HttpError;

/** Розбір параметрів запиту з перевіркою (некоректне значення -> 400). */
public final class Params {
    private Params() {}

    public static int intParam(HttpServletRequest req, String name, int def) {
        String v = req.getParameter(name);
        if (v == null || v.isBlank()) {
            return def;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            throw HttpError.badRequest(name + " must be a number");
        }
    }

    public static long id(String raw, String name) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            throw HttpError.badRequest(name + " must be a number");
        }
    }

    public static Instant instantParam(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        if (v == null || v.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(v.trim());
        } catch (DateTimeParseException e) {
            throw HttpError.badRequest(name + " must be ISO-8601 instant, e.g. 2026-11-20T10:00:00Z");
        }
    }

    /**
     * page, size і sort. Формат sort: "title" або "title,asc" / "year,desc".
     * Допустимість самого поля перевіряє BookService.
     */
    public static PageRequest pageRequest(HttpServletRequest req, int defaultSize) {
        int page = intParam(req, "page", 0);
        int size = intParam(req, "size", defaultSize);

        String sort = req.getParameter("sort");
        if (sort == null || sort.isBlank()) {
            return new PageRequest(page, size);
        }
        String[] parts = sort.trim().split(",");
        String field = parts[0].trim().toLowerCase();
        boolean desc = false;
        if (parts.length > 1) {
            String dir = parts[1].trim().toLowerCase();
            if (!dir.equals("asc") && !dir.equals("desc")) {
                throw HttpError.badRequest("sort direction must be asc or desc");
            }
            desc = dir.equals("desc");
        }
        return new PageRequest(page, size, field, desc);
    }
}
