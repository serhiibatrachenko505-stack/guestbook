package app.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Фабрика JDBC-з'єднань з файловою БД H2. */
public final class Db {
  /** Файл БД: ./data/guest.mv.db (відносно каталогу, з якого запущено mvn). */
  private static final String URL = "jdbc:h2:file:./data/guest;AUTO_SERVER=TRUE";

  static {
    try {
      Class.forName("org.h2.Driver");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("H2 driver not found", e);
    }
  }

  private Db() {}

  public static Connection get() throws SQLException {
    return DriverManager.getConnection(URL, "sa", "");
  }
}
