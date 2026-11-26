package sumdu.edu.ua.core.domain;

/** Користувач системи. Пароль у доменній моделі не зберігається (див. UserCredentials). */
public class User {
    private final long id;
    private final String username;
    private final String email;
    private final String role;
    private final boolean enabled;

    public User(long id, String username, String email, String role, boolean enabled) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.enabled = enabled;
    }

    public User(long id, String username, String role) {
        this(id, username, null, role, true);
    }

    public long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public boolean isEnabled() { return enabled; }
}
