package sumdu.edu.ua.core.service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import sumdu.edu.ua.core.domain.User;
import sumdu.edu.ua.core.domain.UserCredentials;
import sumdu.edu.ua.core.exception.ConflictException;
import sumdu.edu.ua.core.exception.NotFoundException;
import sumdu.edu.ua.core.exception.ValidationException;
import sumdu.edu.ua.core.port.NotificationPort;
import sumdu.edu.ua.core.port.PasswordHasher;
import sumdu.edu.ua.core.port.UserRepositoryPort;

/** Бізнес-логіка користувачів: пошук, реєстрація з підтвердженням email, дані для входу. */
public class UserService {
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final int MIN_PASSWORD = 6;
    public static final int MAX_EMAIL = 128;

    private final UserRepositoryPort users;
    private final PasswordHasher hasher;
    private final NotificationPort notifier;

    public UserService(UserRepositoryPort users, PasswordHasher hasher, NotificationPort notifier) {
        this.users = users;
        this.hasher = hasher;
        this.notifier = notifier;
    }

    /** Для тестів і сценаріїв без реєстрації. */
    public UserService(UserRepositoryPort users) {
        this(users, raw -> "{noop}" + raw, (email, username, code) -> { });
    }

    public User findById(long id) {
        PageRules.checkId(id, "userId");
        return users.findById(id)
                .orElseThrow(() -> new NotFoundException("User " + id + " not found"));
    }

    public List<User> findAll() {
        return users.findAll();
    }

    /** Повертає користувача з таким іменем або створює нового з роллю USER (без пароля). */
    @Transactional
    public User findOrCreate(String username) {
        String name = BookService.required(username, "username", CommentService.MAX_AUTHOR);
        return users.findByUsername(name).orElseGet(() -> users.create(name, ROLE_USER));
    }

    /**
     * Реєстрація: перевірка даних, хешування пароля, роль USER, акаунт неактивний
     * до підтвердження email, генерується одноразовий код і надсилається лист.
     */
    @Transactional
    public User register(String username, String email, String rawPassword) {
        String name = BookService.required(username, "username", CommentService.MAX_AUTHOR);
        String mail = BookService.required(email, "email", MAX_EMAIL).toLowerCase();
        if (!mail.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new ValidationException("email is invalid");
        }
        if (rawPassword == null || rawPassword.length() < MIN_PASSWORD) {
            throw new ValidationException("password must be at least " + MIN_PASSWORD + " characters");
        }
        if (users.findByUsername(name).isPresent()) {
            throw new ConflictException("Username is already taken");
        }
        if (users.existsByEmail(mail)) {
            throw new ConflictException("Email is already registered");
        }

        String code = UUID.randomUUID().toString();
        User user = users.register(name, mail, hasher.hash(rawPassword), ROLE_USER, code);
        notifier.sendAccountConfirmation(mail, name, code);
        return user;
    }

    /** Підтвердження email за кодом із листа. */
    @Transactional
    public Optional<User> confirm(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return users.confirm(code.trim());
    }

    /** Дані для входу (викликає UserDetailsService у web). */
    public Optional<UserCredentials> findCredentials(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return users.findCredentials(username.trim());
    }

    /** Збереження нового хешу пароля (автоматичне оновлення {noop} -> BCrypt під час входу). */
    @Transactional
    public void updatePasswordHash(String username, String passwordHash) {
        users.updatePassword(username, passwordHash);
    }
}
