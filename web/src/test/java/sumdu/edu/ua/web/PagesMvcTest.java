package sumdu.edu.ua.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import sumdu.edu.ua.web.service.MailService;

/** HTML-сторінки Thymeleaf: контролер -> Model -> шаблон, форми і локалізація. */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", roles = "ADMIN")   // сторінки доступні лише після входу
class PagesMvcTest {

    @Autowired
    private MockMvc mvc;

    /** У тестах лист не надсилається: MailService підмінено заглушкою Mockito. */
    @MockBean
    private MailService mailService;

    @Test
    void booksPageRendersTemplateWithModel() throws Exception {
        mvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("books"))
                .andExpect(model().attributeExists("books", "page"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("Каталог книг")));
    }

    @Test
    void emptySearchShowsNoBooksMessage() throws Exception {
        mvc.perform(get("/books").param("q", "zzz-no-such-book"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Немає книг")));
    }

    @Test
    void langParamSwitchesToEnglish() throws Exception {
        mvc.perform(get("/books").param("lang", "en"))
                .andExpect(content().string(containsString("Book catalog")))
                .andExpect(content().string(not(containsString("Каталог книг"))));
    }

    @Test
    void acceptLanguageHeaderSwitchesToEnglish() throws Exception {
        mvc.perform(get("/books").header("Accept-Language", "en-US,en;q=0.9"))
                .andExpect(content().string(containsString("Book catalog")));
    }

    @Test
    void addFormIsBoundToBookForm() throws Exception {
        mvc.perform(get("/books/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("book-form"))
                .andExpect(model().attributeExists("bookForm"))
                .andExpect(content().string(containsString("name=\"title\"")));
    }

    @Test
    void validBookIsSavedAndRedirects() throws Exception {
        mvc.perform(post("/books/add").with(csrf())
                        .param("title", "Thymeleaf Test Book")
                        .param("author", "Test Author")
                        .param("pubYear", "2024"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"))
                .andExpect(flash().attribute("message", "msg.bookAdded"));
    }

    @Test
    void invalidBookReturnsFormWithErrors() throws Exception {
        mvc.perform(post("/books/add").with(csrf())
                        .param("title", "")
                        .param("author", "Someone")
                        .param("pubYear", "2024"))
                .andExpect(status().isOk())
                .andExpect(view().name("book-form"))
                .andExpect(model().attributeHasFieldErrors("bookForm", "title"))
                .andExpect(content().string(containsString("Поле обов’язкове")));
    }

    @Test
    void bookPageShowsComments() throws Exception {
        mvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("book"))
                .andExpect(model().attributeExists("book", "comments", "commentForm"));
    }

    @Test
    void usersPageListsUsers() throws Exception {
        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("users"))
                .andExpect(content().string(containsString("Олена")));
    }

    @Test
    void userCommentsPageShowsCommentsWithBooks() throws Exception {
        mvc.perform(get("/users/2/comments"))          // Олена з V2__seed_data.sql
                .andExpect(status().isOk())
                .andExpect(view().name("user-comments"))
                .andExpect(model().attributeExists("user", "comments", "books"))
                .andExpect(content().string(containsString("Clean Code")));
    }

    @Test
    void unknownUserShowsErrorPage() throws Exception {
        mvc.perform(get("/users/999999/comments"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"));
    }

    @Test
    @WithMockUser(username = "Test Reader", roles = "USER")   // автор відгуку = поточний користувач
    void commentFromFormIsLinkedToUser() throws Exception {
        mvc.perform(post("/comments").with(csrf())
                        .param("bookId", "2")
                        .param("text", "Comment from form"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/2"));
        mvc.perform(get("/users"))
                .andExpect(content().string(containsString("Test Reader")));
    }

    @Test
    void unknownBookShowsErrorPage() throws Exception {
        mvc.perform(get("/books/999999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"));
    }
}
