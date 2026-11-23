package sumdu.edu.ua.persistence.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.port.CommentRepositoryPort;

/** Реалізація порту відгуків через JDBC/H2. */
@Repository
public class JdbcCommentRepository implements CommentRepositoryPort {
    private static final Logger log = LoggerFactory.getLogger(JdbcCommentRepository.class);

    private static final String COLUMNS = "id, book_id, author, text, created_at";

    @Override
    public Comment add(long bookId, String author, String text) {
        try (var c = Db.get();
             var ps = c.prepareStatement(
                     "insert into comments(book_id, author, text) values (?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, bookId);
            ps.setString(2, author);
            ps.setString(3, text);
            ps.executeUpdate();

            long id;
            try (var keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new IllegalStateException("Insert succeeded but no id generated");
                }
                id = keys.getLong(1);
            }
            log.debug("DB: inserted comment #{} for book {}", id, bookId);
            return findById(bookId, id).orElseThrow();
        } catch (SQLException e) {
            throw new RuntimeException("DB insert error", e);
        }
    }

    @Override
    public Optional<Comment> findById(long bookId, long commentId) {
        try (var c = Db.get();
             var ps = c.prepareStatement(
                     "select " + COLUMNS + " from comments where id = ? and book_id = ?")) {
            ps.setLong(1, commentId);
            ps.setLong(2, bookId);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB query error", e);
        }
    }

    @Override
    public Page<Comment> list(long bookId, String author, Instant since, PageRequest request) {
        StringBuilder where = new StringBuilder(" where book_id = ?");
        if (author != null) {
            where.append(" and lower(author) like ?");
        }
        if (since != null) {
            where.append(" and created_at >= ?");
        }

        var items = new ArrayList<Comment>();
        long total;

        try (var c = Db.get()) {
            try (var ps = c.prepareStatement("select " + COLUMNS + " from comments" + where
                    + " order by created_at desc, id desc limit ? offset ?")) {
                int i = bindFilters(ps, bookId, author, since);
                ps.setInt(i++, request.getSize());
                ps.setInt(i, request.getOffset());
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        items.add(map(rs));
                    }
                }
            }
            try (var ps = c.prepareStatement("select count(*) from comments" + where)) {
                bindFilters(ps, bookId, author, since);
                try (var rs = ps.executeQuery()) {
                    rs.next();
                    total = rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB query error", e);
        }
        return new Page<>(items, request, total);
    }

    @Override
    public boolean delete(long bookId, long commentId) {
        try (var c = Db.get();
             var ps = c.prepareStatement("delete from comments where id = ? and book_id = ?")) {
            ps.setLong(1, commentId);
            ps.setLong(2, bookId);
            boolean deleted = ps.executeUpdate() > 0;
            log.debug("DB: delete comment #{} for book {} -> {}", commentId, bookId, deleted);
            return deleted;
        } catch (SQLException e) {
            throw new RuntimeException("DB delete error", e);
        }
    }

    private static int bindFilters(java.sql.PreparedStatement ps, long bookId, String author, Instant since)
            throws SQLException {
        int i = 1;
        ps.setLong(i++, bookId);
        if (author != null) {
            ps.setString(i++, "%" + author.toLowerCase() + "%");
        }
        if (since != null) {
            ps.setTimestamp(i++, Timestamp.from(since));
        }
        return i;
    }

    private static Comment map(ResultSet rs) throws SQLException {
        return new Comment(
                rs.getLong("id"),
                rs.getLong("book_id"),
                rs.getString("author"),
                rs.getString("text"),
                rs.getTimestamp("created_at").toInstant());
    }
}
