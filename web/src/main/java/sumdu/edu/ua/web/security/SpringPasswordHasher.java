package sumdu.edu.ua.web.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import sumdu.edu.ua.core.port.PasswordHasher;

/** Реалізація порту core через PasswordEncoder Spring Security (BCrypt). */
@Component
public class SpringPasswordHasher implements PasswordHasher {

    private final PasswordEncoder encoder;

    public SpringPasswordHasher(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }
}
