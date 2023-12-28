package testcase.runner;

import java.lang.reflect.Method;
import log.Log;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import testcase.TestCase;
import testcase.TestCaseTypeDetector;

/**
 * Runs Junit3 test case. <br/>
 *
 * @author WYK
 * @see TestCaseRunner
 */
public class Junit3TestCaseRunner extends AbstractTestCaseRunner {

    /**
     * Constructor. <br/> To create a Junit3TestCaseRunner object, use
     * {@link TestCaseRunnerFactory}.
     *
     * @param testCase the test case to be executed.
     * @throws NullPointerException if {@code testCase} is null.
     */
    Junit3TestCaseRunner(final TestCase testCase) {
        super(testCase);
    }

    @Override
    public void runTestCase() {

        // Verify that the test case is Junit3 test case.
        if (!TestCaseTypeDetector.isJunit3(testCase.testClassName)) {
            throw new IllegalArgumentException(
                Log.genMessage("Given test case is not a Junit3 test class: " + this.testCase,
                    Junit3TestCaseRunner.class));
        }

        try {

            // Extract target test method.
            final Class<?> testClass = Class.forName(testCase.testClassName);
            final Method testMethod = testClass.getDeclaredMethod(testCase.testMethodName);

            // Run the test case in standard Junit way.
            final Request request = Request.method(testClass, testMethod.getName());
            final JUnitCore junitCore = new JUnitCore();

            // Add listener for executing instrumentation agent.
            junitCore.addListener(new RunListener() {
                @Override
                public void testStarted(Description description) throws Exception {
                    $testStarted(description.getClassName(), description.getMethodName());
                }

                @Override
                public void testFinished(Description description)
                    throws Exception {
                    $testFinished(description.getClassName(), description.getMethodName());
                }
            });

            // Collect test case result.
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
        } catch (NoSuchMethodException e) {
            Log.error("Cannot find the test method: " + testCase.testMethodName,
                Junit3TestCaseRunner.class);
            throw new RuntimeException(e);
        }
    }
}
