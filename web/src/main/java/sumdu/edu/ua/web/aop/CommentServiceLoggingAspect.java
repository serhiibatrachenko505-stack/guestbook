package sumdu.edu.ua.web.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Логування викликів CommentService.delete(..): параметри, час виконання, помилки.
 * Бізнес-код сервісу при цьому не містить жодного рядка логування.
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)   // зовнішній аспект: вимірює час разом з усіма вкладеними
public class CommentServiceLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(CommentServiceLoggingAspect.class);

    @Around("execution(* sumdu.edu.ua.core.service.CommentService.delete(..))")
    public Object logDeleteCall(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();

        Object[] args = pjp.getArgs();
        Long bookId = (Long) args[0];
        Long commentId = (Long) args[1];
        Object createdAt = args.length > 2 ? args[2] : "(з БД)";

        log.info("Calling CommentService.delete(bookId={}, commentId={}, createdAt={})",
                bookId, commentId, createdAt);

        try {
            Object result = pjp.proceed();
            long time = System.currentTimeMillis() - start;
            log.info("CommentService.delete(bookId={}, commentId={}) finished in {} ms",
                    bookId, commentId, time);
            return result;
        } catch (Exception ex) {
            long time = System.currentTimeMillis() - start;
            log.warn("CommentService.delete(bookId={}, commentId={}) failed in {} ms: {} - {}",
                    bookId, commentId, time, ex.getClass().getSimpleName(), ex.getMessage());
            throw ex; // важливо: не ковтати виняток — його обробить GlobalApiExceptionHandler
        }
    }
}
