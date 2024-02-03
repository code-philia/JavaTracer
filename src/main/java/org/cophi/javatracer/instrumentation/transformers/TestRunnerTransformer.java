package org.cophi.javatracer.instrumentation.transformers;

import java.io.IOException;
import java.security.ProtectionDomain;
import org.cophi.javatracer.configs.ProjectConfig;
import org.cophi.javatracer.instrumentation.instrumentator.TestRunnerInstrumentator;
import org.cophi.javatracer.instrumentation.tracer.TracerManager;
import org.cophi.javatracer.instrumentation.tracer.TracerState;
import org.cophi.javatracer.log.Log;
import org.cophi.javatracer.testcase.runner.AbstractTestCaseRunner;
import org.cophi.javatracer.testcase.runner.Junit3TestCaseRunner;
import org.cophi.javatracer.testcase.runner.Junit4TestCaseRunner;
import org.cophi.javatracer.testcase.runner.Junit5TestCaseRunner;
import org.cophi.javatracer.testcase.runner.TestNGTestCaseRunner;

public class TestRunnerTransformer extends AbstractTransformer {

    protected TestRunnerInstrumentator instrumentator;

    public TestRunnerTransformer(ProjectConfig projectConfig) {
        super(projectConfig);
        this.instrumentator = new TestRunnerInstrumentator();
    }

    protected byte[] instrument(final String className, final byte[] classfileBuffer)
        throws IOException {
        return this.instrumentator.instrument(className, classfileBuffer);
    }

    @Override
    protected byte[] transform_(ClassLoader loader, String className,
        Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
        byte[] classfileBuffer) {

        if (TracerManager.getInstance().getState() == TracerState.SHUTDOWN) {
            return null;
        }

        if (className.equals(Junit3TestCaseRunner.class.getCanonicalName()) ||
            className.equals(Junit4TestCaseRunner.class.getCanonicalName()) ||
            className.equals(Junit5TestCaseRunner.class.getCanonicalName()) ||
            className.equals(TestNGTestCaseRunner.class.getCanonicalName()) ||
            className.equals(AbstractTestCaseRunner.class.getCanonicalName())) {
            try {
                return this.instrument(className, classfileBuffer);
            } catch (Throwable throwable) {
                Log.error(throwable.getLocalizedMessage(), this.getClass());
                return classfileBuffer;
            }
        }
        return null;
    }

}
