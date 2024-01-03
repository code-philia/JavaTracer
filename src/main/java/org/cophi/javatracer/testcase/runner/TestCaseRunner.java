package org.cophi.javatracer.testcase.runner;

/**
 * Interface for test case runner. <br/> The execution process of the given test case will be
 * instrumented. <br/>
 *
 * @author WYK
 */
@FunctionalInterface
public interface TestCaseRunner {

    /**
     * Runs the target test case.
     */
    void runTestCase();
}
