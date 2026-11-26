package sumdu.edu.ua.core.service;

import jakarta.transaction.Transactional;
import java.util.List;
import sumdu.edu.ua.core.domain.User;
import sumdu.edu.ua.core.exception.NotFoundException;
import sumdu.edu.ua.core.port.UserRepositoryPort;

/** Бізнес-логіка користувачів. */
public class UserService {
    public static final String DEFAULT_ROLE = "USER";

    private final UserRepositoryPort users;

    public UserService(UserRepositoryPort users) {
        this.users = users;
    }

    public User findById(long id) {
        PageRules.checkId(id, "userId");
        return users.findById(id)
                .orElseThrow(() -> new NotFoundException("User " + id + " not found"));
    }

    public List<User> findAll() {
        return users.findAll();
    }

    /** Повертає користувача з таким іменем або створює нового з роллю USER. */
    @Transactional
    public User findOrCreate(String username) {
        String name = BookService.required(username, "username", CommentService.MAX_AUTHOR);
        return users.findByUsername(name).orElseGet(() -> users.create(name, DEFAULT_ROLE));
    }
}
