package org.cophi.javatracer.testcase;

/**
 * Enum for test case type.
 *
 * @author WYK
 */
public enum TestCaseType {
    /**
     * JUnit 3 test case.
     */
    JUNIT3,
    /**
     * JUnit 4 test case.
     */
    JUNIT4,
    /**
     * JUnit 5 test case.
     */
    JUNIT5,
    /**
     * TestNG test case.
     */
    TESTNG,
    /**
     * Auto-detect test case type.
     */
    AUTO,
}
