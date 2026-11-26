package sumdu.edu.ua.web.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/** Сторінки помилок доступу: /403 (бракує прав) та /404. */
@Controller
public class ErrorPagesController {

    /** Сюди Spring Security перенаправляє (forward) при AccessDeniedException — accessDeniedPage("/403"). */
    @RequestMapping("/403")
    public String forbidden(HttpServletResponse resp) {
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return "403";
    }

    @RequestMapping("/404")
    public String notFound(HttpServletResponse resp, Model model) {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        model.addAttribute("status", 404);
        model.addAttribute("titleKey", "error.notFound");
        return "error";
    }
}
