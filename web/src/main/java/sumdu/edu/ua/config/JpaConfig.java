package sumdu.edu.ua.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Де шукати JPA-сутності та Spring Data репозиторії.
 * Без цього Spring Boot шукає їх лише в пакеті класу AppInit (sumdu.edu.ua.web),
 * а вони лежать у модулі persistence. Пакети задано рядками, тому web не залежить
 * від класів persistence на рівні коду.
 */
@Configuration
@EntityScan(basePackages = "sumdu.edu.ua.persistence.jpa.entity")
@EnableJpaRepositories(basePackages = "sumdu.edu.ua.persistence.jpa.repository")
public class JpaConfig {
}
