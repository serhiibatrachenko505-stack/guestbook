package sumdu.edu.ua.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import sumdu.edu.ua.config.LibraryProperties;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.port.CatalogRepositoryPort;
import sumdu.edu.ua.core.port.CommentRepositoryPort;
import sumdu.edu.ua.core.service.BookService;
import sumdu.edu.ua.core.service.CommentService;
import sumdu.edu.ua.persistence.jdbc.JdbcBookRepository;
import sumdu.edu.ua.persistence.jdbc.JdbcCommentRepository;
import sumdu.edu.ua.web.http.BooksServlet;
import sumdu.edu.ua.web.http.InfoServlet;
import sumdu.edu.ua.web.service.LibraryInfoService;

/** Перевіряє, що Spring знайшов, створив і зв'язав усі біни застосунку. */
@SpringBootTest
class ApplicationContextTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private LibraryProperties props;

    @Test
    void repositoriesAreFoundByComponentScan() {
        assertInstanceOf(JdbcBookRepository.class, context.getBean(CatalogRepositoryPort.class));
        assertInstanceOf(JdbcCommentRepository.class, context.getBean(CommentRepositoryPort.class));
    }

    @Test
    void servicesAreCreatedByBeanMethods() {
        assertNotNull(context.getBean(BookService.class));
        assertNotNull(context.getBean(CommentService.class));
        assertNotNull(context.getBean(LibraryInfoService.class));
    }

    @Test
    void servletsAreBeans() {
        assertNotNull(context.getBean(BooksServlet.class));
        assertNotNull(context.getBean(InfoServlet.class));
    }

    @Test
    void propertiesAreLoadedFromYaml() {
        assertEquals("Каталог книг", props.title());
        assertEquals(10, props.books().defaultPageSize());
        assertEquals(24, props.comments().deleteWindowHours());
    }

    @Test
    void propertiesAreInjectedIntoCommentService() {
        CommentService comments = context.getBean(CommentService.class);
        assertEquals(Duration.ofHours(props.comments().deleteWindowHours()), comments.getDeleteWindow());
    }

    @Test
    void beansWorkTogetherWithDatabase() {
        BookService books = context.getBean(BookService.class);
        assertTrue(books.search(null, new PageRequest(0, 5)).getTotal() > 0);
    }
}
