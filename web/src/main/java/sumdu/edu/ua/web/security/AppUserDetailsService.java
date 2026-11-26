package sumdu.edu.ua.web.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sumdu.edu.ua.core.domain.UserCredentials;
import sumdu.edu.ua.core.service.UserService;

/**
 * Власний UserDetailsService: Spring Security завантажує користувача з БД
 * (через UserService core -> JPA-адаптер), а не з пам'яті.
 *
 * UserDetailsPasswordService дозволяє Spring Security автоматично перехешувати пароль
 * під час входу, якщо він збережений застарілим способом (наприклад {noop} -> {bcrypt}).
 */
@Service
public class AppUserDetailsService implements UserDetailsService, UserDetailsPasswordService {
    private static final Logger log = LoggerFactory.getLogger(AppUserDetailsService.class);

    private final UserService users;

    public AppUserDetailsService(UserService users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserCredentials c = users.findCredentials(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (c.getPasswordHash() == null) {
            // користувач створений автоматично (автор відгуку через API) і не має пароля
            throw new UsernameNotFoundException("User has no password: " + username);
        }

        return User.withUsername(c.getUsername())
                .password(c.getPasswordHash())
                .roles(c.getRole())              // "ADMIN" -> authority ROLE_ADMIN
                .disabled(!c.isEnabled())        // непідтверджений email -> DisabledException
                .build();
    }

    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        log.info("Upgrading password hash of '{}' to {}", user.getUsername(),
                newPassword.substring(0, Math.min(8, newPassword.length())) + "...");
        users.updatePasswordHash(user.getUsername(), newPassword);
        return User.withUserDetails(user).password(newPassword).build();
    }
}
