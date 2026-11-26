package sumdu.edu.ua.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.service.CommentService;

/**
 * Повний ланцюжок зі справжнім сервісом і Spring Security:
 * контролер -> аспект -> CommentService (валідація) -> GlobalApiExceptionHandler / репозиторій.
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", roles = "ADMIN")
@ExtendWith(OutputCaptureExtension.class)
class CommentDeleteFlowIT {

    @Autowired private MockMvc mvc;
    @Autowired private CommentService comments;

    @Test
    void invalidIds_return400() throws Exception {
        mvc.perform(post("/comments/delete").with(csrf())
                        .param("bookId", "0").param("commentId", "2")
                        .param("createdAt", Instant.now().toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Некоректний ідентифікатор книги або коментаря"));
    }

    @Test
    void missingCreatedAt_returns400() throws Exception {
        mvc.perform(post("/comments/delete").with(csrf())
                        .param("bookId", "1").param("commentId", "2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Не вказано дату створення коментаря"));
    }

    @Test
    void oldComment_returns400_andAspectLogsFailure(CapturedOutput output) throws Exception {
        mvc.perform(post("/comments/delete").with(csrf())
                        .param("bookId", "1").param("commentId", "2")
                        .param("createdAt", "2024-01-01T00:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("більше ніж 24 години")));

        assertThat(output).contains("Calling CommentService.delete(bookId=1, commentId=2");
        assertThat(output).contains("failed in");
        assertThat(output).contains("CommentTooOldException");
    }

    @Test
    void freshComment_isDeleted_andAspectLogsTime(CapturedOutput output) throws Exception {
        Comment c = comments.add(3, "admin", "Comment for delete test");

        mvc.perform(post("/comments/delete").with(csrf())
                        .param("bookId", "3").param("commentId", String.valueOf(c.getId()))
                        .param("createdAt", c.getCreatedAt().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/3"))
                .andExpect(flash().attribute("message", "msg.commentDeleted"));

        assertThat(output).contains("CommentService.delete(bookId=3, commentId=" + c.getId() + ") finished in");
        assertThat(output).contains("[timing] CommentService.delete took");
    }

    @Test
    void browserRequest_isRedirectedBackWithFlashError() throws Exception {
        mvc.perform(post("/comments/delete").with(csrf())
                        .header("Accept", "text/html,application/xhtml+xml")
                        .header("Referer", "http://localhost/books/1")
                        .param("bookId", "1").param("commentId", "2")
                        .param("createdAt", "2024-01-01T00:00:00Z"))
                .andExpect(status().isSeeOther())
                .andExpect(header().string("Location", "http://localhost/books/1"))
                .andExpect(flash().attribute("error", "error.commentTooOld"));
    }
}
