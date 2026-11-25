package sumdu.edu.ua.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import sumdu.edu.ua.web.RequestLoggingInterceptor;

/**
 * Налаштування Spring MVC.
 * На відміну від лаб. 5 тут немає @EnableWebMvc: вона вимикає автоконфігурацію MVC Spring Boot,
 * а нам вона потрібна — Boot сам підключає ThymeleafViewResolver, статичні файли, MessageSource,
 * Jackson. Через WebMvcConfigurer ми лише доповнюємо стандартні налаштування.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RequestLoggingInterceptor loggingInterceptor;

    public WebConfig(RequestLoggingInterceptor loggingInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/books/**", "/comments/**", "/api/**");
    }

    /** Головна сторінка -> каталог. */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/books");
    }
}
