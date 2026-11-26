package sumdu.edu.ua.core.port;

import java.util.List;
import java.util.Optional;
import sumdu.edu.ua.core.domain.User;
import sumdu.edu.ua.core.domain.UserCredentials;

/** Контракт доступу до користувачів. Реалізація — у модулі persistence (JPA). */
public interface UserRepositoryPort {

    Optional<User> findById(long id);

    Optional<User> findByUsername(String username);

    List<User> findAll();

    /** Користувач без пароля (автор відгуку через REST API), активний одразу. */
    User create(String username, String role);

    // ---- автентифікація та реєстрація ----

    Optional<UserCredentials> findCredentials(String username);

    boolean existsByEmail(String email);

    /** Новий непідтверджений акаунт із кодом підтвердження. */
    User register(String username, String email, String passwordHash, String role, String confirmationCode);

    /** Активує акаунт за кодом; порожній результат — код не знайдено. */
    Optional<User> confirm(String confirmationCode);

    void updatePassword(String username, String passwordHash);
}
