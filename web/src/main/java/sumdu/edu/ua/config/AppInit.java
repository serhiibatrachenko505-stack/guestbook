package sumdu.edu.ua.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Точка ініціалізації: створює схему БД і всі залежності до першого запиту. */
public class AppInit implements ServletContextListener {
    private static final Logger log = LoggerFactory.getLogger(AppInit.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Beans.init();
        log.info("App started at: http://localhost:8080/");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("App stopped");
    }
}
