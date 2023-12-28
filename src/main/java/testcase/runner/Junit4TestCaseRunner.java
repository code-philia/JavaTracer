package testcase.runner;

import log.Log;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import testcase.TestCase;
import testcase.TestCaseTypeDetector;

/**
 * Runs Junit4 test case. <br/> To create a Junit4TestCaseRunner object, use
 * {@link TestCaseRunnerFactory}.
 *
 * @author WYK
 * @see TestCaseRunner
 */
public class Junit4TestCaseRunner extends AbstractTestCaseRunner {

    /**
     * Constructor.
     *
     * @param testCase the test case to be executed.
     * @throws NullPointerException if {@code testCase} is null.
     */
    Junit4TestCaseRunner(final TestCase testCase) {
        super(testCase);
    }

    @Override
    public void runTestCase() {
        // Verify that the test case is Junit4 test case.
        if (!TestCaseTypeDetector.isJunit4(testCase.testClassName, testCase.testMethodName)) {
            throw new IllegalArgumentException(Log.genMessage(
                "Given test case is not a Junit4 test class: " + this.testCase,
                Junit4TestCaseRunner.class));
        }

        try {

            // Extract target test method.
            final Class<?> testClass = Class.forName(testCase.testClassName);

            // Run the test case in standard Junit way.
            Request request = Request.method(testClass, testCase.testMethodName);
            final JUnitCore junitCore = new JUnitCore();
            // Add listener for executing instrumentation agent.
            junitCore.addListener(new RunListener() {
                @Override
                public void testStarted(org.junit.runner.Description description) throws Exception {
                    $testStarted(description.getClassName(), description.getMethodName());
                }

                @Override
                public void testFinished(org.junit.runner.Description description)
                    throws Exception {
                    $testFinished(description.getClassName(), description.getMethodName());
                }
            });

            // Collect the result of test case.
            final Result result = junitCore.run(request);
            final boolean isSuccessful = result.wasSuccessful();

            /*
            Note that we only run one test case so that failure list should only contain one element
            if the teat case fails.
             */
            String message = null;
            for (Failure failure : result.getFailures()) {
                Throwable exception = failure.getException();
                message = exception.getMessage();
            }
            final String resultMessage = isSuccessful ? "Test passed" : message;

            Log.info("Junit3 test case passed: " + isSuccessful);
            if (!isSuccessful) {
                Log.info("Junit3 test case failure message: " + resultMessage);
            }

            this.$exitProgram(isSuccessful + ";" + resultMessage);

        } catch (ClassNotFoundException e) {
            Log.error("Cannot find the test class: " + testCase.testClassName,
                Junit3TestCaseRunner.class);
            throw new RuntimeException(e);
        }
    }
}
