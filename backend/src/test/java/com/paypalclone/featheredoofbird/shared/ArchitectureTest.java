package com.paypalclone.featheredoofbird.shared;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Verifies the modular package structure introduced by T-0001.
 *
 * <p>Rules enforce that:
 *
 * <ul>
 *   <li>Domain packages have no outward dependencies on infrastructure or application layers.
 *   <li>Application packages do not depend on infrastructure details (web / persistence).
 *   <li>REST controllers live exclusively in {@code infrastructure.web} packages.
 *   <li>Spring Data repository interfaces live in {@code infrastructure.persistence} packages.
 * </ul>
 */
@AnalyzeClasses(
        packages = "com.paypalclone.featheredoofbird",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String DOMAIN = "..domain..";
    private static final String APPLICATION = "..application..";
    private static final String INFRA = "..infrastructure..";
    private static final String INFRA_WEB = "..infrastructure.web..";
    private static final String INFRA_PERSISTENCE = "..infrastructure.persistence..";

    // ── Layer dependency rules ──────────────────────────────────────────

    @ArchTest
    static final ArchRule domain_should_not_depend_on_application =
            noClasses()
                    .that()
                    .resideInAPackage(DOMAIN)
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage(APPLICATION);

    @ArchTest
    static final ArchRule domain_should_not_depend_on_infrastructure =
            noClasses()
                    .that()
                    .resideInAPackage(DOMAIN)
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage(INFRA);

    @ArchTest
    static final ArchRule application_should_not_depend_on_web =
            noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage(INFRA_WEB);

    // ── Placement rules ─────────────────────────────────────────────────

    @ArchTest
    static final ArchRule controllers_should_reside_in_infrastructure_web =
            classes()
                    .that()
                    .haveSimpleNameEndingWith("Controller")
                    .should()
                    .resideInAPackage(INFRA_WEB);

    @ArchTest
    static final ArchRule jpa_repositories_should_reside_in_infrastructure_persistence =
            classes()
                    .that()
                    .haveSimpleNameEndingWith("Repository")
                    .and()
                    .areInterfaces()
                    .should()
                    .resideInAPackage(INFRA_PERSISTENCE);

    @ArchTest
    static final ArchRule stores_should_reside_in_infrastructure_persistence =
            classes()
                    .that()
                    .haveSimpleNameEndingWith("Store")
                    .should()
                    .resideInAPackage(INFRA_PERSISTENCE);
}
