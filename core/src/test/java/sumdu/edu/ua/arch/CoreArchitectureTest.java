package sumdu.edu.ua.arch;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.constructors;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Архітектурні правила модуля core. */
class CoreArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("sumdu.edu.ua.core");
    }

    @Test
    void coreShouldNotDependOnServletOrJdbc() {
        noClasses()
                .that().resideInAPackage("sumdu.edu.ua.core..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("jakarta.servlet..", "java.sql..", "javax.sql..", "org.h2..", "org.springframework..")
                .because("доменний шар не повинен знати про HTTP, JDBC та Spring")
                .check(classes);
    }

    @Test
    void coreShouldNotDependOnOuterModules() {
        noClasses()
                .that().resideInAPackage("sumdu.edu.ua.core..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("sumdu.edu.ua.persistence..", "sumdu.edu.ua.web..", "sumdu.edu.ua.config..")
                .check(classes);
    }

    @Test
    void portsShouldBeInterfaces() {
        classes()
                .that().resideInAPackage("..core.port..")
                .should().beInterfaces()
                .check(classes);
    }

    @Test
    void servicesShouldResideInServicePackage() {
        classes()
                .that().haveSimpleNameEndingWith("Service")
                .should().resideInAPackage("..core.service..")
                .check(classes);
    }

    /** Відповідь на питання 4 звіту: усі класи core мають лише публічні конструктори. */
    @Test
    void coreClassesShouldHavePublicConstructors() {
        constructors()
                .that().areDeclaredInClassesThat().resideInAPackage("sumdu.edu.ua.core..")
                .should().bePublic()
                .check(classes);
    }
}
