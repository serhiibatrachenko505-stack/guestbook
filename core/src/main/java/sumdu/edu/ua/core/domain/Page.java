package sumdu.edu.ua.core.domain;

import java.util.List;

/** Одна сторінка результатів + загальна кількість записів. */
public class Page<T> {
    private final List<T> items;
    private final PageRequest request;
    private final long total;

    public Page(List<T> items, PageRequest request, long total) {
        this.items = List.copyOf(items);
        this.request = request;
        this.total = total;
    }

    public List<T> getItems() { return items; }
    public int getPage() { return request.getPage(); }
    public int getSize() { return request.getSize(); }
    public String getSort() { return request.getSortBy() + "," + (request.isDescending() ? "desc" : "asc"); }
    public long getTotal() { return total; }

    public long getTotalPages() {
        return request.getSize() == 0 ? 0 : (total + request.getSize() - 1) / request.getSize();
    }
}
