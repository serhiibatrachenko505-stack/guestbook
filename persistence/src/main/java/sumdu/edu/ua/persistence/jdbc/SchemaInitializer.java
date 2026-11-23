package sumdu.edu.ua.persistence.jdbc;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Бін, що демонструє життєвий цикл:
 * Spring створює об'єкт, потім викликає @PostConstruct (створення схеми БД),
 * а під час зупинки контексту — @PreDestroy.
 */
@Component
public class SchemaInitializer {
    private static final Logger log = LoggerFactory.getLogger(SchemaInitializer.class);

    public SchemaInitializer() {
        log.info("Lifecycle: SchemaInitializer constructed");
    }

    @PostConstruct
    public void init() {
        DbInit.init();
        log.info("Lifecycle: @PostConstruct — DB schema is ready");
    }

    @PreDestroy
    public void shutdown() {
        log.info("Lifecycle: @PreDestroy — application context is closing");
    }
}
