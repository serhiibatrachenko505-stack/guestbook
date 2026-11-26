package sumdu.edu.ua.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sumdu.edu.ua.core.domain.User;
import sumdu.edu.ua.core.exception.ConflictException;
import sumdu.edu.ua.core.exception.ValidationException;

/** Реєстрація та підтвердження акаунта без БД і без пошти. */
class UserServiceTest {

    private CommentServiceFakePortsTest.FakeUsers users;
    private final List<String> sentCodes = new ArrayList<>();
    private UserService service;

    @BeforeEach
    void setUp() {
        users = new CommentServiceFakePortsTest.FakeUsers();
        service = new UserService(users, raw -> "HASH(" + raw + ")",
                (email, username, code) -> sentCodes.add(code));
    }

    @Test
    void registrationHashesPasswordAndSendsCode() {
        User u = service.register("reader", "Reader@Mail.com", "secret123");

        assertEquals("USER", u.getRole());
        assertFalse(u.isEnabled());                                  // до підтвердження
        assertEquals("reader@mail.com", u.getEmail());               // email у нижньому регістрі
        assertEquals("HASH(secret123)", users.hashes.get("reader")); // пароль не зберігається відкрито
        assertEquals(1, sentCodes.size());
    }

    @Test
    void confirmationCodeActivatesAccountOnce() {
        service.register("reader", "reader@mail.com", "secret123");
        String code = sentCodes.get(0);

        assertTrue(service.confirm(code).isPresent());
        assertFalse(service.confirm(code).isPresent());              // код одноразовий
        assertFalse(service.confirm("wrong").isPresent());
    }

    @Test
    void shortPasswordIsRejected() {
        assertThrows(ValidationException.class, () -> service.register("reader", "r@mail.com", "123"));
    }

    @Test
    void invalidEmailIsRejected() {
        assertThrows(ValidationException.class, () -> service.register("reader", "not-an-email", "secret123"));
    }

    @Test
    void duplicateUsernameOrEmailIsRejected() {
        service.register("reader", "reader@mail.com", "secret123");
        assertThrows(ConflictException.class, () -> service.register("reader", "other@mail.com", "secret123"));
        assertThrows(ConflictException.class, () -> service.register("other", "reader@mail.com", "secret123"));
    }

    @Test
    void registeredUserGetsId() {
        assertNotNull(service.register("reader", "reader@mail.com", "secret123"));
    }
}
