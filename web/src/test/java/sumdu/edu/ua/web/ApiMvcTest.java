package sumdu.edu.ua.web;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/** REST API (/api/...) — JSON-відповіді. */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", roles = "ADMIN")   // API доступний лише автентифікованим
class ApiMvcTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void getBooksReturnsJsonList() throws Exception {
        mvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.total", greaterThan(0)));
    }

    @Test
    void getBookReturnsBookWithComments() throws Exception {
        mvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.book.id").value(1))
                .andExpect(jsonPath("$.comments.items").isArray());
    }

    @Test
    void postCommentCreatesComment() throws Exception {
        mvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":1,\"author\":\"Test\",\"text\":\"Created from MockMvc\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.author").value("Test"));
    }

    @Test
    void unknownBookGives404() throws Exception {
        mvc.perform(get("/api/books/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void emptyAuthorGives400() throws Exception {
        mvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":1,\"author\":\"\",\"text\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nonNumericIdGives400() throws Exception {
        mvc.perform(get("/api/books/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void oldCommentCannotBeDeleted() throws Exception {
        // тестовий відгук #2 з data.sql створено 2 доби тому
        mvc.perform(delete("/api/comments/2").param("bookId", "1"))
                .andExpect(status().isConflict());
    }
}
