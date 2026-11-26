package sumdu.edu.ua.web.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/** Сторінки помилок: /403 (бракує прав), /404 та /error-page (після redirect з MvcExceptionHandler). */
@Controller
public class ErrorPagesController {

    /** Сюди Spring Security перенаправляє (forward) при AccessDeniedException — accessDeniedPage("/403"). */
    @RequestMapping("/403")
    public String forbidden(HttpServletResponse resp) {
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return "403";
    }

    /**
     * Сторінка після невдалої дії з форми. Дані (status, titleKey, details) приходять
     * як flash-атрибути redirect-у і вже лежать у Model.
     */
    @RequestMapping("/error-page")
    public String errorPage(Model model) {
        if (!model.containsAttribute("status")) {
            model.addAttribute("status", 400);
            model.addAttribute("titleKey", "error.generic");
        }
        return "error";
    }

    @RequestMapping("/404")
    public String notFound(HttpServletResponse resp, Model model) {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        model.addAttribute("status", 404);
        model.addAttribute("titleKey", "error.notFound");
        return "error";
    }
}
