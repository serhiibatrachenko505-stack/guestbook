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
import sumdu.edu.ua.core.port.UserRepositoryPort;

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
                .or().implement(UserRepositoryPort.class)
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

    @Test
    void noJdbcAnywhere() {
        noClasses()
                .that().resideInAPackage("sumdu.edu.ua..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("java.sql..", "javax.sql..")
                .because("доступ до даних виконується через JPA/Hibernate, а не через JDBC")
                .check(classes);
    }

    @Test
    void entitiesShouldResideInPersistenceEntityPackage() {
        classes()
                .that().areAnnotatedWith(jakarta.persistence.Entity.class)
                .should().resideInAPackage("sumdu.edu.ua.persistence.jpa.entity..")
                .andShould().haveSimpleNameEndingWith("Entity")
                .check(classes);
    }

    @Test
    void entitiesShouldNotLeakToCore() {
        noClasses()
                .that().resideInAPackage("sumdu.edu.ua.core..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("sumdu.edu.ua.persistence..", "jakarta.persistence..")
                .because("доменна модель core не залежить від ORM")
                .check(classes);
    }
}
