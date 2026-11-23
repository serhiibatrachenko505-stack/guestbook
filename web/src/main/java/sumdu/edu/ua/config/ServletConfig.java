package sumdu.edu.ua.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sumdu.edu.ua.web.error.ErrorFilter;
import sumdu.edu.ua.web.http.BooksServlet;
import sumdu.edu.ua.web.http.InfoServlet;

/**
 * Замість web.xml: реєстрація сервлетів і фільтра у вбудованому Tomcat.
 * Самі сервлети — біни (@Component), Spring передає їх сюди вже з усіма залежностями.
 */
@Configuration
public class ServletConfig {

    @Bean
    public ServletRegistrationBean<BooksServlet> booksServletRegistration(BooksServlet servlet) {
        return new ServletRegistrationBean<>(servlet, "/books/*");
    }

    @Bean
    public ServletRegistrationBean<InfoServlet> infoServletRegistration(InfoServlet servlet) {
        return new ServletRegistrationBean<>(servlet, "/info");
    }

    @Bean
    public FilterRegistrationBean<ErrorFilter> errorFilterRegistration() {
        FilterRegistrationBean<ErrorFilter> reg = new FilterRegistrationBean<>(new ErrorFilter());
        reg.addUrlPatterns("/books/*", "/info");
        return reg;
    }
}
