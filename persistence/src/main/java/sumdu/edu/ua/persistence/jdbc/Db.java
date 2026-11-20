package sumdu.edu.ua.persistence.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Фабрика JDBC-з'єднань з файловою БД H2. */
public final class Db {
    /** Файл БД: ./data/library.mv.db (відносно каталогу, з якого запущено Maven). */
    private static final String URL  = "jdbc:h2:file:./data/library;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASS = "";

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Db() {}

    public static String url() {
        return URL;
    }

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
