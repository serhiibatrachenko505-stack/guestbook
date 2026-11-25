package sumdu.edu.ua.config;

import java.time.Duration;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

/**
 * Локалізація інтерфейсу.
 * Мова визначається так:
 *   1) параметр ?lang=en / ?lang=uk — перемикає мову і запам'ятовує її в cookie "lang";
 *   2) якщо cookie немає — мова браузера (заголовок Accept-Language);
 *   3) якщо заголовка немає — українська.
 */
@Configuration
public class LanguageConfig implements WebMvcConfigurer {

    public static final Locale DEFAULT_LOCALE = Locale.forLanguageTag("uk");

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver("lang");
        resolver.setCookieMaxAge(Duration.ofDays(30));
        resolver.setDefaultLocaleFunction(request ->
                request.getHeader("Accept-Language") == null ? DEFAULT_LOCALE : request.getLocale());
        return resolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
