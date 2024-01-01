package testcase.runner;

import java.util.Arrays;
import java.util.Objects;
import log.Log;
import testcase.TestCase;
import testcase.TestCaseType;

/**
 * Abstract class for test case runner. <br/>
 * <p>
 * During instrumentation, this class is used to execute target test case if there are any. <br/>
 *
 * @author WYK
 */
public abstract class AbstractTestCaseRunner implements TestCaseRunner {

    /**
     * The test case to be executed.
     */
    protected final TestCase testCase;

    /**
     * Constructor.
     *
     * @param testCase the test case to be executed.
     * @throws NullPointerException if {@code testCase} is null.
     */
    AbstractTestCaseRunner(final TestCase testCase) {
        Objects.requireNonNull(testCase,
            Log.genMessage("testCase should not be null", this.getClass()));
        this.testCase = testCase;
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            throw new IllegalArgumentException(
                Log.genMessage(
                    "The number of arguments should be 3, with [test case class name], [test case method name], and [test case type], but "
                        + Arrays.toString(
                        args) + " is given", AbstractTestCaseRunner.class));
        }
        /*
        The input of the main is the following:
        1. The name of test case class.
        2. The name of test case method.
        3. The type of test case.
         */
        final String className = args[0];
        final String methodName = args[1];
        final TestCaseType testCaseType = TestCaseType.valueOf(args[2]);

        // Run the test case
        final TestCase testCase = new TestCase(className, methodName, testCaseType);
        final TestCaseRunner testCaseRunner = TestCaseRunnerFactory.createTestCaseRunner(testCase);
        testCaseRunner.runTestCase();
    }

    /**
     * This method will be executed when all test case is executed. <br/> The content of this method
     * will be injected by instrumentation agent. <br/>
     */
    protected void $exitProgram(final String resultMessage) {
        // For instrumentation agent
    }

    /**
     * This method will be executed whenever a test case is finished. <br/> The content of this
     * method will be injected by instrumentation agent. <br/>
     */
    protected void $testFinished(final String className, final String methodName) {
        // For instrumentation agent
    }

    /**
     * This method will be executed whenever a test case is started. <br/> The content of this
     * method will be injected by instrumentation agent. <br/>
     */
    protected void $testStarted(final String className, final String methodName) {
        // For instrumentation agent
    }
}
