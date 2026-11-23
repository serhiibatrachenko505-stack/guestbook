package sumdu.edu.ua.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Точка входу. @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan.
 * scanBasePackages = "sumdu.edu.ua" — щоб знайти біни у всіх модулях (web, persistence, config).
 */
@SpringBootApplication(scanBasePackages = "sumdu.edu.ua")
public class AppInit {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(AppInit.class, args);
        String port = ctx.getEnvironment().getProperty("server.port", "8080");
        System.out.println("\nStarted at http://localhost:" + port + "/\n");
    }
}
