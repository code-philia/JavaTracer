package org.cophi.javatracer.instrumentation.tracer;

public class EmptyTracer implements Tracer {

    private static EmptyTracer INSTANCE = null;

    public static EmptyTracer getInstance() {
        synchronized (EmptyTracer.class) {
            if (INSTANCE == null) {
                INSTANCE = new EmptyTracer();
            }
        }
        return INSTANCE;
    }

    @Override
    public void _afterInvoke(Object returnedValue, Object invokeObj, String invokeMethodSig,
        int line, String residingClassName, String residingMethodSignature,
        boolean needRevisiting) {

    }

    @Override
    public void _hitExceptionTarget(int line, String className, String methodSignature) {

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
        return 0;
    }

    @Override
    public String getThreadName() {
        return null;
    }
}
