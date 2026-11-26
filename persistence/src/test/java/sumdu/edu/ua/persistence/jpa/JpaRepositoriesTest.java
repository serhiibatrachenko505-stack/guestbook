package sumdu.edu.ua.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.persistence.jpa.adapter.JpaCatalogRepositoryAdapter;
import sumdu.edu.ua.persistence.jpa.adapter.JpaCommentRepositoryAdapter;
import sumdu.edu.ua.persistence.jpa.adapter.JpaUserRepositoryAdapter;
import sumdu.edu.ua.persistence.jpa.repository.BookRepository;
import sumdu.edu.ua.persistence.jpa.repository.CommentRepository;
import sumdu.edu.ua.persistence.jpa.repository.UserRepository;

/**
 * Репозиторії й адаптери на тимчасовій H2 в пам'яті.
 * Схему і тестові дані створює Flyway (V1__init.sql, V2__seed_data.sql).
 */
@DataJpaTest
@Import({JpaCatalogRepositoryAdapter.class, JpaCommentRepositoryAdapter.class, JpaUserRepositoryAdapter.class})
class JpaRepositoriesTest {

    @Autowired private BookRepository bookRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private CommentRepository commentRepo;

    @Autowired private JpaCatalogRepositoryAdapter catalog;
    @Autowired private JpaCommentRepositoryAdapter comments;
    @Autowired private JpaUserRepositoryAdapter users;

    @Test
    void flywaySeedDataIsLoaded() {
        assertEquals(12, bookRepo.count());
        assertEquals(4, userRepo.count());
        assertTrue(commentRepo.count() >= 6);
    }

    @Test
    void searchIsCaseInsensitiveWithPaging() {
        var page = catalog.search("MARTIN", new PageRequest(0, 10, "year", true));
        assertEquals(3, page.getTotal());   // Robert C. Martin x2, Martin Fowler
        assertEquals(2018, page.getItems().get(0).getPubYear());
    }

    @Test
    void commentIsLinkedToBookAndUser() {
        var user = users.findByUsername("Student").orElseThrow();
        Comment saved = comments.add(1, user.getId(), "Student", "JPA test");

        assertNotNull(saved.getCreatedAt());          // заповнено @PrePersist
        assertEquals(1, saved.getBookId());
        assertEquals(user.getId(), saved.getUserId());
    }

    @Test
    void findCommentsByUser() {
        long olenaId = users.findByUsername("Олена").orElseThrow().getId();
        var list = comments.findByUser(olenaId);
        assertEquals(3, list.size());
        // нові першими
        assertTrue(list.get(0).getCreatedAt().isAfter(list.get(1).getCreatedAt()));
    }

    @Test
    void bookCommentsAreFilteredAndPaged() {
        var page = comments.list(1, "андр", null, new PageRequest(0, 10));
        assertEquals(1, page.getTotal());
    }

    @Test
    void deleteRemovesComment() {
        long id = comments.add(2, null, "Guest", "to delete").getId();
        assertTrue(comments.delete(2, id));
        assertFalse(comments.findById(2, id).isPresent());
    }

    @Test
    void newUserCanBeCreated() {
        var u = users.create("NewReader", "USER");
        assertTrue(u.getId() > 0);
        assertEquals("USER", users.findById(u.getId()).orElseThrow().getRole());
    }
}
