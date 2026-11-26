package sumdu.edu.ua.web.service;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sumdu.edu.ua.core.port.NotificationPort;

/**
 * Реалізація порту core: лист для підтвердження акаунта (FreeMarker-шаблон + Gmail SMTP з ЛР7).
 * Посилання також пишеться в лог — щоб підтвердити акаунт, навіть якщо пошта не налаштована.
 */
@Component
public class AccountMailNotifier implements NotificationPort {
    private static final Logger log = LoggerFactory.getLogger(AccountMailNotifier.class);

    private final MailService mailService;
    private final EmailTemplateProcessor templates;
    private final String baseUrl;

    public AccountMailNotifier(MailService mailService, EmailTemplateProcessor templates,
                               @Value("${app.base-url:http://localhost:8080}") String baseUrl) {
        this.mailService = mailService;
        this.templates = templates;
        this.baseUrl = baseUrl;
    }

    @Override
    public void sendAccountConfirmation(String email, String username, String confirmationCode) {
        String link = baseUrl + "/confirm?code=" + confirmationCode;
        log.info("Confirmation link for '{}': {}", username, link);

        Map<String, Object> model = new HashMap<>();
        model.put("username", username);
        model.put("link", link);
        try {
            String html = templates.process("confirm_account.ftl", model);
            mailService.sendHtml(email, "Підтвердження реєстрації", html);
        } catch (Exception e) {
            // реєстрація не скасовується через збій пошти: посилання є в лозі
            log.warn("Confirmation email to {} was not sent: {}", email, e.getMessage());
        }
    }
}
