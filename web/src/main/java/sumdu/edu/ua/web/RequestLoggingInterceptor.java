package sumdu.edu.ua.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Перехоплювач Spring MVC: показує, який метод контролера DispatcherServlet
 * вибрав для запиту, і з яким статусом завершилась обробка.
 */
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) {
        if (handler instanceof HandlerMethod hm) {
            log.info("{} {} -> {}.{}()", req.getMethod(), req.getRequestURI(),
                    hm.getBeanType().getSimpleName(), hm.getMethod().getName());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse resp, Object handler, Exception ex) {
        log.debug("{} {} completed with {}", req.getMethod(), req.getRequestURI(), resp.getStatus());
    }
}
