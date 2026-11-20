package sumdu.edu.ua.web.http;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Спільний ObjectMapper і допоміжні методи для JSON-відповідей. */
public final class Json {
    public static final String CONTENT_TYPE = "application/json;charset=UTF-8";

    public static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private Json() {}

    public static void write(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setContentType(CONTENT_TYPE);
        MAPPER.writeValue(resp.getOutputStream(), body);
    }

    public static <T> T read(HttpServletRequest req, Class<T> type) throws IOException {
        return MAPPER.readValue(req.getInputStream(), type);
    }
}
