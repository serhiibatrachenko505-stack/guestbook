package sumdu.edu.ua.config;

import java.time.Clock;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sumdu.edu.ua.core.port.CatalogRepositoryPort;
import sumdu.edu.ua.core.port.CommentRepositoryPort;
import sumdu.edu.ua.core.port.NotificationPort;
import sumdu.edu.ua.core.port.PasswordHasher;
import sumdu.edu.ua.core.port.UserRepositoryPort;
import sumdu.edu.ua.core.service.BookService;
import sumdu.edu.ua.core.service.CommentService;
import sumdu.edu.ua.core.service.UserService;

/**
 * Java-конфігурація бінів.
 * Сервіси з core не мають анотацій Spring (core залишається чистим),
 * тому вони реєструються тут через @Bean. Spring сам передає в методи
 * реалізації портів — JPA-адаптери з модуля persistence.
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
        log.info("Creating bean bookService with {}", name(bookRepo));
        return new BookService(bookRepo);
    }

    @Bean
    public UserService userService(UserRepositoryPort userRepo, PasswordHasher hasher, NotificationPort notifier) {
        log.info("Creating bean userService with {}, {}, {}", name(userRepo), name(hasher), name(notifier));
        return new UserService(userRepo, hasher, notifier);
    }

    @Bean
    public CommentService commentService(CommentRepositoryPort commentRepo,
                                         CatalogRepositoryPort bookRepo,
                                         UserService userService,
                                         Clock clock,
                                         LibraryProperties props) {
        Duration window = Duration.ofHours(props.comments().deleteWindowHours());
        log.info("Creating bean commentService with {} (delete window = {} h)", name(commentRepo), window.toHours());
        return new CommentService(commentRepo, bookRepo, userService, clock, window);
    }

    /** Ім'я класу реалізації без суфікса проксі Spring ($$SpringCGLIB$$). */
    private static String name(Object bean) {
        return AopUtils.getTargetClass(bean).getSimpleName();
    }
}
