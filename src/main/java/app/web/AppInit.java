package app.web;

import app.db.CommentDao;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Виконується один раз під час старту веб-додатку: створює схему БД. */
public class AppInit implements ServletContextListener {
  private static final Logger log = LoggerFactory.getLogger(AppInit.class);

  @Override
  public void contextInitialized(ServletContextEvent e) {
    try {
      new CommentDao().init();
      log.info("DB schema initialized (table 'comments' is ready)");
    } catch (Exception ex) {
      log.error("DB init failed", ex);
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent e) {
    log.info("Application stopped");
  }
}
