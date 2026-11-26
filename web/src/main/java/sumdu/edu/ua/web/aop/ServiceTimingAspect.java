package sumdu.edu.ua.web.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Вимірювання часу виконання всіх методів сервісного шару core.
 * Кожен виклик пишеться на рівні DEBUG, повільні (довші за поріг) — на рівні WARN.
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class ServiceTimingAspect {

    private static final Logger log = LoggerFactory.getLogger(ServiceTimingAspect.class);

    private final long slowThresholdMs;

    public ServiceTimingAspect(@Value("${library.aop.slow-threshold-ms:200}") long slowThresholdMs) {
        this.slowThresholdMs = slowThresholdMs;
    }

    /** Усі методи всіх класів пакета sumdu.edu.ua.core.service та його підпакетів. */
    @Pointcut("execution(* sumdu.edu.ua.core.service..*(..))")
    public void serviceLayer() {
    }

    @Around("serviceLayer()")
    public Object measure(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.nanoTime();
        String method = pjp.getSignature().getDeclaringType().getSimpleName() + "." + pjp.getSignature().getName();
        boolean ok = false;
        try {
            Object result = pjp.proceed();
            ok = true;
            return result;
        } finally {
            long ms = (System.nanoTime() - start) / 1_000_000;
            if (ms > slowThresholdMs) {
                log.warn("[timing] {} took {} ms (slow, threshold {} ms){}", method, ms, slowThresholdMs,
                        ok ? "" : " — failed");
            } else {
                log.debug("[timing] {} took {} ms{}", method, ms, ok ? "" : " — failed");
            }
        }
    }
}
