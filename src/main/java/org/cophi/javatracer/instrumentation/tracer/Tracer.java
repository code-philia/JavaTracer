package org.cophi.javatracer.instrumentation.tracer;

import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.cophi.javatracer.log.Log;
import org.cophi.javatracer.utils.NamingUtils;

public interface Tracer {

    void _afterInvoke(Object returnedValue, Object invokeObj, String invokeMethodSig, int line,
        String residingClassName,
        String residingMethodSignature, boolean needRevisiting);

    void _hitExceptionTarget(int line, String className, String methodSignature);

    void _hitInvoke(Object invokeObj, String invokeTypeSign, String methodName,
        Object[] params,
        String paramTypeSignsCode, String returnTypeSign, int line, String className,
        String methodSignature);

    void _hitInvokeStatic(String invokeTypeSign, String methodName, Object[] params,
        String paramTypeSignsCode,
        String returnTypeSign, int line, String className, String methodSignature);

    void _hitLine(int line, String className, String methodSignature, int numOfReadVars,
        int numOfWrittenVars, String bytecode);

    void _hitMethodEnd(int line, String className, String methodSignature);

    void _hitReturn(Object returnObj, String returnGeneralType, int line, String className,
        String methodSignature);

    void _hitVoidReturn(int line, String className, String methodSignature);

    void _iincLocalVar(Object varValue, Object varValueAfter, String varName, String varType,
        int line,
        int bcLocalVarIdx, int varScopeStartLine, int varScopeEndLine, String className,
        String methodSignature);

    void _readArrayElementVar(Object arrayRef, int index, Object eleValue,
        String elementType, int line, String className, String methodSignature);

    void _readField(Object refValue, Object fieldValue, String fieldName, String fieldType,
        int line, String className, String methodSignature);

    void _readLocalVar(Object varValue, String varName, String varType, int line,
        int bcLocalVarIdx,
        int varScopeStartLine, int varScopeEndLine, String className, String methodSignature);

    void _readStaticField(Object fieldValue, String refType, String fieldName,
        String fieldTypeSign, int line, String className, String methodSignature);

    void _start();

    void _writeArrayElementVar(Object arrayRef, int index, Object eleValue,
        String elementType, int line, String className, String methodSignature);

    void _writeField(Object refValue, Object fieldValue, String fieldName,
        String fieldTypeSign, int line, String className, String methodSignature);

    void _writeLocalVar(Object varValue, String varName, String varType, int line,
        int bcLocalVarIdx,
        int varScopeStartLine, int varScopeEndLine, String className, String methodSignature);

    void _writeStaticField(Object fieldValue, String refType, String fieldName,
        String fieldType, int line, String className, String methodSignature);

    long getThreadId();

    String getThreadName();

    enum Methods implements InstrumentMethods {
        AFTER_INVOKE("_afterInvoke", Object.class, Object.class, String.class, int.class,
            String.class, String.class, boolean.class),
        HIT_EXCEPTION_TARGET("_hitExceptionTarget", int.class, String.class, String.class),
        HIT_INVOKE("_hitInvoke", Object.class, String.class, String.class, Object[].class,
            String.class,
            String.class, int.class, String.class, String.class),
        HIT_INVOKE_STATIC("_hitInvokeStatic", String.class, String.class, Object[].class,
            String.class, String.class, int.class, String.class, String.class),
        HIT_LINE("_hitLine", int.class, String.class, String.class, int.class, int.class,
            String.class),
        HIT_METHOD_END("_hitMethodEnd", int.class, String.class, String.class),
        HIT_RETURN("_hitReturn", Object.class, String.class, int.class, String.class,
            String.class),
        HIT_VOID_RETURN("_hitVoidReturn", int.class, String.class, String.class),
        IINC_LOCAL_VAR("_iincLocalVar", Object.class, Object.class, String.class, String.class,
            int.class, int.class, int.class, int.class, String.class, String.class),
        READ_ARRAY_ELEMENT_VAR("_readArrayElementVar", Object.class, int.class, Object.class,
            String.class, int.class, String.class, String.class),
        READ_FIELD("_readField", Object.class, Object.class, String.class, String.class, int.class,
            String.class, String.class),
        READ_LOCAL_VAR("_readLocalVar", Object.class, String.class, String.class, int.class,
            int.class, int.class, int.class, String.class, String.class),
        READ_STATIC_FIELD("_readStaticField", Object.class, String.class, String.class,
            String.class, int.class, String.class, String.class),
        START("_start"),
        WRITE_ARRAY_ELEMENT_VAR("_writeArrayElementVar", Object.class, int.class, Object.class,
            String.class, int.class, String.class, String.class),
        WRITE_FIELD("_writeField", Object.class, Object.class, String.class, String.class,
            int.class, String.class, String.class),
        WRITE_LOCAL_VAR("_writeLocalVar", Object.class, String.class, String.class, int.class,
            int.class, int.class, int.class, String.class, String.class),
        WRITE_STATIC_FIELD("_writeStaticField", Object.class, String.class, String.class,
            String.class, int.class, String.class, String.class);

        private final String methodName;
        private final int argumentNumber;
        private final String descriptor;
        private final String declareClassBinaryName;


        Methods(final String methodName, Class<?>... argumentTypes) {
            try {
                final Class<?> clazz = Tracer.class;
                Method method = clazz.getDeclaredMethod(methodName, argumentTypes);
                this.declareClassBinaryName = NamingUtils.canonicalToClassBinaryName(
                    method.getDeclaringClass().getName());
                this.descriptor = MethodType.methodType(method.getReturnType(),
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
}
