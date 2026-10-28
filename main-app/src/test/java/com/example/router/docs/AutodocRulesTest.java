package com.example.router.docs;

import com.example.autodoc.test.AutodocRules;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.example.router", importOptions = ImportOption.DoNotIncludeTests.class)
class AutodocRulesTest {

    @ArchTest
    static final ArchTests autodoc = ArchTests.in(AutodocRules.class);

    @ArchTest
    static final ArchRule partnerClientCallsAreSteps = AutodocRules.downstreamCallsAreSteps("com.example.partnerclients..");
}
