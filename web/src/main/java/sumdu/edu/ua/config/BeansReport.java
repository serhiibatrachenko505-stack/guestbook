package sumdu.edu.ua.config;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Після старту виводить у лог усі біни нашого застосунку (пакет sumdu.edu.ua) —
 * для перевірки, що контейнер їх знайшов і створив.
 */
@Component
public class BeansReport implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(BeansReport.class);

    private final ApplicationContext context;

    public BeansReport(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("===== Application beans (sumdu.edu.ua) =====");
        Arrays.stream(context.getBeanDefinitionNames())
                .sorted()
                .forEach(name -> {
                    Class<?> type = context.getType(name);
                    if (type != null && type.getName().startsWith("sumdu.edu.ua")) {
                        log.info("  {} -> {}", name, type.getName());
                    }
                });
        log.info("Total beans in context: {}", context.getBeanDefinitionCount());
    }
}
