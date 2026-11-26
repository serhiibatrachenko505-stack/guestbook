package sumdu.edu.ua.core.port;

import java.util.List;
import java.util.Optional;
import sumdu.edu.ua.core.domain.User;

/** Контракт доступу до користувачів. Реалізація — у модулі persistence (JPA). */
public interface UserRepositoryPort {

    Optional<User> findById(long id);

    Optional<User> findByUsername(String username);

    List<User> findAll();

    User create(String username, String role);
}
