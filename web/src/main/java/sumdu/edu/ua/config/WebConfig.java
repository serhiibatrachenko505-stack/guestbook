package sumdu.edu.ua.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import sumdu.edu.ua.web.RequestLoggingInterceptor;

/**
 * Конфігурація Spring MVC.
 * @EnableWebMvc вмикає анотаційний режим MVC (обробку @RestController, @GetMapping ...)
 * і замінює автоконфігурацію MVC Spring Boot, тому частину налаштувань задаємо тут явно.
 * DispatcherServlet при цьому реєструє Spring Boot автоматично.
 */
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    private final ObjectMapper objectMapper;
    private final RequestLoggingInterceptor loggingInterceptor;

    public WebConfig(ObjectMapper objectMapper, RequestLoggingInterceptor loggingInterceptor) {
        this.objectMapper = objectMapper;
        this.loggingInterceptor = loggingInterceptor;
    }

    /** Jackson з налаштуваннями Spring Boot (дати як "2026-11-23T10:00:00Z", а не числа). */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter jackson) {
                jackson.setObjectMapper(objectMapper);
            }
        }
    }

    /** Логування кожного запиту до API. */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/books/**", "/api/**", "/comments/**", "/info");
    }

    /** Статичні сторінки index.html, book.html з classpath:/static. */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }

    /** Головна сторінка. */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/index.html");
    }
}
