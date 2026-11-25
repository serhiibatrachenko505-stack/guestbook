package sumdu.edu.ua.web.api;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import sumdu.edu.ua.web.service.LibraryInfoService;

/** GET /api/info — інформація про застосунок (JSON). */
@RestController
public class InfoController {

    private final LibraryInfoService infoService;
    private final String appName;

    public InfoController(LibraryInfoService infoService,
                          @Value("${spring.application.name}") String appName) {
        this.infoService = infoService;
        this.appName = appName;
    }

    @GetMapping("/api/info")
    public Map<String, Object> info() {   // @RestController => @ResponseBody => JSON
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("application", appName);
        body.putAll(infoService.info());
        return body;
    }
}
