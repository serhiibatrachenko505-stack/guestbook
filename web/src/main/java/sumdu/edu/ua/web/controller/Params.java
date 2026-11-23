package sumdu.edu.ua.web.controller;

import io.javalin.http.Context;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.exception.ValidationException;

/** Розбір параметрів пагінації та сортування з Context. */
final class Params {
    private Params() {}

    /**
     * page і size — через валідатор Javalin (нечислове значення -> 400),
     * sort — у форматі "title" або "year,desc".
     */
    static PageRequest pageRequest(Context ctx, int defaultSize) {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(0);
        int size = ctx.queryParamAsClass("size", Integer.class).getOrDefault(defaultSize);

        String sort = ctx.queryParam("sort");
        if (sort == null || sort.isBlank()) {
            return new PageRequest(page, size);
        }
        String[] parts = sort.trim().split(",");
        String field = parts[0].trim().toLowerCase();
        boolean desc = false;
        if (parts.length > 1) {
            String dir = parts[1].trim().toLowerCase();
            if (!dir.equals("asc") && !dir.equals("desc")) {
                throw new ValidationException("sort direction must be asc or desc");
            }
            desc = dir.equals("desc");
        }
        return new PageRequest(page, size, field, desc);
    }
}
