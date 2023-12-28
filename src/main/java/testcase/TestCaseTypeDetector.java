package testcase;

import exceptions.UnknownTestCaseTypeException;
import java.lang.reflect.Method;
import log.Log;

/**
 * Detects the type of test case. <br/>
 *
 * @author WYK
 * @see TestCaseType
 */
public class TestCaseTypeDetector {

    /**
     * Private constructor. <br/> TestCaseTypeDetector is a utility class and should not be
     * instantiated.
     */
    private TestCaseTypeDetector() {
        throw new IllegalStateException(Log.genMessage("Utility class", this.getClass()));
    }

    /**
     * Detects the type of test case.
     *
     * @param testClassName  the name of test case class to be detected
     * @param testMethodName the name of test case method to be detected
     * @return the type of test case.
     * @throws UnknownTestCaseTypeException if the type of test case cannot be detected.
     * @see TestCaseType
     */
    public static TestCaseType detectTestCaseType(final String testClassName,
        final String testMethodName)
        throws UnknownTestCaseTypeException {
        if (TestCaseTypeDetector.isJunit3(testClassName)) {
            return TestCaseType.JUNIT3;
        } else if (TestCaseTypeDetector.isJunit4(testClassName, testMethodName)) {
            return TestCaseType.JUNIT4;
        } else if (TestCaseTypeDetector.isJunit5(testClassName, testMethodName)) {
            return TestCaseType.JUNIT5;
        } else if (TestCaseTypeDetector.isTestNG(testClassName, testMethodName)) {
            return TestCaseType.TESTNG;
        } else {
            throw new UnknownTestCaseTypeException(
                Log.genMessage("Unknown test case type", TestCaseTypeDetector.class));
        }
    }

    /**
     * Detects if the test case is JUnit 3.
     *
     * @param testClassName the name of test case class to be detected
     * @return {@code True} if the test case is JUnit 3, {@code False} otherwise.
     */
    public static boolean isJunit3(final String testClassName) {
        try {
            // Junit3 test case is required to extends junit.framework.TestCase.
            final Class<?> testClass = Class.forName(testClassName);
            return junit.framework.TestCase.class.isAssignableFrom(testClass);
        } catch (ClassNotFoundException e) {
            Log.error("Class not found: " + testClassName, TestCaseTypeDetector.class);
            throw new RuntimeException(e);
        }
    }

    /**
     * Detects if the test case is JUnit 4.
     *
     * @param testClassName  the name of test case class to be detected
     * @param testMethodName the name of test case method to be detected
     * @return {@code True} if the test case is JUnit 4, {@code False} otherwise.
     */
    public static boolean isJunit4(final String testClassName, final String testMethodName) {
        try {
            // Junit4 test case is required to have @Test annotation.
            // This @Test annotation is from org.junit.Test, which differ from Junit5 and TestNG.
            final Class<?> testClass = Class.forName(testClassName);
            final Method method = testClass.getDeclaredMethod(testMethodName);
            return method.getAnnotation(org.junit.Test.class) != null;
        } catch (ClassNotFoundException e) {
            Log.error("Class not found: " + testClassName, TestCaseTypeDetector.class);
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            Log.error("Method not found: " + testMethodName, TestCaseTypeDetector.class);
            throw new RuntimeException(e);
        }
    }

    /**
     * Detects if the test case is JUnit 5.
     *
     * @param testClassName  the name of test case class to be detected
     * @param testMethodName the name of test case method to be detected
     * @return {@code True} if the test case is JUnit 5, {@code False} otherwise.
     */
    public static boolean isJunit5(final String testClassName, final String testMethodName) {
        try {
            // Junit5 test case is required to have @Test annotation.
            // This @Test annotation is from org.junit.jupiter.api, which differ from Junit4 and TestNG.
            final Class<?> testClass = Class.forName(testClassName);
            final Method method = testClass.getDeclaredMethod(testMethodName);
            return method.getAnnotation(org.junit.jupiter.api.Test.class) != null;
        } catch (ClassNotFoundException e) {
            Log.error("Class not found: " + testClassName, TestCaseTypeDetector.class);
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            Log.error("Method not found: " + testMethodName, TestCaseTypeDetector.class);
            throw new RuntimeException(e);
        }
    }

    /**
     * Detects if the test case is TestNG.
     *
     * @param testClassName  the name of test case class to be detected
     * @param testMethodName the name of test case method to be detected
     * @return {@code True} if the test case is TestNG, {@code False} otherwise.
     */
    public static boolean isTestNG(final String testClassName, final String testMethodName) {
        try {
            // TestNG test case is required to have @Test annotation.
            // This @Test annotation is from org.testng, which differ from Junit4 and Junit5.
            final Class<?> testClass = Class.forName(testClassName);
            final Method method = testClass.getDeclaredMethod(testMethodName);
            return method.getAnnotation(org.testng.annotations.Test.class) != null;
        } catch (ClassNotFoundException e) {
            Log.error("Class not found: " + testClassName, TestCaseTypeDetector.class);
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            Log.error("Method not found: " + testMethodName, TestCaseTypeDetector.class);
            throw new RuntimeException(e);
        }
    }
}
