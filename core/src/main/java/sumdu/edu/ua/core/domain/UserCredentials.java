package sumdu.edu.ua.core.domain;

/**
 * Дані для автентифікації: ім'я, хеш пароля, роль і ознака підтвердженого акаунта.
 * Використовуються лише під час входу (web перетворює їх на UserDetails Spring Security).
 */
public class UserCredentials {
    private final String username;
    private final String passwordHash;
    private final String role;
    private final boolean enabled;

    public UserCredentials(String username, String passwordHash, String role, boolean enabled) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.enabled = enabled;
    }

    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public boolean isEnabled() { return enabled; }
}
