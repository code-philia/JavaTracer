package org.cophi.javatracer.testcase.runner;

import org.cophi.javatracer.exceptions.UnknownTestCaseTypeException;
import java.util.Objects;
import org.cophi.javatracer.log.Log;
import org.cophi.javatracer.testcase.TestCase;
import org.cophi.javatracer.testcase.TestCaseType;
import org.cophi.javatracer.testcase.TestCaseTypeDetector;

/**
 * Factory class for test case runner. <br/>
 *
 * @author WYK
 */
public class TestCaseRunnerFactory {

    /**
     * Create a test case runner for the given test case.
     *
     * @param testCase the test case to be executed.
     * @return the test case runner for the given test case.
     * @throws NullPointerException if {@code testCase} is null.
     */
    public static TestCaseRunner createTestCaseRunner(final TestCase testCase) {
        Objects.requireNonNull(testCase, Log.genMessage("testCase should not be null",
            TestCaseRunnerFactory.class));
        try {
            // Detect the type of test case if AUTO is set.
            final TestCaseType testCaseType = testCase.testCaseType == TestCaseType.AUTO
                ? TestCaseTypeDetector.detectTestCaseType(testCase.testClassName,
                testCase.testMethodName)
                : testCase.testCaseType;
            return switch (testCaseType) {
                case JUNIT3 -> new Junit3TestCaseRunner(testCase);
                case JUNIT4 -> new Junit4TestCaseRunner(testCase);
                case JUNIT5 -> new Junit5TestCaseRunner(testCase);
                case TESTNG -> new TestNGTestCaseRunner(testCase);
                default -> throw new IllegalArgumentException(
                    "Unknown test case type: " + testCaseType);
            };
        } catch (UnknownTestCaseTypeException e) {
            Log.error(
                "Cannot detect the type of test case: " + testCase + ", please specify it manually",
                TestCaseRunnerFactory.class);
            throw new RuntimeException(e);
        }
    }
}
