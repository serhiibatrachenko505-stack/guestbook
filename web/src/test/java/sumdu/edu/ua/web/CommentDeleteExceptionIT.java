package sumdu.edu.ua.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import sumdu.edu.ua.core.exception.BookNotFoundException;
import sumdu.edu.ua.core.exception.CommentTooOldException;
import sumdu.edu.ua.core.exception.InvalidCommentDeleteException;
import sumdu.edu.ua.core.service.CommentService;

/**
 * Інтеграційний тест web-рівня: справжній CommentService замінено моком (@MockBean),
 * який кидає винятки core. Перевіряється, що GlobalApiExceptionHandler перетворює їх
 * на правильні HTTP-статуси і JSON {"error": "..."}.
 * addFilters = false — фільтри Spring Security вимкнено, тест зосереджено на обробці винятків.
 */
@SpringBootTest(classes = AppInit.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(roles = "ADMIN")   // для @RolesAllowed на методі сервісу (method security працює і без фільтрів)
class CommentDeleteExceptionIT {

    private static final String TOO_OLD =
            "Коментар створено більше ніж 24 години тому і не може бути видалений";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    /** Бін обгорнуто проксі Spring (транзакції, безпека, аспекти) — стабимо сам мок усередині. */
    private CommentService mock() {
        return AopTestUtils.getUltimateTargetObject(commentService);
    }

    @Test
    void commentTooOld_returns400WithErrorJson() throws Exception {
        doThrow(new CommentTooOldException(TOO_OLD))
                .when(mock()).delete(eq(1L), eq(2L), any(Instant.class));

        mockMvc.perform(post("/comments/delete")
                        .param("bookId", "1")
                        .param("commentId", "2")
                        .param("createdAt", "2024-01-01T00:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(TOO_OLD));
    }

    @Test
    void invalidDelete_returns400WithErrorJson() throws Exception {
        doThrow(new InvalidCommentDeleteException("Некоректний ідентифікатор книги або коментаря"))
                .when(mock()).delete(eq(0L), eq(2L), any(Instant.class));

        mockMvc.perform(post("/comments/delete")
                        .param("bookId", "0")
                        .param("commentId", "2")
                        .param("createdAt", "2024-01-01T00:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Некоректний ідентифікатор книги або коментаря"));
    }

    @Test
    void bookNotFound_returns404WithErrorJson() throws Exception {
        doThrow(new BookNotFoundException("Книгу з id=77 не знайдено"))
                .when(mock()).delete(eq(77L), eq(2L), any(Instant.class));

        mockMvc.perform(post("/comments/delete")
                        .param("bookId", "77")
                        .param("commentId", "2")
                        .param("createdAt", "2024-01-01T00:00:00Z"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Книгу з id=77 не знайдено"));
    }
}
