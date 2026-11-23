package sumdu.edu.ua.config;

import java.time.Clock;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sumdu.edu.ua.core.port.CatalogRepositoryPort;
import sumdu.edu.ua.core.port.CommentRepositoryPort;
import sumdu.edu.ua.core.service.BookService;
import sumdu.edu.ua.core.service.CommentService;

/**
 * Java-конфігурація бінів.
 * Сервіси з core не мають анотацій Spring (core залишається чистим),
 * тому вони реєструються тут через @Bean. Spring сам передає в методи
 * потрібні залежності — реалізації портів (@Repository з модуля persistence).
 */
@Configuration
@EnableConfigurationProperties(LibraryProperties.class)
public class AppConfig {
    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    /** Кастомний бін: годинник, від якого залежить правило видалення відгуків. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public BookService bookService(CatalogRepositoryPort bookRepo) {
        log.info("Creating bean bookService with {}", bookRepo.getClass().getSimpleName());
        return new BookService(bookRepo);
    }

    @Bean
    public CommentService commentService(CommentRepositoryPort commentRepo,
                                         CatalogRepositoryPort bookRepo,
                                         Clock clock,
                                         LibraryProperties props) {
        Duration window = Duration.ofHours(props.comments().deleteWindowHours());
        log.info("Creating bean commentService with {} (delete window = {} h)",
                commentRepo.getClass().getSimpleName(), window.toHours());
        return new CommentService(commentRepo, bookRepo, clock, window);
    }
}
