package org.cophi.javatracer.instrumentation.tracer;

import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.cophi.javatracer.configs.ProjectConfig;
import org.cophi.javatracer.instrumentation.agents.LoadedClassRecord;
import org.cophi.javatracer.instrumentation.tracer.factories.ExecutionTracerFactory;
import org.cophi.javatracer.log.Log;
import org.cophi.javatracer.model.Trace;
import org.cophi.javatracer.utils.NamingUtils;

public class ExecutionTracer implements Tracer {

    protected final long threadId;
    protected final String threadName;
    protected final ManagerDelegate managerDelegate;

    protected final ProjectConfig projectConfig;

    Trace trace = new Trace();


    public ExecutionTracer(final long threadId, final String threadName,
        ProjectConfig projectConfig) {
        this.threadId = threadId;
        this.threadName = threadName;
        this.managerDelegate = new ManagerDelegate(threadId);
        this.projectConfig = projectConfig;
    }

    public synchronized static Tracer _getTracer(boolean isInternalClass, String className,
        String methodSig, int methodStartLine, int methodEndLine, String paramNamesCode,
        String paramTypeSignsCode, Object[] params) {

        TracerManager manager = TracerManager.getInstance();
        try {

            final long threadId = Thread.currentThread().getId();
            final String threadName = Thread.currentThread().getName();

            // If the test already stated, and it is the internal class, then start recording.
            if (manager.getState() == TracerState.TEST_STARTED && isInternalClass) {
                manager.setState(TracerState.RECORDING);
                ExecutionTracerFactory.getInstance().setMainThreadId(threadId);
            }

            // Do not record anything if it is not recording.
            if (manager.getState() != TracerState.RECORDING) {
                return EmptyTracer.getInstance();
            }

            // Do not record anything if the thread is locked.
            if (manager.isLocked(threadId)) {
                return EmptyTracer.getInstance();
            }

            manager.lock(threadId);
            ExecutionTracer tracer = ExecutionTracerFactory.getInstance()
                .getOrCreateTracer(threadId, threadName);

            manager.unlock(threadId);

            System.out.println("Calling getTrace");
            return tracer;

        } catch (Throwable throwable) {
            Log.error(throwable.getLocalizedMessage());
            throw throwable;
        }
    }

    @Override
    public void _afterInvoke(Object returnedValue, Object invokeObj, String invokeMethodSig,
        int line, String residingClassName, String residingMethodSignature,
        boolean needRevisiting) {

    }

    @Override
    public void _hitExceptionTarget(int line, String className, String methodSignature) {
        Log.info("hitExceptionTarget: " + line + " " + className + " " + methodSignature);
    }

    @Override
    public void _hitInvoke(Object invokeObj, String invokeTypeSign, String methodName,
        Object[] params, String paramTypeSignsCode, String returnTypeSign, int line,
        String className, String methodSignature) {

    }

    @Override
    public void _hitInvokeStatic(String invokeTypeSign, String methodName, Object[] params,
        String paramTypeSignsCode, String returnTypeSign, int line, String className,
        String methodSignature) {

    }

    @Override
    public void _hitLine(int line, String className, String methodSignature, int numOfReadVars,
        int numOfWrittenVars, String bytecode) {
        Log.info("hitLine: " + line + " " + className + " " + methodSignature + " " + numOfReadVars
            + " " + numOfWrittenVars + " " + bytecode);
    }

    @Override
    public void _hitMethodEnd(int line, String className, String methodSignature) {

    }

    @Override
    public void _hitReturn(Object returnObj, String returnGeneralType, int line, String className,
        String methodSignature) {

    }

    @Override
    public void _hitVoidReturn(int line, String className, String methodSignature) {

    }

    @Override
    public void _iincLocalVar(Object varValue, Object varValueAfter, String varName, String varType,
        int line, int bcLocalVarIdx, int varScopeStartLine, int varScopeEndLine, String className,
        String methodSignature) {

    }

    @Override
    public void _readArrayElementVar(Object arrayRef, int index, Object eleValue,
        String elementType, int line, String className, String methodSignature) {

    }

    @Override
    public void _readField(Object refValue, Object fieldValue, String fieldName, String fieldType,
        int line, String className, String methodSignature) {

    }

    @Override
    public void _readLocalVar(Object varValue, String varName, String varType, int line,
        int bcLocalVarIdx, int varScopeStartLine, int varScopeEndLine, String className,
        String methodSignature) {

    }

    @Override
    public void _readStaticField(Object fieldValue, String refType, String fieldName,
        String fieldTypeSign, int line, String className, String methodSignature) {

    }

    @Override
    public void _start() {

    }

    @Override
    public void _writeArrayElementVar(Object arrayRef, int index, Object eleValue,
        String elementType, int line, String className, String methodSignature) {

    }

    @Override
    public void _writeField(Object refValue, Object fieldValue, String fieldName,
        String fieldTypeSign, int line, String className, String methodSignature) {

    }

    @Override
    public void _writeLocalVar(Object varValue, String varName, String varType, int line,
        int bcLocalVarIdx, int varScopeStartLine, int varScopeEndLine, String className,
        String methodSignature) {

    }

    @Override
    public void _writeStaticField(Object fieldValue, String refType, String fieldName,
        String fieldType, int line, String className, String methodSignature) {

    }

    @Override
    public long getThreadId() {
        return this.threadId;
    }

    @Override
    public String getThreadName() {
        return this.threadName;
    }

    public void enterMethod(final String className, final String methodSignature,
        final int methodStartLine, int methodEndLine, final String paramTypeSignatureCode,
        final String paramNamesCode, final Object[] params) {
        final TracerManager manager = TracerManager.getInstance();

        final boolean isExclusive = LoadedClassRecord.getInstance().isExclusive(className);
        if (isExclusive) {
            this.managerDelegate.unlock();
            return;
        } else {

        }
    }

    public Trace getTrace() {
        return this.trace;
    }

    public void hitLine(final int lineNumber, final String className,
        final String methodSignature) {
        this._hitLine(lineNumber, className, methodSignature, -1, -1, null);
    }

    public enum Methods implements InstrumentMethods {
        GET_TRACER("_getTracer", boolean.class, String.class, String.class, int.class, int.class,
            String.class, String.class, Object[].class);

        private final String declareClassBinaryName;

        private final String methodName;

        private final String descriptor;

        private final int argumentNumber;

        Methods(final String methodName, final Class<?>... argumentTypes) {
            try {
                final Class<?> clazz = ExecutionTracer.class;
                Method method = clazz.getDeclaredMethod(methodName, argumentTypes);
                this.declareClassBinaryName = NamingUtils.canonicalToClassBinaryName(
                    method.getDeclaringClass().getName());
                this.descriptor = MethodType.methodType(method.getReturnType(),
                    method.getParameterTypes()).toMethodDescriptorString();
                this.argumentNumber = this.isStatic(method) ? method.getParameterCount()
                    : method.getParameterCount() + 1;
                this.methodName = methodName;
            } catch (NoSuchMethodException e) {
                Log.fetal("Cannot fine method " + methodName, Method.class);
                Log.fetal(e.getMessage());
                throw new RuntimeException(e);
            }
        }

        @Override
        public int getArgumentNumber() {
            return argumentNumber;
        }

        @Override
        public String getDeclareClassBinaryName() {
            return declareClassBinaryName;
        }

        @Override
        public String getDescriptor() {
            return descriptor;
        }

        @Override
        public String getMethodName() {
            return methodName;
        }

        private boolean isStatic(final Method method) {
            return Modifier.isStatic(method.getModifiers());
        }
    }

    protected static class ManagerDelegate {

        protected final long threadId;
        protected final TracerManager manager = TracerManager.getInstance();

        public ManagerDelegate(final long threadId) {
            this.threadId = threadId;
        }

        public boolean isLocked() {
            return this.manager.isLocked(this.threadId);
        }

        public void lock() {
            this.manager.lock(this.threadId);
        }

        public void unlock(final boolean preservedLock) {
            if (!preservedLock) {
                this.unlock();
            }
        }

        public void unlock() {
            this.manager.unlock(this.threadId);
        }
    }
}
