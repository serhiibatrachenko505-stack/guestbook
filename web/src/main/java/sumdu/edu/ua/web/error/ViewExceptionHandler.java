package sumdu.edu.ua.web.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import sumdu.edu.ua.core.exception.NotFoundException;
import sumdu.edu.ua.core.exception.ValidationException;

/**
 * Обробка помилок HTML-сторінок (пакет web.controller): замість JSON показується шаблон error.html.
 */
@ControllerAdvice(basePackages = "sumdu.edu.ua.web.controller")
public class ViewExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ViewExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public String notFound(NotFoundException e, HttpServletRequest req, HttpServletResponse resp, Model model) {
        return page(404, "error.notFound", e.getMessage(), req, resp, model);
    }

    @ExceptionHandler({ValidationException.class, MethodArgumentTypeMismatchException.class})
    public String badRequest(Exception e, HttpServletRequest req, HttpServletResponse resp, Model model) {
        return page(400, "error.badRequest", e.getMessage(), req, resp, model);
    }

    private String page(int status, String titleKey, String details,
                        HttpServletRequest req, HttpServletResponse resp, Model model) {
        log.warn("{} {} -> {} {}", req.getMethod(), req.getRequestURI(), status, details);
        resp.setStatus(status);
        model.addAttribute("status", status);
        model.addAttribute("titleKey", titleKey);
        model.addAttribute("details", details);
        return "error";
    }
}
