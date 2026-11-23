package sumdu.edu.ua.web.http;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sumdu.edu.ua.web.service.LibraryInfoService;

/**
 * GET /info — інформація про застосунок.
 * Демонстрація ін'єкції через поле (field injection): @Autowired та @Value.
 */
@Component
public class InfoServlet extends HttpServlet {

    @Autowired
    private transient LibraryInfoService infoService;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${server.port}")
    private int port;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("application", appName);
        body.put("port", port);
        body.putAll(infoService.info());
        Json.write(resp, 200, body);
    }
}
