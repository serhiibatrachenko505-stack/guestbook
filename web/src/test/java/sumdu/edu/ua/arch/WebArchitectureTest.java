package sumdu.edu.ua.arch;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServlet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Архітектурні правила модуля web (імпортуються класи всіх трьох модулів). */
class WebArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("sumdu.edu.ua");
    }

    @Test
    void webShouldNotDependOnPersistence() {
        noClasses()
                .that().resideInAPackage("sumdu.edu.ua.web..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("sumdu.edu.ua.persistence..", "java.sql..", "org.h2..")
                .because("web працює лише з сервісами та портами core")
                .check(classes);
    }

    @Test
    void onlyConfigMayKnowPersistence() {
        noClasses()
                .that().resideOutsideOfPackages("sumdu.edu.ua.persistence..", "sumdu.edu.ua.config..")
                .should().dependOnClassesThat()
                .resideInAPackage("sumdu.edu.ua.persistence..")
                .check(classes);
    }

    @Test
    void servletsShouldResideOnlyInWeb() {
        classes()
                .that().areAssignableTo(HttpServlet.class)
                .or().haveSimpleNameEndingWith("Servlet")
                .should().resideInAPackage("sumdu.edu.ua.web..")
                .check(classes);
    }

    @Test
    void filtersShouldResideInWebError() {
        classes()
                .that().implement(Filter.class)
                .should().resideInAPackage("sumdu.edu.ua.web.error..")
                .check(classes);
    }

    @Test
    void repositoriesShouldResideOnlyInPersistence() {
        classes()
                .that().haveSimpleNameEndingWith("Repository")
                .or().haveSimpleNameEndingWith("Dao")
                .should().resideInAPackage("sumdu.edu.ua.persistence..")
                .check(classes);
    }

    @Test
    void coreShouldNotDependOnServletJdbcOrSpring() {
        noClasses()
                .that().resideInAPackage("sumdu.edu.ua.core..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("jakarta.servlet..", "java.sql..", "javax.sql..", "org.springframework..")
                .because("core — чиста бізнес-логіка; біни з неї створюються у web через @Bean")
                .check(classes);
    }

    @Test
    void configurationClassesShouldResideInConfig() {
        classes()
                .that().areAnnotatedWith(org.springframework.context.annotation.Configuration.class)
                .should().resideInAPackage("sumdu.edu.ua.config..")
                .check(classes);
    }
}
