package com.autosar.pdf;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Unit test suite for running all unit tests in the AUTOSAR PDF project.
 *
 * Usage:
 *   mvn test -Dtest=UnitTests
 */
@Suite
@SuiteDisplayName("AUTOSAR PDF Unit Tests")
@SelectPackages({
    "com.autosar.pdf.unit.domain",
    "com.autosar.pdf.unit.extraction",
    "com.autosar.pdf.unit.parser",
    "com.autosar.pdf.unit.writer",
    "com.autosar.pdf.unit.cli"
})
public class UnitTests {
}