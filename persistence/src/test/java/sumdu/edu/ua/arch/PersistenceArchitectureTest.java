package sumdu.edu.ua.arch;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sumdu.edu.ua.core.port.CatalogRepositoryPort;
import sumdu.edu.ua.core.port.CommentRepositoryPort;

/** Архітектурні правила модуля persistence. */
class PersistenceArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("sumdu.edu.ua");
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
    void portImplementationsShouldResideInPersistence() {
        classes()
                .that().implement(CatalogRepositoryPort.class)
                .or().implement(CommentRepositoryPort.class)
                .should().resideInAPackage("sumdu.edu.ua.persistence..")
                .check(classes);
    }

    @Test
    void persistenceShouldNotDependOnWebOrServlet() {
        noClasses()
                .that().resideInAPackage("sumdu.edu.ua.persistence..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("jakarta.servlet..", "sumdu.edu.ua.web..", "sumdu.edu.ua.config..")
                .check(classes);
    }

    @Test
    void persistenceShouldNotDependOnServices() {
        noClasses()
                .that().resideInAPackage("sumdu.edu.ua.persistence..")
                .should().dependOnClassesThat()
                .resideInAPackage("sumdu.edu.ua.core.service..")
                .because("persistence реалізує лише порти і не викликає бізнес-логіку")
                .check(classes);
    }
}
