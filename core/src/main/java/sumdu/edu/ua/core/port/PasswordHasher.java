package sumdu.edu.ua.core.port;

/** Хешування паролів. Реалізація — у web (PasswordEncoder Spring Security, BCrypt). */
public interface PasswordHasher {

    String hash(String rawPassword);
}
