package app.web;

import app.db.CommentDao;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** REST-ендпоїнт /comments: GET — список, POST — додавання. */
public class CommentsServlet extends HttpServlet {
  private static final Logger log = LoggerFactory.getLogger(CommentsServlet.class);
  private static final int MAX_AUTHOR = 64;
  private static final int MAX_TEXT = 1000;

  private final CommentDao dao = new CommentDao();
  private final ObjectMapper om = new ObjectMapper();

  /**
   * GET /comments
   * 200 OK + JSON-масив коментарів (останні зверху).
   * Помилка БД: 500 + "DB error".
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      var comments = dao.list();
      resp.setStatus(HttpServletResponse.SC_OK);
      resp.setContentType("application/json; charset=UTF-8");
      om.writeValue(resp.getWriter(), comments);
    } catch (SQLException e) {
      log.error("GET /comments failed", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "DB error");
    }
  }

  /**
   * POST /comments
   * 204 No Content при успіху.
   * Валідація: 400 (порожні/надто довгі поля).
   * Помилка БД: 500 + "DB error".
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    req.setCharacterEncoding("UTF-8"); // до читання параметрів!

    String author = trim(req.getParameter("author"));
    String text = trim(req.getParameter("text"));

    String error = validate(author, text);
    if (error != null) {
      log.warn("Validation failed: {}", error);
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, error);
      return;
    }

    try {
      long id = dao.add(author, text);
      log.info("New comment: id={}, author='{}', length={}", id, author, text.length());
      resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } catch (SQLException e) {
      log.error("POST /comments failed", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "DB error");
    }
  }

  private static String trim(String s) {
    return s == null ? null : s.trim();
  }

  private static String validate(String author, String text) {
    if (author == null || author.isEmpty()) return "author is required";
    if (author.length() > MAX_AUTHOR) return "author must be <= " + MAX_AUTHOR + " chars";
    if (text == null || text.isEmpty()) return "text is required";
    if (text.length() > MAX_TEXT) return "text must be <= " + MAX_TEXT + " chars";
    return null;
  }
}
