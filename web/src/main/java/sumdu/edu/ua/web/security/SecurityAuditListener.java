package sumdu.edu.ua.web.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.stereotype.Component;

/** Аудит подій безпеки: вхід, невдалий вхід, вихід. */
@Component
public class SecurityAuditListener {
    private static final Logger audit = LoggerFactory.getLogger("SECURITY-AUDIT");

    /** Успішний вхід через форму /login. */
    @EventListener
    public void onLogin(InteractiveAuthenticationSuccessEvent e) {
        audit.info("LOGIN success: user='{}', roles={}",
                e.getAuthentication().getName(), e.getAuthentication().getAuthorities());
    }

    /** Невірний пароль, неіснуючий або непідтверджений користувач. */
    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent e) {
        audit.warn("LOGIN failed: user='{}', reason={}",
                e.getAuthentication().getName(), e.getException().getClass().getSimpleName());
    }

    @EventListener
    public void onLogout(LogoutSuccessEvent e) {
        audit.info("LOGOUT: user='{}'", e.getAuthentication().getName());
    }
}
