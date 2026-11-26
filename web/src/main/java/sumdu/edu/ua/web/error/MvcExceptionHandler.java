package sumdu.edu.ua.web.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.RequestContextUtils;
import sumdu.edu.ua.core.exception.NotFoundException;
import sumdu.edu.ua.core.exception.ValidationException;

/**
 * Обробка помилок HTML-сторінок (MVC-контролери з пакета web.controller).
 *  - GET  (відкриття сторінки)  -> одразу сторінка error.html з відповідним статусом;
 *  - POST (відправка форми)     -> redirect:/error-page з повідомленням у flash-атрибутах
 *    (Post/Redirect/Get: оновлення сторінки не повторює невдалу дію).
 * Винятки видалення коментаря та BookNotFound раніше перехоплює GlobalApiExceptionHandler.
 */
@ControllerAdvice(basePackages = "sumdu.edu.ua.web.controller")
@Order(Ordered.LOWEST_PRECEDENCE)
public class MvcExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(MvcExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public String notFound(NotFoundException e, HttpServletRequest req, HttpServletResponse resp, Model model) {
        return handle(404, "error.notFound", e.getMessage(), req, resp, model);
    }

    @ExceptionHandler({ValidationException.class, MethodArgumentTypeMismatchException.class})
    public String badRequest(Exception e, HttpServletRequest req, HttpServletResponse resp, Model model) {
        return handle(400, "error.badRequest", e.getMessage(), req, resp, model);
    }

    private String handle(int status, String titleKey, String details,
                          HttpServletRequest req, HttpServletResponse resp, Model model) {
        log.warn("{} {} -> {} {}", req.getMethod(), req.getRequestURI(), status, details);

        if ("POST".equalsIgnoreCase(req.getMethod())) {
            FlashMap flash = RequestContextUtils.getOutputFlashMap(req);
            flash.put("status", status);
            flash.put("titleKey", titleKey);
            flash.put("details", details);
            return "redirect:/error-page";
        }

        resp.setStatus(status);
        model.addAttribute("status", status);
        model.addAttribute("titleKey", titleKey);
        model.addAttribute("details", details);
        return "error";
    }
}
