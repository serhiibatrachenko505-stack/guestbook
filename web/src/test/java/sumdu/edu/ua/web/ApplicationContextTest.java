package sumdu.edu.ua.web;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import sumdu.edu.ua.config.WebConfig;
import sumdu.edu.ua.core.port.CatalogRepositoryPort;
import sumdu.edu.ua.core.port.CommentRepositoryPort;
import sumdu.edu.ua.core.service.BookService;
import sumdu.edu.ua.core.service.CommentService;
import sumdu.edu.ua.persistence.jdbc.JdbcBookRepository;
import sumdu.edu.ua.persistence.jdbc.JdbcCommentRepository;
import sumdu.edu.ua.web.controller.BooksController;
import sumdu.edu.ua.web.controller.CommentsController;
import sumdu.edu.ua.web.error.GlobalExceptionHandler;

/** Перевіряє, що контекст Spring MVC піднімається і всі біни на місці. */
@SpringBootTest
class ApplicationContextTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void dispatcherServletIsRegisteredAutomatically() {
        assertNotNull(context.getBean(DispatcherServlet.class));
    }

    @Test
    void mvcConfigurationIsLoaded() {
        assertNotNull(context.getBean(WebConfig.class));
        assertNotNull(context.getBean(GlobalExceptionHandler.class));
    }

    @Test
    void controllersAreBeans() {
        assertNotNull(context.getBean(BooksController.class));
        assertNotNull(context.getBean(CommentsController.class));
    }

    @Test
    void servicesAndRepositoriesAreWired() {
        assertNotNull(context.getBean(BookService.class));
        assertNotNull(context.getBean(CommentService.class));
        assertInstanceOf(JdbcBookRepository.class, context.getBean(CatalogRepositoryPort.class));
        assertInstanceOf(JdbcCommentRepository.class, context.getBean(CommentRepositoryPort.class));
    }

    @Test
    void noServletsLeftInApplication() {
        assertTrue(context.getBeansOfType(jakarta.servlet.http.HttpServlet.class).keySet().stream()
                .allMatch(name -> name.equals("dispatcherServlet")));
    }
}
