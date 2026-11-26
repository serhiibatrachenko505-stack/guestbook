package sumdu.edu.ua.config;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;
import sumdu.edu.ua.web.error.ErrorBody;

/**
 * Конфігурація Spring Security. Два ланцюжки фільтрів:
 *  1) /api/**  — REST API: HTTP Basic, без сесій і CSRF, гість -> 401 JSON, бракує прав -> 403 JSON;
 *  2) решта    — HTML-сторінки: форма входу /login, сесія, CSRF, бракує прав -> сторінка /403.
 * Ролі: ADMIN — повний доступ, USER — перегляд книг і відгуків, додавання відгуків.
 */
@Configuration
@EnableMethodSecurity(jsr250Enabled = true)   // @PreAuthorize у web і @RolesAllowed у сервісах core
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    /**
     * BCrypt для нових паролів. DelegatingPasswordEncoder зберігає хеш із префіксом алгоритму
     * ({bcrypt}$2a$10$...) і розуміє старі тестові паролі {noop}, які після входу оновлюються до BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // ------------------------------------------------------------------ REST API

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurity(HttpSecurity http, ObjectMapper mapper) throws Exception {
        http
            .securityMatcher(antMatcher("/api/**"))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(antMatcher(HttpMethod.GET, "/api/**")).hasAnyRole("USER", "ADMIN")
                .anyRequest().hasRole("ADMIN"))
            .httpBasic(basic -> basic.authenticationEntryPoint(
                    (req, resp, ex) -> json(mapper, req, resp, 401, "Unauthorized", "Authentication required")))
            .exceptionHandling(ex -> ex.accessDeniedHandler(
                    (req, resp, e) -> json(mapper, req, resp, 403, "Forbidden", "Access denied")))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable());   // API без cookies-сесії, тому CSRF не потрібен
        return http.build();
    }

    // ------------------------------------------------------------------ HTML-сторінки

    @Bean
    @Order(2)
    public SecurityFilterChain webSecurity(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // статичні ресурси і сторінки для гостей
                .requestMatchers(antMatcher("/style.css"), antMatcher("/css/**"), antMatcher("/js/**"),
                        antMatcher("/images/**"), antMatcher("/img/**"), antMatcher("/favicon.ico")).permitAll()
                .requestMatchers(antMatcher("/login"), antMatcher("/register"), antMatcher("/confirm"),
                        antMatcher("/error"), antMatcher("/403"), antMatcher("/404"),
                        antMatcher("/error-page")).permitAll()
                // H2 Console — лише адміністратор
                .requestMatchers(PathRequest.toH2Console()).hasRole("ADMIN")
                // керування книгами — лише ADMIN
                .requestMatchers(antMatcher("/books/add"), antMatcher("/books/new"),
                        antMatcher("/books/*/delete")).hasRole("ADMIN")
                .requestMatchers(antMatcher(HttpMethod.POST, "/comments/delete")).hasRole("ADMIN")
                // відгуки — USER і ADMIN
                .requestMatchers(antMatcher(HttpMethod.POST, "/comments")).hasAnyRole("USER", "ADMIN")
                // перегляд каталогу та користувачів — USER і ADMIN (гостям заборонено)
                .requestMatchers(antMatcher(HttpMethod.GET, "/books/**"),
                        antMatcher(HttpMethod.GET, "/users/**")).hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")                       // власна сторінка templates/auth/login.html
                .loginProcessingUrl("/login")              // POST username + password
                .defaultSuccessUrl("/books", true)
                .failureHandler(failureHandler())
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll())
            .exceptionHandling(ex -> ex.accessDeniedPage("/403"))
            // H2 Console працює у фреймах і має власні форми
            .csrf(csrf -> csrf.ignoringRequestMatchers(PathRequest.toH2Console()))
            .headers(h -> h.frameOptions(f -> f.sameOrigin()));
        return http.build();
    }

    /** Непідтверджений акаунт -> /login?disabled, неправильний пароль -> /login?error. */
    private ExceptionMappingAuthenticationFailureHandler failureHandler() {
        var handler = new ExceptionMappingAuthenticationFailureHandler();
        handler.setExceptionMappings(Map.of(DisabledException.class.getName(), "/login?disabled"));
        handler.setDefaultFailureUrl("/login?error");
        return handler;
    }

    private static void json(ObjectMapper mapper, HttpServletRequest req, HttpServletResponse resp,
                             int status, String error, String message) throws IOException {
        log.warn("{} {} -> {} {}", req.getMethod(), req.getRequestURI(), status, message);
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        mapper.writeValue(resp.getOutputStream(),
                new ErrorBody(Instant.now(), status, error, message, req.getRequestURI()));
    }
}
