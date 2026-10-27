package com.example.router.docs;

import com.example.flowcap.test.FlowcapRules;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.example.router", importOptions = ImportOption.DoNotIncludeTests.class)
class FlowcapRulesTest {

    @ArchTest
    static final ArchTests flowcap = ArchTests.in(FlowcapRules.class);

    @ArchTest
    static final ArchRule partnerClientCallsAreSteps = FlowcapRules.downstreamCallsAreSteps("com.example.partnerclients..");
}
