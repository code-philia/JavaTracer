package instrumentation.runtime;

import instrumentation.instr.ClassLoaderInstrumenter;

public class ClassLoaderHandler extends EmptyExecutionTracer {

    private static final IExecutionTracer instance = new ClassLoaderHandler();
    private boolean tracerLockPreserve;

    public static IExecutionTracer getInstance() {
        return instance;
    }

    @Override
    public void _hitLine(int line, String className, String methodSignature, int numOfReadVars,
        int numOfWrittenVars, String bytecode) {
        if (line == ClassLoaderInstrumenter.ENTER_MARKER) {
            System.out.println("gLock: " + className);
//			tracerLockPreserve = ExecutionTracer.glock();
        } else if (line == ClassLoaderInstrumenter.EXIT_MARKER) {
//			ExecutionTracer.gUnlock(tracerLockPreserve);
        }
    }
}
