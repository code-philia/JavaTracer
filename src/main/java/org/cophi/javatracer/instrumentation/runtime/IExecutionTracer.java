package org.cophi.javatracer.instrumentation.runtime;

public interface IExecutionTracer {

    /*
     * VERY IMPORTANT NODE:
     * Please make sure that the package name of IExecutionTracer and the method signature are the same as the one in
     * TracerMethods.java.
     */
    void _hitInvoke(Object invokeObj, String invokeTypeSign, String methodName,
        Object[] params,
        String paramTypeSignsCode, String returnTypeSign, int line, String className,
        String methodSignature);

    void _hitInvokeStatic(String invokeTypeSign, String methodName, Object[] params,
        String paramTypeSignsCode,
        String returnTypeSign, int line, String className, String methodSignature);

    void _hitReturn(Object returnObj, String returnGeneralType, int line, String className,
        String methodSignature);

    void _hitVoidReturn(int line, String className, String methodSignature);

    void _hitLine(int line, String className, String methodSignature, int numOfReadVars,
        int numOfWrittenVars, String bytecode);

    void _hitExeptionTarget(int line, String className, String methodSignature);

    void _writeField(Object refValue, Object fieldValue, String fieldName,
        String fieldTypeSign, int line, String className, String methodSignature);

    void _writeStaticField(Object fieldValue, String refType, String fieldName,
        String fieldType, int line, String className, String methodSignature);

    void _readField(Object refValue, Object fieldValue, String fieldName, String fieldType,
        int line, String className, String methodSignature);

    void _readStaticField(Object fieldValue, String refType, String fieldName,
        String fieldTypeSign, int line, String className, String methodSignature);

    void _writeLocalVar(Object varValue, String varName, String varType, int line,
        int bcLocalVarIdx,
        int varScopeStartLine, int varScopeEndLine, String className, String methodSignature);

    void _readLocalVar(Object varValue, String varName, String varType, int line,
        int bcLocalVarIdx,
        int varScopeStartLine, int varScopeEndLine, String className, String methodSignature);

    void _iincLocalVar(Object varValue, Object varValueAfter, String varName, String varType,
        int line,
        int bcLocalVarIdx, int varScopeStartLine, int varScopeEndLine, String className,
        String methodSignature);

    void _readArrayElementVar(Object arrayRef, int index, Object eleValue,
        String elementType, int line, String className, String methodSignature);

    void _writeArrayElementVar(Object arrayRef, int index, Object eleValue,
        String elementType, int line, String className, String methodSignature);

    void _afterInvoke(Object returnedValue, Object invokeObj, String invokeMethodSig, int line,
        String residingClassName,
        String residingMethodSignature, boolean needRevisiting);

    void _hitMethodEnd(int line, String className, String methodSignature);

    /**
     * @return: isLocking
     */
    boolean lock();

    void unLock();

    void setThreadName(String threadName);

}
