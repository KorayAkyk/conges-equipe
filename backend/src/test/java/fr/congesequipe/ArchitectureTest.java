package fr.congesequipe;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/** Les couches ne se melangent pas : le web ne touche pas la base, le domaine ignore Spring et JPA. */
@AnalyzeClasses(packages = "fr.congesequipe", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule couches = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Web").definedBy("..web..")
            .layer("Securite").definedBy("..securite..")
            .layer("Service").definedBy("..service..")
            .layer("Persistance").definedBy("..persistance..")
            .layer("Domaine").definedBy("..domaine..")
            .layer("Demo").definedBy("..demo..")
            .whereLayer("Web").mayNotBeAccessedByAnyLayer()
            .whereLayer("Securite").mayOnlyBeAccessedByLayers("Web")
            .whereLayer("Service").mayOnlyBeAccessedByLayers("Web", "Securite", "Demo")
            .whereLayer("Persistance").mayOnlyBeAccessedByLayers("Service", "Demo")
            .whereLayer("Demo").mayNotBeAccessedByAnyLayer();

    @ArchTest
    static final ArchRule domainePur = noClasses().that().resideInAPackage("..domaine..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta.persistence..", "org.hibernate..");
}
