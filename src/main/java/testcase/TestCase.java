package testcase;

import java.util.Objects;
import log.Log;

/**
 * This class is used to represent a test case. <br/> This class contain the name of the class in
 * fully qualified name format and the name of the test method. <br/> The test case type is also
 * stored in this class.
 *
 * @author WYK
 * @see TestCaseType
 */
public class TestCase {

    /**
     * Name of the target test class in fully qualified name format. <br/>
     */
    public final String testClassName;
    /**
     * Name of the target test method. <br/>
     */
    public final String testMethodName;
    /**
     * Type of the test case. <br/>
     *
     * @see TestCaseType
     */
    public final TestCaseType testCaseType;

    /**
     * Create a test case with the given test class name and test method name. <br/> The test case
     * type is set to {@link TestCaseType#AUTO} by default. <br/>
     *
     * @param testClassName  Name of the target test class in fully qualified name format
     * @param testMethodName Name of the target test method
     */
    public TestCase(final String testClassName, final String testMethodName) {
        this(testClassName, testMethodName, TestCaseType.AUTO);
    }

    /**
     * Create a test case with the given test class name, test method name and test case type.
     * <br/>
     *
     * @param testClassName  Name of the target test class in fully qualified name format
     * @param testMethodName Name of the target test method
     * @param testCaseType   Type of the test case
     * @throws NullPointerException     if any of the given parameters is null.
     * @throws IllegalArgumentException if any of the given parameters is blank.
     */
    public TestCase(final String testClassName, final String testMethodName,
        final TestCaseType testCaseType) {
        Objects.requireNonNull(testClassName,
            Log.genMessage("testClassName should not be null", this.getClass()));
        Objects.requireNonNull(testMethodName,
            Log.genMessage("testMethodName should not be null", this.getClass()));
        Objects.requireNonNull(testCaseType,
            Log.genMessage("testCaseType should not be null", this.getClass()));
        if (testClassName.isBlank()) {
            throw new IllegalArgumentException(
                Log.genMessage("testClassName should not be blank", this.getClass()));
        }
        if (testMethodName.isBlank()) {
            throw new IllegalArgumentException(
                Log.genMessage("testMethodName should not be blank", this.getClass()));
        }
        this.testClassName = testClassName;
        this.testMethodName = testMethodName;
        this.testCaseType = testCaseType;
    }

    /**
     * Create a test case with the given test case. <br/>
     *
     * @param testCase Test case to be copied
     */
    public TestCase(final TestCase testCase) {
        this(testCase.testClassName, testCase.testMethodName, testCase.testCaseType);
    }

    @Override
    public int hashCode() {
        int hashCode = this.getName().hashCode();
        hashCode = 31 * hashCode + this.testCaseType.hashCode();
        return hashCode;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TestCase other) {
            return this.getName().equals(other.getName())
                && this.testCaseType == other.testCaseType;
        }
        return false;
    }

    /**
     * Get the name of the test case in the format of "testClassName#testMethodName".
     *
     * @return Name of the test case
     */
    public String getName() {
        return this.testClassName + "#" + this.testMethodName;
    }
}
