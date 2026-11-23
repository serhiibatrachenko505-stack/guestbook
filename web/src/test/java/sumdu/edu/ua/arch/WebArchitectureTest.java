package sumdu.edu.ua.arch;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
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
    void controllersShouldResideOnlyInWeb() {
        classes()
                .that().haveSimpleNameEndingWith("Controller")
                .should().resideInAPackage("sumdu.edu.ua.web.controller..")
                .check(classes);
    }

    @Test
    void javalinShouldBeUsedOnlyInWeb() {
        noClasses()
                .that().resideOutsideOfPackage("sumdu.edu.ua.web..")
                .should().dependOnClassesThat()
                .resideInAPackage("io.javalin..")
                .because("HTTP-фреймворк — деталь веб-рівня; core і persistence його не знають")
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
    void coreShouldNotDependOnHttpOrJdbc() {
        noClasses()
                .that().resideInAPackage("sumdu.edu.ua.core..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("jakarta.servlet..", "io.javalin..", "java.sql..", "javax.sql..")
                .check(classes);
    }
}
