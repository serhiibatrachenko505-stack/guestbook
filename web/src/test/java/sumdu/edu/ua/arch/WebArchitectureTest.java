package sumdu.edu.ua.arch;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import jakarta.servlet.http.HttpServlet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
                .because("контролери працюють лише з сервісами core")
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
    void restControllersShouldResideInWebApi() {
        classes()
                .that().areAnnotatedWith(RestController.class)
                .should().resideInAPackage("sumdu.edu.ua.web.api..")
                .because("JSON-контролери відокремлені від контролерів сторінок")
                .check(classes);
    }

    @Test
    void viewControllersShouldResideInWebController() {
        classes()
                .that().areAnnotatedWith(Controller.class)
                .should().resideInAPackage("sumdu.edu.ua.web.controller..")
                .andShould().haveSimpleNameEndingWith("Controller")
                .check(classes);
    }

    @Test
    void noServletsAnymore() {
        noClasses()
                .that().resideInAPackage("sumdu.edu.ua..")
                .should().beAssignableTo(HttpServlet.class)
                .because("у Spring MVC запити обробляють контролери, а не сервлети")
                .check(classes);
    }

    @Test
    void exceptionHandlersShouldResideInWebError() {
        classes()
                .that().areAnnotatedWith(RestControllerAdvice.class)
                .or().areAnnotatedWith(ControllerAdvice.class)
                .should().resideInAPackage("sumdu.edu.ua.web.error..")
                .check(classes);
    }

    @Test
    void configurationClassesShouldResideInConfig() {
        classes()
                .that().areAnnotatedWith(Configuration.class)
                .should().resideInAPackage("sumdu.edu.ua.config..")
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
                .check(classes);
    }
}
