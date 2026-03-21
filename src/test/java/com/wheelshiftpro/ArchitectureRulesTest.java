package com.wheelshiftpro;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Architecture rules enforced automatically on every build via ArchUnit.
 *
 * <p>These tests exist to preserve the agreed layered architecture and prevent
 * accidental violations (e.g., controllers talking directly to repositories).
 * A failing ArchUnit test is a BUILD FAILURE — fix the architecture, don't
 * suppress the test.
 */
@AnalyzeClasses(
    packages = "com.wheelshiftpro",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class ArchitectureRulesTest {

    // -------------------------------------------------------------------------
    // Layer isolation rules
    // -------------------------------------------------------------------------

    @ArchTest
    static final ArchRule layeredArchitectureRule = layeredArchitecture()
        .consideringOnlyDependenciesInLayers()
        .layer("Controller").definedBy("com.wheelshiftpro.controller..")
        .layer("Service").definedBy("com.wheelshiftpro.service..")
        .layer("Repository").definedBy("com.wheelshiftpro.repository..")
        .layer("Entity").definedBy("com.wheelshiftpro.entity..")
        .layer("DTO").definedBy("com.wheelshiftpro.dto..")
        .layer("Mapper").definedBy("com.wheelshiftpro.mapper..")
        .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
        .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller", "Service")
        .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service");

    @ArchTest
    static final ArchRule controllersNeverAccessRepositoriesDirectly =
        noClasses()
            .that().resideInAPackage("..controller..")
            .should().dependOnClassesThat().resideInAPackage("..repository..")
            .because("Controllers must go through the service layer");

    @ArchTest
    static final ArchRule servicesShouldNotDependOnControllers =
        noClasses()
            .that().resideInAPackage("..service..")
            .should().dependOnClassesThat().resideInAPackage("..controller..")
            .because("Services must not have controller dependencies");

    // -------------------------------------------------------------------------
    // HTTP hygiene rules
    // -------------------------------------------------------------------------

    @ArchTest
    static final ArchRule servicesNeverUseHttpTypes =
        noClasses()
            .that().resideInAPackage("..service..")
            .should().dependOnClassesThat()
                .resideInAPackage("jakarta.servlet..")
            .because("Services must not depend on HTTP servlet types — pass data via DTOs");

    // -------------------------------------------------------------------------
    // Naming convention rules
    // -------------------------------------------------------------------------

    @ArchTest
    static final ArchRule controllersShouldBeSuffixedController =
        noClasses()
            .that().resideInAPackage("..controller..")
            .and().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
            .should().haveSimpleNameNotEndingWith("Controller")
            .because("All REST controllers must end with 'Controller'");

    @ArchTest
    static final ArchRule serviceImplsMustEndWithImpl =
        noClasses()
            .that().resideInAPackage("..service.impl..")
            .and().areNotInterfaces()
            .and().areNotAnonymousClasses()
            .and().areNotMemberClasses()
            .should().haveSimpleNameNotEndingWith("Impl")
            .because("Service implementation classes must end with 'Impl'");
}
