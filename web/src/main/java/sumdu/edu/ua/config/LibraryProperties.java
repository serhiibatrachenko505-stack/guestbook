package sumdu.edu.ua.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Власні параметри застосунку з application.yaml (префікс "library").
 * Spring Boot сам заповнює цей record під час старту.
 */
@ConfigurationProperties(prefix = "library")
public record LibraryProperties(String title, String version, Books books, Comments comments) {

    /** library.books.default-page-size */
    public record Books(int defaultPageSize) {
    }

    /** library.comments.delete-window-hours */
    public record Comments(int deleteWindowHours) {
    }
}
