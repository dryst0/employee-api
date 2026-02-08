package com.jfi.api;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ApplicationTests {

    JavaClasses importedClasses;

    @BeforeEach
    void setup() {
        importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.jfi");
    }

    @Test
    void domainShouldNotDependOnAdapters() {
        ArchRuleDefinition.noClasses()
            .that()
            .resideInAnyPackage("..employee.domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..employee.adapter..")
            .because("Domain must not depend on adapters.")
            .allowEmptyShould(true)
            .check(importedClasses);
    }

    @Test
    void domainShouldNotDependOnUseCases() {
        ArchRuleDefinition.noClasses()
            .that()
            .resideInAnyPackage("..employee.domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..employee.usecase..")
            .because("Domain must not depend on use cases.")
            .allowEmptyShould(true)
            .check(importedClasses);
    }

    @Test
    void portsShouldNotDependOnAdapters() {
        ArchRuleDefinition.noClasses()
            .that()
            .resideInAnyPackage("..employee.port..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..employee.adapter..")
            .because("Ports must not depend on adapters.")
            .allowEmptyShould(true)
            .check(importedClasses);
    }

    @Test
    void useCasesShouldNotDependOnAdapters() {
        ArchRuleDefinition.noClasses()
            .that()
            .resideInAnyPackage("..employee.usecase..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..employee.adapter..")
            .because("Use cases must depend on ports, not adapters.")
            .allowEmptyShould(true)
            .check(importedClasses);
    }

    @Test
    void inboundAdaptersShouldNotDependOnOutboundAdapters() {
        ArchRuleDefinition.noClasses()
            .that()
            .resideInAnyPackage("..employee.adapter.in..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..employee.adapter.out..")
            .because("Inbound adapters must not depend on outbound adapters.")
            .allowEmptyShould(true)
            .check(importedClasses);
    }

    @Test
    void outboundAdaptersShouldNotDependOnInboundAdapters() {
        ArchRuleDefinition.noClasses()
            .that()
            .resideInAnyPackage("..employee.adapter.out..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..employee.adapter.in..")
            .because("Outbound adapters must not depend on inbound adapters.")
            .allowEmptyShould(true)
            .check(importedClasses);
    }
}
