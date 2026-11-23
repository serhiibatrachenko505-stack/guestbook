package sumdu.edu.ua.web.controller;

import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.exception.ValidationException;

/** Перетворює page/size/sort з запиту на PageRequest. sort: "title" або "year,desc". */
final class SortParam {
    private SortParam() {}

    static PageRequest toPageRequest(int page, int size, String sort) {
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
