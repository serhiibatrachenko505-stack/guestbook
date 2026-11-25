package sumdu.edu.ua.web;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

/** Форматування дат у шаблонах: th:text="${@dates.format(c.createdAt)}". */
@Component("dates")
public class Dates {
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault());

    public String format(Instant instant) {
        return instant == null ? "" : FORMAT.format(instant);
    }
}
