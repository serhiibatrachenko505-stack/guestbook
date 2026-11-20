package sumdu.edu.ua.persistence.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.port.CatalogRepositoryPort;

/** Реалізація порту каталогу через JDBC/H2. */
public class JdbcBookRepository implements CatalogRepositoryPort {
    private static final Logger log = LoggerFactory.getLogger(JdbcBookRepository.class);

    /** Логічне поле сортування -> колонка БД (білий список, захист від SQL-ін'єкцій). */
    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "id", "id",
            "title", "title",
            "author", "author",
            "year", "pub_year");

    @Override
    public Page<Book> search(String q, PageRequest request) {
        boolean filter = q != null && !q.isBlank();
        String where = filter ? " where lower(title) like ? or lower(author) like ?" : "";
        String column = SORT_COLUMNS.getOrDefault(request.getSortBy(), "id");
        String dir = request.isDescending() ? "desc" : "asc";

        String sql = "select id, title, author, pub_year from books" + where
                + " order by " + column + " " + dir + ", id " + dir
                + " limit ? offset ?";

        var items = new ArrayList<Book>();
        long total;

        try (var c = Db.get()) {
            try (var ps = c.prepareStatement(sql)) {
                int i = 1;
                if (filter) {
                    String pattern = "%" + q.toLowerCase() + "%";
                    ps.setString(i++, pattern);
                    ps.setString(i++, pattern);
                }
                ps.setInt(i++, request.getSize());
                ps.setInt(i, request.getOffset());
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        items.add(map(rs));
                    }
                }
            }

            try (var ps = c.prepareStatement("select count(*) from books" + where)) {
                if (filter) {
                    String pattern = "%" + q.toLowerCase() + "%";
                    ps.setString(1, pattern);
                    ps.setString(2, pattern);
                }
                try (var rs = ps.executeQuery()) {
                    rs.next();
                    total = rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB query error", e);
        }

        log.debug("DB: books search q='{}' -> {} of {}", q, items.size(), total);
        return new Page<>(items, request, total);
    }

    @Override
    public Optional<Book> findById(long id) {
        try (var c = Db.get();
             var ps = c.prepareStatement("select id, title, author, pub_year from books where id = ?")) {
            ps.setLong(1, id);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB query error", e);
        }
    }

    @Override
    public Book add(String title, String author, int pubYear) {
        try (var c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "insert into books(title, author, pub_year) values (?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setInt(3, pubYear);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Book(keys.getLong(1), title, author, pubYear);
                }
            }
            throw new IllegalStateException("Insert succeeded but no id generated");
        } catch (SQLException e) {
            throw new RuntimeException("DB insert book failed", e);
        }
    }

    private static Book map(ResultSet rs) throws SQLException {
        return new Book(rs.getLong("id"), rs.getString("title"),
                rs.getString("author"), rs.getInt("pub_year"));
    }
}
