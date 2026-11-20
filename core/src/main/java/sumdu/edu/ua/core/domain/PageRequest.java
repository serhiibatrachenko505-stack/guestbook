package sumdu.edu.ua.core.domain;

/**
 * Параметри сторінки: номер (з 0), розмір і сортування.
 * sortBy — логічна назва поля (id, title, author, year), а не назва колонки БД.
 */
public class PageRequest {
    private final int page;
    private final int size;
    private final String sortBy;
    private final boolean descending;

    public PageRequest(int page, int size, String sortBy, boolean descending) {
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.descending = descending;
    }

    /** Сторінка з сортуванням за замовчуванням: нові записи першими. */
    public PageRequest(int page, int size) {
        this(page, size, "id", true);
    }

    public int getPage() { return page; }
    public int getSize() { return size; }
    public String getSortBy() { return sortBy; }
    public boolean isDescending() { return descending; }

    public int getOffset() { return page * size; }
}
