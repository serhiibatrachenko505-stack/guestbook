package sumdu.edu.ua.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.flywaydb.core.Flyway;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import sumdu.edu.ua.web.controller.BooksController;
import sumdu.edu.ua.web.controller.CommentsController;
import sumdu.edu.ua.web.controller.UserController;
import sumdu.edu.ua.core.port.UserRepositoryPort;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.aop.support.AopUtils;

/** Контекст: DispatcherServlet, Thymeleaf, локалізація, контролери. */
@SpringBootTest
class ApplicationContextTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MessageSource messages;

    @Test
    void dispatcherServletAndThymeleafAreConfigured() {
        assertNotNull(context.getBean(DispatcherServlet.class));
        assertNotNull(context.getBean(ThymeleafViewResolver.class));
    }

    @Test
    void viewControllersAreBeans() {
        assertNotNull(context.getBean(BooksController.class));
        assertNotNull(context.getBean(CommentsController.class));
        assertNotNull(context.getBean(UserController.class));
    }

    @Test
    void jpaAndFlywayAreConfigured() {
        assertNotNull(context.getBean(EntityManagerFactory.class));
        Flyway flyway = context.getBean(Flyway.class);
        assertEquals(2, flyway.info().applied().length);        // V1__init, V2__seed_data
    }

    @Test
    void portsAreImplementedByJpaAdapters() {
        Object users = context.getBean(UserRepositoryPort.class);
        assertEquals("JpaUserRepositoryAdapter", AopUtils.getTargetClass(users).getSimpleName());
    }

    @Test
    void cookieLocaleResolverIsUsed() {
        assertInstanceOf(CookieLocaleResolver.class, context.getBean(LocaleResolver.class));
    }

    @Test
    void messagesAreLocalized() {
        assertEquals("Каталог книг", messages.getMessage("title.books", null, Locale.forLanguageTag("uk")));
        assertEquals("Book catalog", messages.getMessage("title.books", null, Locale.ENGLISH));
        // невідома мова -> messages.properties (українська)
        assertEquals("Каталог книг", messages.getMessage("title.books", null, Locale.GERMAN));
    }
}
