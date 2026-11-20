package sumdu.edu.ua.persistence.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Створює схему з schema.sql і, якщо таблиця books порожня, заповнює її з data.sql. */
public final class DbInit {
    private static final Logger log = LoggerFactory.getLogger(DbInit.class);

    private DbInit() {}

    public static void init() {
        try (Connection c = Db.get(); Statement st = c.createStatement()) {
            runScript(st, "schema.sql");

            try (var rs = st.executeQuery("select count(*) from books")) {
                rs.next();
                if (rs.getLong(1) == 0) {
                    runScript(st, "data.sql");
                    log.info("DB: demo data inserted");
                }
            }
            log.info("DB schema initialized ({})", Db.url());
        } catch (Exception e) {
            throw new RuntimeException("DB schema init failed", e);
        }
    }

    private static void runScript(Statement st, String resource) throws IOException, SQLException {
        try (InputStream in = DbInit.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException(resource + " not found in resources");
            }
            String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            for (String cmd : sql.split(";")) {
                String clean = stripComments(cmd);
                if (!clean.isBlank()) {
                    st.execute(clean);
                }
            }
        }
    }

    private static String stripComments(String sql) {
        StringBuilder sb = new StringBuilder();
        for (String line : sql.split("\\R")) {
            if (!line.trim().startsWith("--")) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }
}
