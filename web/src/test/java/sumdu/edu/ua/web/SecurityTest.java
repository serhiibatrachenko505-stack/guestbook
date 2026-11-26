package sumdu.edu.ua.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import sumdu.edu.ua.core.port.NotificationPort;
import sumdu.edu.ua.core.service.UserService;
import sumdu.edu.ua.web.service.MailService;

/** Автентифікація та авторизація: гість, USER, ADMIN, вхід, реєстрація, вихід. */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {

    @Autowired private MockMvc mvc;
    @Autowired private UserService users;

    /** Лист із кодом підтвердження перехоплюється, щоб отримати код у тесті. */
    @MockBean private NotificationPort notifier;
    @MockBean private MailService mailService;

    // ---------------------------------------------------------------- гість

    @Test
    void guestIsRedirectedToLoginPage() throws Exception {
        mvc.perform(get("/books"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void guestCanOpenLoginAndRegisterPages() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"username\"")));
        mvc.perform(get("/register")).andExpect(status().isOk());
    }

    @Test
    void guestGets401FromApi() throws Exception {
        mvc.perform(get("/api/books"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    // ---------------------------------------------------------------- USER

    @Test
    @WithMockUser(username = "Student", roles = "USER")
    void userCanViewBooksButSeesNoAdminButtons() throws Exception {
        mvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("/books/add"))))
                .andExpect(content().string(containsString("Student")));
    }

    @Test
    @WithMockUser(username = "Student", roles = "USER")
    void userCannotOpenAddBookForm() throws Exception {
        mvc.perform(get("/books/add"))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/403"));
    }

    @Test
    @WithMockUser(username = "Student", roles = "USER")
    void userCannotDeleteBook() throws Exception {
        mvc.perform(post("/books/1/delete").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "Student", roles = "USER")
    void userCanReadApiButCannotModifyIt() throws Exception {
        mvc.perform(get("/api/books")).andExpect(status().isOk());
        mvc.perform(post("/api/books").contentType("application/json")
                        .content("{\"title\":\"X\",\"author\":\"Y\",\"pubYear\":2000}"))
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------------- ADMIN

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminSeesAdminButtonsAndForm() throws Exception {
        mvc.perform(get("/books")).andExpect(content().string(containsString("/books/add")));
        mvc.perform(get("/books/add")).andExpect(status().isOk());
    }

    @Test
    void apiWorksWithHttpBasic() throws Exception {
        mvc.perform(get("/api/books").with(httpBasic("admin", "admin")))
                .andExpect(status().isOk());
    }

    // ---------------------------------------------------------------- вхід / вихід

    @Test
    void seedAdminCanLogInAndPasswordIsUpgradedToBcrypt() throws Exception {
        mvc.perform(formLogin("/login").user("admin").password("admin"))
                .andExpect(authenticated().withRoles("ADMIN"))
                .andExpect(redirectedUrl("/books"));

        String hash = users.findCredentials("admin").orElseThrow().getPasswordHash();
        assertTrue(hash.startsWith("{bcrypt}"), "password must be re-hashed with BCrypt, got " + hash);
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mvc.perform(formLogin("/login").user("admin").password("wrong"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    @WithMockUser(username = "Student", roles = "USER")
    void logoutRedirectsToLoginPage() throws Exception {
        mvc.perform(post("/logout").with(csrf()))
                .andExpect(redirectedUrl("/login?logout"));
    }

    // ---------------------------------------------------------------- реєстрація

    @Test
    void registrationRequiresEmailConfirmation() throws Exception {
        String name = "reader" + UUID.randomUUID().toString().substring(0, 8);

        mvc.perform(post("/register").with(csrf())
                        .param("username", name)
                        .param("email", name + "@mail.com")
                        .param("password", "secret123")
                        .param("confirmPassword", "secret123"))
                .andExpect(redirectedUrl("/login?registered"));

        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(notifier).sendAccountConfirmation(eq(name + "@mail.com"), eq(name), code.capture());

        // до підтвердження увійти не можна
        mvc.perform(formLogin("/login").user(name).password("secret123"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?disabled"));

        mvc.perform(get("/confirm").param("code", code.getValue()))
                .andExpect(redirectedUrl("/login?confirmed"));

        mvc.perform(formLogin("/login").user(name).password("secret123"))
                .andExpect(authenticated().withRoles("USER"));

        String hash = users.findCredentials(name).orElseThrow().getPasswordHash();
        assertTrue(hash.startsWith("{bcrypt}$2a$"));
    }

    @Test
    void registrationWithMismatchedPasswordsShowsForm() throws Exception {
        mvc.perform(post("/register").with(csrf())
                        .param("username", "someone")
                        .param("email", "someone@mail.com")
                        .param("password", "secret123")
                        .param("confirmPassword", "other123"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Паролі не збігаються")));
    }

    @Test
    void invalidConfirmationCode() throws Exception {
        mvc.perform(get("/confirm").param("code", "no-such-code"))
                .andExpect(redirectedUrl("/login?confirmError"));
    }
}
