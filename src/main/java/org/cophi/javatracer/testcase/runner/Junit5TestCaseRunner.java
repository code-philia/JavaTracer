package org.cophi.javatracer.testcase.runner;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;

import java.lang.reflect.Method;
import org.cophi.javatracer.log.Log;
import org.cophi.javatracer.testcase.TestCase;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.junit.platform.launcher.listeners.TestExecutionSummary.Failure;
import org.cophi.javatracer.testcase.TestCaseTypeDetector;

/**
 * Runs Junit5 test case. <br/> To create a Junit5TestCaseRunner object, use
 * {@link TestCaseRunnerFactory}.
 *
 * @author WYK
 * @see TestCaseRunner
 */
public class Junit5TestCaseRunner extends AbstractTestCaseRunner {

    /**
     * Constructor.
     *
     * @param testCase the test case to be executed.
     * @throws NullPointerException if {@code testCase} is null.
     */
    Junit5TestCaseRunner(final TestCase testCase) {
        super(testCase);
    }

    @Override
    public void runTestCase() {
        // Verify that the test case is Junit5 test case.
        if (!TestCaseTypeDetector.isJunit5(testCase.testClassName, testCase.testMethodName)) {
            throw new IllegalArgumentException(
                Log.genMessage("Given test case is not a Junit5 test class: " + this.testCase,
                    Junit3TestCaseRunner.class));
        }

        try {
            // Extract target test method.
            final Class<?> testClass = Class.forName(testCase.testClassName);
            final Method testMethod = testClass.getDeclaredMethod(testCase.testMethodName);

            // Run the test case in standard Junit way.
            LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectMethod(testClass, testMethod))
                .build();
            Launcher launcher = LauncherFactory.create();

            // Add listener for executing instrumentation agent.
            SummaryGeneratingListener listener = new SummaryGeneratingListener() {
                @Override
                public void executionStarted(TestIdentifier testIdentifier) {
                    super.executionStarted(testIdentifier);
                    if (this.isTargetMethod(testIdentifier.getDisplayName())) {
                        $testStarted(testCase.testClassName, testCase.testMethodName);
                    }
                }

                @Override
                public void executionFinished(TestIdentifier testIdentifier,
                    TestExecutionResult testExecutionResult) {
                    if (this.isTargetMethod(testIdentifier.getDisplayName())) {
                        $testFinished(testCase.testClassName, testCase.testMethodName);
                    }
                    super.executionFinished(testIdentifier, testExecutionResult);
                }

                private boolean isTargetMethod(final String methodName) {
                    return methodName.startsWith(testCase.testMethodName + "(");
                }
            };
            launcher.registerTestExecutionListeners(listener);
            launcher.execute(request);

            // Collect test case result.
            TestExecutionSummary summary = listener.getSummary();
            final boolean isSuccessful = summary.getTotalFailureCount() == 0;

            /*
            Note that we only run one test case so that failure list should only contain one element
            if the teat case fails.
             */
            String message = null;
            for (Failure failure : summary.getFailures()) {
                Throwable exception = failure.getException();
                message = exception.getMessage();
            }
            final String resultMessage = isSuccessful ? "Test passed" : message;

            Log.info("Junit5 test case passed: " + isSuccessful);
            if (!isSuccessful) {
                Log.info("Junit5 test case failure message: " + resultMessage);
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
