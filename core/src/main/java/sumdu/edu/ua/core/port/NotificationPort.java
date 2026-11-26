package sumdu.edu.ua.core.port;

/** Надсилання повідомлень користувачам. Реалізація — у web (лист через Gmail SMTP + FreeMarker). */
public interface NotificationPort {

    void sendAccountConfirmation(String email, String username, String confirmationCode);
}
