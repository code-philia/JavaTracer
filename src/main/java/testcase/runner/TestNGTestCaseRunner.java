package testcase.runner;

import java.util.ArrayList;
import java.util.List;
import log.Log;
import org.testng.ITestContext;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import testcase.TestCase;
import testcase.TestCaseTypeDetector;

/**
 * Test case runner for TestNG test case. <br/> To create a TestNGTestCaseRunner object, use
 * {@link TestCaseRunnerFactory}.
 *
 * @author WYK
 * @see TestCaseRunner
 */
public class TestNGTestCaseRunner extends AbstractTestCaseRunner {

    /**
     * Constructor.
     *
     * @param testCase the test case to be executed.
     * @throws NullPointerException if {@code testCase} is null.
     */
    TestNGTestCaseRunner(final TestCase testCase) {
        super(testCase);
    }

    @Override
    public void runTestCase() {
        // Verify that the test case is TestNG test case.
        if (!TestCaseTypeDetector.isTestNG(testCase.testClassName, testCase.testMethodName)) {
            throw new IllegalArgumentException(
                Log.genMessage("Given test case is not a TestNG test class: " + this.testCase,
                    Junit3TestCaseRunner.class));
        }

        // Run the test case in standard TestNG way.
        XmlSuite suite = new XmlSuite();
        suite.setName("TmpSuite");

        XmlTest test = new XmlTest(suite);
        test.setName("My Test");

        XmlClass xmlClass = new XmlClass(testCase.testClassName);
        List<XmlInclude> includeMethods = new ArrayList<>();
        includeMethods.add(new XmlInclude(testCase.testMethodName));
        xmlClass.setIncludedMethods(includeMethods);

        List<XmlClass> classes = new ArrayList<>();
        classes.add(xmlClass);
        test.setXmlClasses(classes);

        List<XmlSuite> suites = new ArrayList<>();
        suites.add(suite);

        TestNG testng = new TestNG();
        testng.setXmlSuites(suites);

        final String[] message = {null};
        // Add listener for executing instrumentation agent.
        TestListenerAdapter tla = new TestListenerAdapter() {
            @Override
            public void onTestFailure(org.testng.ITestResult tr) {
                message[0] = tr.getThrowable().getMessage();
                super.onTestFailure(tr);
            }

            @Override
            public void onStart(ITestContext context) {
                super.onStart(context);
                $testStarted(testCase.testClassName, testCase.testMethodName);
            }

            @Override
            public void onFinish(ITestContext context) {
                $testFinished(testCase.testClassName, testCase.testMethodName);
                super.onFinish(context);
            }
        };
        testng.setUseDefaultListeners(false);
        testng.addListener(tla);
        testng.run();

        // Collect test case result.
        boolean isSuccessful = !testng.hasFailure();
        if (isSuccessful) {
            message[0] = "Test passed";
        }

        Log.info("TestNG test case passed: " + isSuccessful);
        if (!isSuccessful) {
            Log.info("TestNG test case failure message: " + message[0]);
        }

        this.$exitProgram(isSuccessful + ";" + message[0]);
    }
}
