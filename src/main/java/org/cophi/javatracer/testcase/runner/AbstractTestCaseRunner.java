package org.cophi.javatracer.testcase.runner;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import org.cophi.javatracer.instrumentation.tracer.InstrumentMethods;
import org.cophi.javatracer.log.Log;
import org.cophi.javatracer.testcase.TestCase;
import org.cophi.javatracer.testcase.TestCaseType;

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

    public enum Methods implements InstrumentMethods {
        EXIT_PROGRAM("$exitProgram", String.class),
        TEST_FINISHED("$testFinished", String.class, String.class),
        TEST_STARTED("$testStarted", String.class, String.class);

        private final String methodName;
        private final int argumentNumber;
        private final String descriptor;
        private final String declareClassName;

        Methods(final String methodName, Class<?>... argumentTypes) {
            try {
                final Class<?> clazz = AbstractTestCaseRunner.class;
                java.lang.reflect.Method method = clazz.getDeclaredMethod(methodName,
                    argumentTypes);
                this.declareClassName = method.getDeclaringClass().getName();
                this.descriptor = java.lang.invoke.MethodType.methodType(method.getReturnType(),
                    method.getParameterTypes()).toMethodDescriptorString();
                this.argumentNumber = this.isStatic(method) ? method.getParameterCount()
                    : method.getParameterCount() + 1;
                this.methodName = methodName;
            } catch (NoSuchMethodException e) {
                Log.fetal(e.getMessage(), this.getClass());
                throw new RuntimeException(e);
            }
        }

        @Override
        public int getArgumentNumber() {
            return this.argumentNumber;
        }

        @Override
        public String getDeclareClassBinaryName() {
            return this.declareClassName;
        }

        @Override
        public String getDescriptor() {
            return this.descriptor;
        }

        @Override
        public String getMethodName() {
            return this.methodName;
        }

        private boolean isStatic(final Method method) {
            return Modifier.isStatic(method.getModifiers());
        }
    }
}
