package com.autosar.pdf;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Test suite for running all tests in the AUTOSAR PDF project.
 *
 * Usage:
 *   mvn test -Dtest=AllTests
 *   mvn test -Dtest=AllTests#testMethodName
 */
@Suite
@SuiteDisplayName("AUTOSAR PDF Test Suite")
@SelectPackages({
    "com.autosar.pdf.unit.domain",
    "com.autosar.pdf.unit.extraction",
    "com.autosar.pdf.unit.parser",
    "com.autosar.pdf.unit.writer",
    "com.autosar.pdf.unit.cli",
    "com.autosar.pdf.integration"
})
public class AllTests {
}