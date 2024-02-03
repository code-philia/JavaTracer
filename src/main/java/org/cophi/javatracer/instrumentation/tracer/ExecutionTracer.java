package org.cophi.javatracer.instrumentation.tracer;

import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;
import org.cophi.javatracer.codeanalysis.bytecode.ByteCodeParser;
import org.cophi.javatracer.codeanalysis.bytecode.MethodFinderBySignature;
import org.cophi.javatracer.configs.JavaTracerConfig;
import org.cophi.javatracer.configs.ProjectConfig;
import org.cophi.javatracer.instrumentation.agents.DefaultAgent;
import org.cophi.javatracer.instrumentation.agents.LoadedClassRecord;
import org.cophi.javatracer.instrumentation.runtime.HeuristicIgnoringFieldRule;
import org.cophi.javatracer.instrumentation.runtime.InvokingDetail;
import org.cophi.javatracer.instrumentation.runtime.MethodCallStack;
import org.cophi.javatracer.instrumentation.tracer.factories.ExecutionTracerFactory;
import org.cophi.javatracer.log.Log;
import org.cophi.javatracer.model.location.BreakPoint;
import org.cophi.javatracer.model.trace.Trace;
import org.cophi.javatracer.model.trace.TraceNode;
import org.cophi.javatracer.model.trace.VariableDefinitions;
import org.cophi.javatracer.model.variables.ArrayElementVar;
import org.cophi.javatracer.model.variables.ArrayValue;
import org.cophi.javatracer.model.variables.FieldVar;
import org.cophi.javatracer.model.variables.LocalVar;
import org.cophi.javatracer.model.variables.PrimitiveValue;
import org.cophi.javatracer.model.variables.ReferenceValue;
import org.cophi.javatracer.model.variables.StringValue;
import org.cophi.javatracer.model.variables.VarValue;
import org.cophi.javatracer.model.variables.Variable;
import org.cophi.javatracer.model.variables.VirtualVar;
import org.cophi.javatracer.utils.JavaTracerUtils;
import org.cophi.javatracer.utils.NamingUtils;
import org.cophi.javatracer.utils.PrimitiveUtils;
import org.cophi.javatracer.utils.SignatureUtils;
import org.cophi.javatracer.utils.TraceUtils;

public class ExecutionTracer implements Tracer {

    protected static Map<String, Integer> adjustVarMap = new HashMap<>();
    private static Set<Class<?>> stringValueBlackList = new HashSet<>();
    protected final long threadId;
    protected final String threadName;
    protected final ManagerDelegate managerDelegate;
    protected final ProjectConfig projectConfig;
    private final MethodCallStack methodCallStack = new MethodCallStack();
    protected Trace trace;

    public ExecutionTracer(final long threadId, final String threadName,
        ProjectConfig projectConfig) {
        this.threadId = threadId;
        this.threadName = threadName;
        this.managerDelegate = new ManagerDelegate(threadId);
        this.projectConfig = projectConfig;
        this.trace = new Trace(this.projectConfig);
    }

    public synchronized static Tracer _getTracer(boolean isInternalClass, String className,
        String methodSig, int methodStartLine, int methodEndLine, String paramNamesCode,
        String paramTypeSignsCode, Object[] params) {
        Log.flow("Get tracer: " + methodSig, ExecutionTracer.class);
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
            tracer.enterMethod(className, methodSig, methodStartLine, methodEndLine,
                paramTypeSignsCode, paramNamesCode, params);
            manager.unlock(threadId);

            return tracer;

        } catch (Throwable throwable) {
            Log.error(throwable.getLocalizedMessage(), ExecutionTracer.class);
            throw throwable;
        }
    }

    @Override
    public void _afterInvoke(Object returnedValue, Object invokeObj, String invokeMethodSig,
        int line, String residingClassName, String residingMethodSignature,
        boolean needRevisiting) {
        Log.flow("After invoke: " + invokeMethodSig + " line: " + line, ExecutionTracer.class);
        this.managerDelegate.lock();
        try {
            boolean exclusive = LoadedClassRecord.getInstance().isExclusive(residingClassName);
            if (!exclusive) {
                hitLine(line, residingClassName, residingMethodSignature);
                TraceNode latestNode = trace.getLatestNode();
                if (latestNode != null) {
                    latestNode.setInvokingDetail(null);
                    TraceNode invokingMatchNode = this.findInvokingMatchNode(latestNode,
                        invokeMethodSig);
                    if (invokingMatchNode == null) {
                        latestNode.setInvokingMatchNode(latestNode);
                    } else {
                        latestNode.setInvokingMatchNode(invokingMatchNode);
                        invokingMatchNode.setInvokingMatchNode(latestNode);
                        latestNode.setBytecode(invokingMatchNode.getBytecode());
                    }

                    // TraceNode invokingMatchNode = findInvokingMatchNode(latestNode,
                    // invokeMethodSig);
                    // if(invokingMatchNode!=null){
                    // invokingMatchNode.setInvokingMatchNode(latestNode);
                    // latestNode.setInvokingMatchNode(invokingMatchNode);
                    // }
                }

                if (returnedValue != null && invokeMethodSig.contains("clone()")) {
                    String returnTypeSign = returnedValue.getClass().getName();
                    String type = SignatureUtils.signatureToName(returnTypeSign);
                    Variable var = new LocalVar("$tmp", type, null, -1);
                    VarValue value = new ReferenceValue(false, false, var);

                    String varID = TraceUtils.getObjectVarId(returnedValue, returnTypeSign);
                    value.setVarID(varID);

                    appendVarValue(returnedValue, var, value, 2);

                    if (!value.getChildren().isEmpty()) {
                        VarValue parent = value.getChildren().get(0);
                        addRWriteValue(latestNode, parent.getChildren(), true);
                    }
                }
            }
        } catch (Throwable t) {
            handleException(t);
        }
        this.managerDelegate.unlock();

    }

    @Override
    public void _hitExceptionTarget(int line, String className, String methodSignature) {
        this.managerDelegate.lock();
        Log.flow("Hit exception target: " + methodSignature + " line: " + line,
            ExecutionTracer.class);
        try {
            hitLine(line, className, methodSignature);
            TraceNode latestNode = trace.getLatestNode();
            if (latestNode == null) {
                return;
            }
            latestNode.setException(true);
            boolean invocationLayerChanged = this.methodCallStack.popForException(methodSignature,
                this.projectConfig);

            if (invocationLayerChanged) {
                TraceNode caller = null;
                if (!this.methodCallStack.isEmpty()) {
                    caller = this.methodCallStack.peek();
                }

                TraceNode olderCaller = latestNode.getInvocationParent();
                if (olderCaller != null) {
                    olderCaller.getInvocationChildren().remove(latestNode);
                    latestNode.setInvocationParent(caller);
                }
            }
        } catch (Throwable t) {
            handleException(t);
        }
        this.managerDelegate.unlock();
    }

    @Override
    public void _hitInvoke(Object invokeObj, String invokeTypeSign, String methodSig,
        Object[] params, String paramTypeSignsCode, String returnTypeSign, int line,
        String residingClassName, String residingMethodSignature) {
        Log.flow("Hit invoke: " + methodSig, ExecutionTracer.class);
        this.managerDelegate.lock();
        try {
            hitLine(line, residingClassName, residingMethodSignature);
            TraceNode latestNode = trace.getLatestNode();
            if (latestNode != null) {
                latestNode.addInvokingMethod(methodSig);
                initInvokingDetail(invokeObj, invokeTypeSign, methodSig, params, paramTypeSignsCode,
                    residingClassName,
                    latestNode);

                if (methodSig.contains("clone()")) {

                    String type = SignatureUtils.signatureToName(invokeTypeSign);
                    Variable var = new LocalVar("$tmp", type, null, -1);
                    VarValue value = new ReferenceValue(false, false, var);

                    String varID = TraceUtils.getObjectVarId(invokeObj, invokeTypeSign);
                    value.setVarID(varID);

                    appendVarValue(invokeObj, var, value, 2);

                    if (!value.getChildren().isEmpty()) {
                        VarValue parent = value.getChildren().get(0);
                        addRWriteValue(latestNode, parent.getChildren(), false);
                    }
                }
            }
        } catch (Throwable t) {
            handleException(t);
        }

        this.managerDelegate.unlock();
    }

    @Override
    public void _hitInvokeStatic(String invokeTypeSign, String methodSig, Object[] params,
        String paramTypeSignsCode, String returnTypeSign, int line, String className,
        String residingMethodSignature) {
        Log.flow("Hit invoke static: " + methodSig + " line: " + line, ExecutionTracer.class);
        this.managerDelegate.lock();
        try {
            hitLine(line, className, residingMethodSignature);

            if (methodSig.equals(
                "java.lang.System#arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V")) {
                Object sourceArray = params[0];
                int sourcePosition = (Integer) params[1];
                Object targetArray = params[2];
                int targetPosition = (Integer) params[3];
                int length = (Integer) params[4];

                buildReadRelationForArrayCopy(sourceArray, sourcePosition, length, line);
                buildWriteRelationForArrayCopy(targetArray, targetPosition, sourceArray,
                    sourcePosition, length, line);
            }

            TraceNode latestNode = trace.getLatestNode();
            if (latestNode != null) {
                latestNode.addInvokingMethod(methodSig);
                initInvokingDetail(null, invokeTypeSign, methodSig, params, paramTypeSignsCode,
                    className, latestNode);
            }
        } catch (Throwable t) {
            handleException(t);
        }
        this.managerDelegate.unlock();
    }

    @Override
    public void _hitLine(int line, String className, String methodSignature, int numOfReadVars,
        int numOfWrittenVars, String bytecode) {
        Log.flow(
            "Hit line: " + line + " className: " + className + " methodSign: " + methodSignature,
            ExecutionTracer.class);
        boolean isLocked = this.managerDelegate.isLocked();
        this.managerDelegate.lock();
        try {
            boolean exclusive = LoadedClassRecord.getInstance().isExclusive(className);
            if (exclusive) {
                this.managerDelegate.unlock(isLocked);
                return;
            }
            TraceNode latestNode = trace.getLatestNode();
            if (latestNode != null && latestNode.getBreakPoint().getClassCanonicalName()
                .equals(className)
                && latestNode.getBreakPoint().getLineNumber() == line) {

                // Add the bytecode if it is missing
                if (latestNode.getBytecode() == null) {
                    latestNode.setBytecode(bytecode);
                }

                this.managerDelegate.unlock(isLocked);
                return;
            }

            int order = trace.size() + 1;

            if (order > JavaTracerConfig.getInstance().getStepLimit()) {
                shutdown();
                DefaultAgent._exitProgram("fail;Trace is over long!");
            }
//			if (order > tolerantExpectedSteps) {
//				shutdown();
//				Agent._exitProgram("fail;Trace size exceeds expected_steps!");
//			}

            BreakPoint bkp = new BreakPoint(className, methodSignature, line);
            long timestamp = System.currentTimeMillis();
            TraceNode currentNode = new TraceNode(bkp, null, order, trace, numOfReadVars,
                numOfWrittenVars, timestamp, bytecode);

            trace.addTraceNode(currentNode);
//            AgentLogger.printProgress(order);
            if (!methodCallStack.isEmpty()) {
                TraceNode caller = methodCallStack.peek();
                caller.addInvocationChild(currentNode);
                currentNode.setInvocationParent(caller);
            }
        } catch (Throwable t) {
            handleException(t);
        }
        this.managerDelegate.unlock();
    }

    @Override
    public void _hitMethodEnd(int line, String className, String methodSignature) {
        Log.flow("Hit method end: " + methodSignature, ExecutionTracer.class);
        this.managerDelegate.lock();
        try {
            this.exitMethod(line, className, methodSignature);
        } catch (Throwable e) {
            this.handleException(e);
        }
        this.managerDelegate.unlock();
    }

    @Override
    public void _hitReturn(Object returnObj, String returnGeneralTypeSign, int line,
        String className,
        String methodSignature) {
        Log.flow("Hit return: " + methodSignature + " line: " + line, ExecutionTracer.class);
        this.managerDelegate.lock();
        try {
            hitLine(line, className, methodSignature);
            String returnGeneralType = SignatureUtils.signatureToName(returnGeneralTypeSign);
            Variable returnVar = new VirtualVar(methodSignature, returnGeneralType);

            String varID = VirtualVar.VIRTUAL_PREFIX + methodSignature;
            returnVar.setVarID(varID);

            String aliasID = TraceUtils.getObjectVarId(returnObj, returnGeneralTypeSign);
            returnVar.setAliasVarID(aliasID);
            VarValue returnVal = appendVarValue(returnObj, returnVar, null);
            if (returnVal != null) {
                TraceNode latestNode = trace.getLatestNode();
                if (latestNode != null) {
                    String definingOrder = trace.findDefiningNodeOrder(Variable.WRITTEN, latestNode,
                        returnVar,
                        VariableDefinitions.USE_LAST);
                    returnVar.setVarID(returnVar.getVarID() + ":" + definingOrder);
                    returnVar.setAliasVarID(returnVar.getAliasVarID() + ":" + definingOrder);

                    latestNode.addReturnVariable(returnVal);
                }
            }
        } catch (Throwable t) {
            handleException(t);
        }
        this.managerDelegate.unlock();
    }

    @Override
    public void _hitVoidReturn(int line, String className, String methodSignature) {
        try {
            Log.flow("Hit void return: " + methodSignature, ExecutionTracer.class);
            hitLine(line, className, methodSignature);
        } catch (Throwable t) {
            handleException(t);
        }
    }

    @Override
    public void _iincLocalVar(Object varValue, Object varValueAfter, String varName, String varType,
        int line, int bcLocalVarIdx, int varScopeStartLine, int varScopeEndLine, String className,
        String methodSignature) {
        Log.flow("Iinc local var: " + varName + " line: " + line, ExecutionTracer.class);
        this.managerDelegate.lock();
        try {
            // boolean exclusive = FilterChecker.isExclusive(className, methodSignature);
            // if (exclusive) {
            // locker.unLock();
            // return;
            // }
            hitLine(line, className, methodSignature);

            TraceNode latestNode = trace.getLatestNode();
            Variable var = new LocalVar(varName, varType, className, line);
            String varID = Variable.concatenateLocalVarID(className, varName, varScopeStartLine,
                varScopeEndLine,
                latestNode.getInvocationLevel());
            // String varID = TraceUtils.getLocalVarId(className, varScopeStartLine,
            // varScopeEndLine, varName, varType, varValue);
            var.setVarID(varID);

            Variable varBefore = var.clone();
            VarValue value = appendVarValue(varValue, varBefore, null);
            addRWriteValue(trace.getLatestNode(), value, false); // add read var

            Variable varAfter = var.clone();
            VarValue writtenValue = appendVarValue(varValueAfter, varAfter, null);
            addRWriteValue(trace.getLatestNode(), writtenValue, true); // add written var
        } catch (Throwable t) {
            handleException(t);
        }
        this.managerDelegate.unlock();
    }

    @Override
    public void _readArrayElementVar(Object arrayRef, int index, Object eleValue,
        String elementType, int line, String className, String methodSignature) {
        Log.flow("Read array element var: " + methodSignature + " line: " + line,
            ExecutionTracer.class);
        this.managerDelegate.lock();
        try {
            boolean exclusive = LoadedClassRecord.getInstance().isExclusive(className);
            if (exclusive) {
                TraceNode latestNode = trace.getLatestNode();
                boolean relevant = false;
                if (latestNode != null && latestNode.getInvokingDetail() != null) {
                    InvokingDetail invokingDetail = latestNode.getInvokingDetail();
                    relevant = invokingDetail.updateRelevantVar(arrayRef, eleValue, elementType);
                }
                if (!relevant) {
                    this.managerDelegate.unlock();
                    return;
                }
            }
            hitLine(line, className, methodSignature);
            VarValue value = addArrayElementVarValue(arrayRef, index, eleValue, elementType, line);

            Variable parentVariable = new FieldVar(false, "unknown", arrayRef.getClass().getName(),
                "unknown");
            String parentVarId = TraceUtils.getObjectVarId(arrayRef, arrayRef.getClass().getName());
            parentVariable.setVarID(parentVarId);
            ReferenceValue parentValue = new ReferenceValue(false, false, parentVariable);
            value.addParent(parentValue);

            addRWriteValue(trace.getLatestNode(), value, false);
            addHeuristicVarChildren(trace.getLatestNode(), value, false);

        } catch (Throwable t) {
            handleException(t);
        }
        this.managerDelegate.unlock();
    }

    @Override
    public void _readField(Object refValue, Object fieldValue, String fieldName, String fieldType,
        int line, String className, String methodSignature) {
        Log.flow("Read field: " + methodSignature + " filed name: " + fieldName + " line: " + line,
            ExecutionTracer.class);
        this.managerDelegate.lock();
        try {
            boolean exclusive = LoadedClassRecord.getInstance().isExclusive(className);
            if (exclusive) {
                TraceNode latestNode = trace.getLatestNode();
                boolean relevant = false;
                if (latestNode != null && latestNode.getInvokingDetail() != null) {
                    InvokingDetail invokingDetail = latestNode.getInvokingDetail();
                    relevant = invokingDetail.updateRelevantVar(refValue, fieldValue, fieldType);
                }
                if (!relevant) {
                    this.managerDelegate.unlock();
                    return;
                }
            }
            hitLine(line, className, methodSignature);
            String parentVarId = TraceUtils.getObjectVarId(refValue, refValue.getClass().getName());
            String fieldVarId = TraceUtils.getFieldVarId(parentVarId, fieldName, fieldType,
                fieldValue);
            // invokeTrack.updateRelevant(parentVarId, fieldVarId);
            // if (exclusive) {
            // return;
            // }
            Variable var = new FieldVar(false, fieldName, fieldType, refValue.getClass().getName());
            var.setVarID(fieldVarId);
            String aliasID = TraceUtils.getObjectVarId(fieldValue, fieldType);
            var.setAliasVarID(aliasID);

            VarValue value = appendVarValue(fieldValue, var, null);

            Variable parentVariable = new FieldVar(false, "unknown", refValue.getClass().getName(),
                "unknown");
            parentVariable.setVarID(parentVarId);
            ReferenceValue parentValue = new ReferenceValue(false, false, parentVariable);
            value.addParent(parentValue);

            addRWriteValue(trace.getLatestNode(), value, false);
            addHeuristicVarChildren(trace.getLatestNode(), value, false);
        } catch (Throwable t) {
            handleException(t);
        }
        this.managerDelegate.unlock();
    }

    @Override
    public void _readLocalVar(Object varValue, String varName, String varType, int line,
        int bcLocalVarIdx, int varScopeStartLine, int varScopeEndLine, String className,
        String methodSignature) {
        Log.flow("Read local var: " + methodSignature + " line: " + line + " varName: " + varName,
            ExecutionTracer.class);
        this.managerDelegate.lock();
        try {
            // boolean exclusive = FilterChecker.isExclusive(className, methodSignature);
            // if (exclusive) {
            // locker.unLock();
            // return;
            // }
            hitLine(line, className, methodSignature);
            TraceNode latestNode = trace.getLatestNode();
            Variable var = new LocalVar(varName, varType, className, line);

            String varID = Variable.concatenateLocalVarID(className, varName, varScopeStartLine,
                varScopeEndLine,
                latestNode.getInvocationLevel());
            // String varID = TraceUtils.getLocalVarId(className, varScopeStartLine,
            // varScopeEndLine, varName, varType, varValue);
            var.setVarID(varID);
            String aliasVarID = TraceUtils.getObjectVarId(varValue, varType);
            var.setAliasVarID(aliasVarID);

            VarValue value = appendVarValue(varValue, var, null);
            addRWriteValue(trace.getLatestNode(), value, false);
            // System.currentTimeMillis();
            addHeuristicVarChildren(trace.getLatestNode(), value, false);

            // TraceNode currentNode = trace.getLatestNode();
            // String order = trace.findDefiningNodeOrder(Variable.READ, currentNode,
            // var.getVarID(), var.getAliasVarID());
            // if(value instanceof ReferenceValue && order.equals("0")){
            // if(isParameter(varScopeStartLine, varScopeEndLine, className)){
            //
            // TraceNode invocationParent = currentNode.getInvocationParent();
            // if(invocationParent!=null){
            // String simpleVarID = Variable.truncateSimpleID(varID);
            // varID = simpleVarID + ":" + invocationParent.getOrder();
            // value.setVarID(varID);
            //
            // if(!invocationParent.getWrittenVariables().contains(value)){
            // invocationParent.addWrittenVariable(value);
            // }
            // if(!currentNode.getReadVariables().contains(value)){
            // currentNode.addReadVariable(value);
            // }
            //
            // StepVariableRelationEntry entry = new StepVariableRelationEntry(varID);
            // entry.addProducer(invocationParent);
            // entry.addConsumer(currentNode);
            // trace.getStepVariableTable().put(varID, entry);
            //
            //
            // }
            // }
            // }
            // else{
            // addRWriteValue(value, false);
            // }

        } catch (Throwable t) {
            handleException(t);
        }
        this.managerDelegate.unlock();
    }

    @Override
    public void _readStaticField(Object fieldValue, String refType, String fieldName,
        String fieldType, int line, String className, String methodSignature) {
        Log.flow(
            "Read static field: " + methodSignature + " line: " + line + " fieldName: " + fieldName,
            ExecutionTracer.class);
        this.managerDelegate.lock();
        try {
            // boolean exclusive = FilterChecker.isExclusive(className, methodSignature);
            // if (exclusive) {
            // locker.unLock();
            // return;
            // }
            hitLine(line, className, methodSignature);
            Variable var = new FieldVar(true, fieldName, fieldType, refType);
            var.setVarID(Variable.concatenateFieldVarID(refType, fieldName));

            String aliasID = TraceUtils.getObjectVarId(fieldValue, fieldType);
            var.setAliasVarID(aliasID);

            VarValue value = appendVarValue(fieldValue, var, null);
            addRWriteValue(trace.getLatestNode(), value, false);
            addHeuristicVarChildren(trace.getLatestNode(), value, false);

        } catch (Throwable t) {
            handleException(t);
        }
        this.managerDelegate.unlock();
    }

    @Override
    public void _start() {
        TracerManager.getInstance().setState(TracerState.TEST_STARTED);
    }

    @Override
    public void _writeArrayElementVar(Object arrayRef, int index, Object eleValue,
        String elementType, int line, String className, String methodSignature) {
        Log.flow("Write array element var: " + methodSignature + " line: " + line,
            ExecutionTracer.class);
        this.managerDelegate.lock();
        try {
            boolean exclusive = LoadedClassRecord.getInstance().isExclusive(className);
            if (exclusive) {
                TraceNode latestNode = trace.getLatestNode();
                boolean relevant = false;
                if (latestNode != null && latestNode.getInvokingDetail() != null) {
                    InvokingDetail invokingDetail = latestNode.getInvokingDetail();
                    relevant = invokingDetail.updateRelevantVar(arrayRef, eleValue, elementType);
                }
                if (!relevant) {
                    this.managerDelegate.unlock();
                    return;
                }
            }
            hitLine(line, className, methodSignature);
            VarValue value = addArrayElementVarValue(arrayRef, index, eleValue, elementType, line);

            Variable parentVariable = new FieldVar(false, "unknown", arrayRef.getClass().getName(),
                "unknown");
            String parentVarId = TraceUtils.getObjectVarId(arrayRef, arrayRef.getClass().getName());
            parentVariable.setVarID(parentVarId);
            ReferenceValue parentValue = new ReferenceValue(false, false, parentVariable);
            value.addParent(parentValue);

            addRWriteValue(trace.getLatestNode(), value, true);
        } catch (Throwable t) {
            handleException(t);
        }
        this.managerDelegate.unlock();
    }

    @Override
    public void _writeField(Object refValue, Object fieldValue, String fieldName,
        String fieldType, int line, String className, String methodSignature) {
        Log.flow("Write field: " + methodSignature + " line: " + line + " fieldName: " + fieldName,
            ExecutionTracer.class);
        this.managerDelegate.lock();
        try {
            hitLine(line, className, methodSignature);
            boolean exclusive = LoadedClassRecord.getInstance().isExclusive(className);
            TraceNode latestNode = trace.getLatestNode();
            if (exclusive) {
                if (latestNode != null && latestNode.getInvokingDetail() != null) {
                    InvokingDetail invokingDetail = latestNode.getInvokingDetail();
                    boolean relevant = invokingDetail.updateRelevantVar(refValue, fieldValue,
                        fieldType);
                    if (!relevant) {
                        this.managerDelegate.unlock();
                        return;
                    }
                } else {
                    this.managerDelegate.unlock();
                    return;
                }
            }
            String parentVarId = TraceUtils.getObjectVarId(refValue, refValue.getClass().getName());
            String fieldVarId = TraceUtils.getFieldVarId(parentVarId, fieldName, fieldType,
                fieldValue);
            Variable var = new FieldVar(false, fieldName, fieldType, refValue.getClass().getName());
            var.setVarID(fieldVarId);
            if (!PrimitiveUtils.isPrimitive(fieldType)) {
                String aliasID = TraceUtils.getObjectVarId(fieldValue, fieldType);
                var.setAliasVarID(aliasID);
            }

            VarValue value = appendVarValue(fieldValue, var, null);

            Variable parentVariable = new FieldVar(false, "unknown", refValue.getClass().getName(),
                "unknown");
            parentVariable.setVarID(parentVarId);
            ReferenceValue parentValue = new ReferenceValue(false, false, parentVariable);
            value.addParent(parentValue);

            addRWriteValue(latestNode, value, true);
        } catch (Throwable t) {
            handleException(t);
        }
        this.managerDelegate.unlock();
    }

    @Override
    public void _writeLocalVar(Object varValue, String varName, String varType, int line,
        int bcLocalVarIdx, int varScopeStartLine, int varScopeEndLine, String className,
        String methodSignature) {
        Log.flow("Write local var: " + methodSignature + " line: " + line + " varName: " + varName
            , ExecutionTracer.class);
        this.managerDelegate.lock();
        try {
            // boolean exclusive = FilterChecker.isExclusive(className, methodSignature);
            // if (exclusive) {
            // locker.unLock();
            // return;
            // }
            hitLine(line, className, methodSignature);
            Variable var = new LocalVar(varName, varType, className, line);

            TraceNode latestNode = trace.getLatestNode();
            String varID = Variable.concatenateLocalVarID(className, varName, varScopeStartLine,
                varScopeEndLine,
                latestNode.getInvocationLevel());
            var.setVarID(varID);
            if (!PrimitiveUtils.isPrimitive(varType)) {
                String aliasID = TraceUtils.getObjectVarId(varValue, varType);
                var.setAliasVarID(aliasID);
            }

            VarValue value = appendVarValue(varValue, var, null);
            addRWriteValue(trace.getLatestNode(), value, true);
        } catch (Throwable t) {
            handleException(t);
        }
        this.managerDelegate.unlock();
    }

    @Override
    public void _writeStaticField(Object fieldValue, String refType, String fieldName,
        String fieldType, int line, String className, String methodSignature) {
        Log.flow("Write static field: " + methodSignature + " fieldName: " + fieldName,
            ExecutionTracer.class);
        this.managerDelegate.lock();
        try {
            // boolean exclusive = FilterChecker.isExclusive(className, methodSignature);
            // if (exclusive) {
            // return;
            // }
            hitLine(line, className, methodSignature);
            Variable var = new FieldVar(false, fieldName, fieldType, refType);
            var.setVarID(Variable.concatenateFieldVarID(refType, fieldName));
            if (!PrimitiveUtils.isPrimitive(fieldType)) {
                String aliasVarID = TraceUtils.getObjectVarId(fieldValue, fieldType);
                var.setAliasVarID(aliasVarID);
            }
            VarValue value = appendVarValue(fieldValue, var, null);
            addRWriteValue(trace.getLatestNode(), value, true);
        } catch (Throwable t) {
            handleException(t);
        }
        this.managerDelegate.unlock();
    }

    @Override
    public long getThreadId() {
        return this.threadId;
    }

    @Override
    public String getThreadName() {
        return this.threadName;
    }

    public void buildReadRelationForArrayCopy(Object array, int startPosition, int length,
        int line) {
        Variable sourceParentVariable = new FieldVar(false, "unknown", array.getClass().getName(),
            "unknown");
        String sourceParentVarId = TraceUtils.getObjectVarId(array, array.getClass().getName());
        sourceParentVariable.setVarID(sourceParentVarId);
        ReferenceValue sourceParentValue = new ReferenceValue(false, false, sourceParentVariable);
        for (int i = 0; i < length; i++) {
            int k = startPosition + i;
            Object elementValue = Array.get(array, k);
            String elementType = "Object";
            if (elementValue != null) {
                elementType = elementValue.getClass().getName();
            }
            VarValue value = addArrayElementVarValue(array, k, elementValue, elementType, line);
            value.addParent(sourceParentValue);
            addRWriteValue(trace.getLatestNode(), value, false);
        }
    }

    public void buildWriteRelationForArrayCopy(Object targetArray, int startPosition,
        Object sourceArray, int srcStartPos,
        int length, int line) {
        Variable targetParentVariable = new FieldVar(false, "unknown",
            targetArray.getClass().getName(), "unknown");
        String targetParentVarId = TraceUtils.getObjectVarId(targetArray,
            targetArray.getClass().getName());
        targetParentVariable.setVarID(targetParentVarId);
        ReferenceValue targetParentValue = new ReferenceValue(false, false, targetParentVariable);
        for (int i = 0; i < length; i++) {
            int k = srcStartPos + i;
            Object elementValue = Array.get(sourceArray, k);
            String elementType = "Object";
            if (elementValue != null) {
                elementType = elementValue.getClass().getName();
            }
            int index = startPosition + i;
            VarValue value = addArrayElementVarValue(targetArray, index, elementValue, elementType,
                line);
            value.addParent(targetParentValue);
            addRWriteValue(trace.getLatestNode(), value, true);
        }
    }

    public void enterMethod(final String className, final String methodSignature,
        final int methodStartLine, int methodEndLine, final String paramTypeSignatureCode,
        final String paramNamesCode, final Object[] params) {

        // Lock this thread so that other tracer will not interrupt the recording.
        this.managerDelegate.lock();

        TraceNode caller = trace.getLatestNode();
        if (caller != null && caller.getMethodSign().contains("<clinit>")) {
            caller = caller.getInvocationParent();
        }

        if (caller != null) {
            int varScopeStart = methodStartLine;
            int varScopeEnd = methodEndLine;

            String[] parameterTypes = JavaTracerUtils.decodeArgumentTypes(paramTypeSignatureCode);
            String[] parameterNames = JavaTracerUtils.decodeArgumentNames(paramNamesCode);
            if (parameterNames.length != 0) {
                int adjust = adjustVariableStartScope(methodSignature, className);
                varScopeStart = (adjust > 0) ? adjust : varScopeStart;
            }

            for (int i = 0; i < parameterTypes.length; i++) {
                String pType = parameterTypes[i];
                String parameterType = SignatureUtils.signatureToName(pType);
                String varName = parameterNames[i];

                Variable var = new LocalVar(varName, parameterType, className, methodStartLine);

                String varID = Variable.concatenateLocalVarID(className, varName, varScopeStart,
                    varScopeEnd,
                    caller.getInvocationLevel() + 1);
                var.setVarID(varID);
                if (!PrimitiveUtils.isPrimitive(pType)) {
                    String aliasID = TraceUtils.getObjectVarId(params[i], pType);
                    var.setAliasVarID(aliasID);
                }

                VarValue value = appendVarValue(params[i], var, null);
//				if(value instanceof PrimitiveValue && !(value instanceof StringValue)) {
//					addRWriteValue(caller, value, true);
//				}
                if ((value instanceof PrimitiveValue || value instanceof ArrayValue)
                    && !(value instanceof StringValue)) {
                    addRWriteValue(caller, value, true);
                }
            }
        }

        boolean exclusive = LoadedClassRecord.getInstance().isExclusive(className);
        if (!exclusive) {
            if (caller != null) {
                methodCallStack.push(caller);
            }
            hitLine(methodStartLine, className, methodSignature);
        } else {
            this.managerDelegate.unlock();
            return;
        }
        // Release the lock
        this.managerDelegate.unlock();

    }

    public void exitMethod(int line, String className, String methodSignature) {
        this.managerDelegate.lock();
        boolean exclusive = LoadedClassRecord.getInstance().isExclusive(className);
        if (!exclusive) {
            methodCallStack.safePop();
        }
        this.managerDelegate.unlock();
    }

    public Trace getTrace() {
        return this.trace;
    }

    public void hitLine(final int lineNumber, final String className,
        final String methodSignature) {
        this._hitLine(lineNumber, className, methodSignature, -1, -1, null);
    }

    protected void shutdown() {
        TracerManager.getInstance().setState(TracerState.SHUTDOWN);
    }

    private VarValue addArrayElementVarValue(Object arrayRef, int index, Object eleValue,
        String elementType, int line) {
        String id = new StringBuilder(
            TraceUtils.getObjectVarId(arrayRef, elementType + "[]")).append("[").append(index)
            .append("]").toString();
        String name = id;
        Variable var = new ArrayElementVar(name, elementType, id);
        var.setVarID(id);

        if (!PrimitiveUtils.isPrimitive(elementType)) {
            String aliasID = TraceUtils.getObjectVarId(eleValue, elementType);
            var.setAliasVarID(aliasID);
        }

        VarValue value = appendVarValue(eleValue, var, null);
        return value;
    }

    private void addHeuristicVarChildren(TraceNode latestNode, VarValue value, boolean isWritten) {
        // if (ArrayList.class.getName().equals(value.getRuntimeType())) {
        // for (VarValue child : value.getChildren()) {
        // if ("elementData".equals(child.getVarName())) {
        // addHeuristicVarChildren(latestNode, child, isWritten);
        // return;
        // }
        // }
        // } else if (HashMap.class.getName().equals(value.getRuntimeType())) {
        // for (VarValue child : value.getChildren()) {
        // if ("table".equals(child.getVarName())) {
        // addHeuristicVarChildren(latestNode, child, isWritten);
        // return;
        // }
        // }
        // }
        // else if (Stack.class.getName().equals(value.getRuntimeType())) {
        // /**
        // * TODO for Xuezhi extend stack content here.
        // */
        // for (VarValue child : value.getChildren()) {
        // if ("elementData".equals(child.getVarName())) {
        // addHeuristicVarChildren(latestNode, child, isWritten);
        // return;
        // }
        // }
        // }
        //
        // else if (value instanceof ArrayValue) {
        // Collection<VarValue> nodeReadVars = trace.getLatestNode().getReadVariables();
        // if (value.getChildren().size() > nodeReadVars.size()) {
        // ((ArrayList<?>)nodeReadVars).ensureCapacity(nodeReadVars.size() +
        // value.getChildren().size());
        // }
        // for (VarValue child : value.getChildren()) {
        // if (child.getVariable() instanceof ArrayElementVar) {
        // if (HeuristicIgnoringFieldRule.isHashMapTableType(child.getRuntimeType())) {
        // for (VarValue nodeAttr : child.getChildren()) {
        // addRWriteValue(trace.getLatestNode(), nodeAttr, false);
        // }
        // } else {
        // addRWriteValue(trace.getLatestNode(), child, false);
        // }
        // }
        // }
        // }

    }

    private void addRWriteValue(TraceNode currentNode, VarValue value, boolean isWrittenVar) {
        if (value == null || currentNode == null) {
            return;
        }
        addSingleRWriteValue(currentNode, value, isWrittenVar);
    }

    private void addRWriteValue(TraceNode currentNode, List<VarValue> value, boolean isWrittenVar) {
        ArrayList<VarValue> values;
        if (isWrittenVar) {
            values = (ArrayList<VarValue>) currentNode.getWrittenVariables();
        } else {
            values = (ArrayList<VarValue>) currentNode.getReadVariables();
        }
        if (value.size() > values.size()) {
            values.ensureCapacity(values.size() + value.size());
        }
        for (VarValue child : value) {
            addSingleRWriteValue(currentNode, child, false);
        }
    }

    private void addSingleRWriteValue(TraceNode currentNode, VarValue value, boolean isWrittenVar) {
        if (isWrittenVar) {

            currentNode.addWrittenVariable(value);
            // buildDataRelation(currentNode, value, Variable.WRITTEN);
        } else {
            currentNode.addReadVariable(value);
            // buildDataRelation(currentNode, value, Variable.READ);
        }
    }

    private int adjustVariableStartScope(String fullSign, String className) {
        Integer value = adjustVarMap.get(fullSign);
        if (value != null) {
            return value;
        }
        String shortSign = fullSign.substring(fullSign.indexOf("#") + 1, fullSign.length());
        MethodFinderBySignature finder = new MethodFinderBySignature(shortSign);
        ByteCodeParser.parse(className, finder, this.projectConfig);
        Method method = finder.getMethod();

        if (method == null) {
            System.currentTimeMillis();
        }

        LocalVariableTable table = method.getLocalVariableTable();
        int start = -1;
        if (table != null) {
            for (LocalVariable v : table.getLocalVariableTable()) {
                int line = method.getCode().getLineNumberTable().getSourceLine(v.getStartPC());
                if (start == -1) {
                    start = line;
                } else {
                    if (start > line) {
                        start = line;
                    }
                }
            }
        }
        adjustVarMap.put(fullSign, start);
        Repository.clearCache();
        return start;
    }

    private VarValue appendVarValue(Object value, Variable var, VarValue parent) {
        return appendVarValue(value, var, parent,
            JavaTracerConfig.getInstance().getVariableLayer());
    }

    private VarValue appendVarValue(Object value, Variable var, VarValue parent,
        int retrieveLayer) {
        if (retrieveLayer <= 0) {
            return null;
        }
        retrieveLayer--;

        boolean isRoot = (parent == null);
        VarValue varValue = null;
        if (PrimitiveUtils.isString(var.getType())) {
            varValue = new StringValue(getStringValue(value, null), isRoot, var);
        } else if (PrimitiveUtils.isPrimitive(var.getType())) {
            varValue = new PrimitiveValue(getStringValue(value, null), isRoot, var);
        } else if (var.getType().endsWith("[]")) {
            /* array */
            ArrayValue arrVal = new ArrayValue(value == null, isRoot, var);
            arrVal.setComponentType(
                var.getType().substring(0, var.getType().length() - 2)); // 2 = "[]".length
            varValue = arrVal;
            // varValue.setStringValue(getStringValue(value, arrVal.getComponentType()));
            varValue.setStringValue(getStringValue(value, var.getType()));
            if (value == null) {
                arrVal.setNull(true);
            } else {
                int length = Array.getLength(value);
                arrVal.ensureChildrenSize(length);
                for (int i = 0; i < length; i++) {
                    String parentSimpleID = Variable.truncateSimpleID(var.getVarID());
                    String arrayElementID = Variable.concatenateArrayElementVarID(parentSimpleID,
                        String.valueOf(i));
                    String varName = arrayElementID;
                    ArrayElementVar varElement = new ArrayElementVar(varName,
                        arrVal.getComponentType(), arrayElementID);
                    Object elementValue = Array.get(value, i);
                    if (HeuristicIgnoringFieldRule.isHashMapTableType(arrVal.getComponentType())) {
                        appendVarValue(elementValue, varElement, arrVal, retrieveLayer + 1);
                    } else {
                        appendVarValue(elementValue, varElement, arrVal, retrieveLayer);
                    }
                }
            }
        } else {
            ReferenceValue refVal = new ReferenceValue(value == null, TraceUtils.getUniqueId(value),
                isRoot, var);
            varValue = refVal;
            // varValue.setStringValue(getStringValue(value, var.getType()));
            varValue.setStringValue(getStringValue(value, null));
            if (value != null) {
                Class<?> objClass = value.getClass();
                var.setRtType(objClass.getName());
                boolean needParseFields = HeuristicIgnoringFieldRule.isNeedParsingFields(objClass);
                boolean isCollectionOrHashMap =
                    HeuristicIgnoringFieldRule.isCollectionClass(objClass)
                        || HeuristicIgnoringFieldRule.isHashMapClass(objClass);
                if (needParseFields) {
                    List<Field> validFields = HeuristicIgnoringFieldRule.getValidFields(objClass,
                        value);
                    for (Field field : validFields) {
                        field.setAccessible(true);
                        try {
                            Object fieldValue = field.get(value);
                            Class<?> fieldType = field.getType();
                            String fieldTypeStr = fieldType.getName();
                            if (fieldType.isArray()) {
                                fieldTypeStr = SignatureUtils.signatureToName(fieldTypeStr);
                            }
                            if (fieldType.isEnum()) {
                                if (fieldTypeStr.equals(var.getType())) {
                                    continue;
                                }
                            }
                            if (fieldValue != null) {
                                FieldVar fieldVar = new FieldVar(
                                    Modifier.isStatic(field.getModifiers()), field.getName(),
                                    fieldTypeStr,
                                    field.getDeclaringClass().getName());
                                fieldVar.setVarID(
                                    TraceUtils.getFieldVarId(var.getVarID(), field.getName(),
                                        fieldTypeStr, fieldValue));
                                if (isCollectionOrHashMap
                                    && HeuristicIgnoringFieldRule.isCollectionOrMapElement(
                                    var.getRuntimeType(), field.getName())) {
                                    appendVarValue(fieldValue, fieldVar, refVal, retrieveLayer + 1);
                                } else {
                                    appendVarValue(fieldValue, fieldVar, refVal, retrieveLayer);
                                }
                            }
                        } catch (Exception e) {
                            handleException(e);
                        }
                    }
                }
            }
        }
        if (parent != null) {
            parent.linkAchild(varValue);
        }
        return varValue;
    }

    private TraceNode findInvokingMatchNode(TraceNode latestNode, String invokingMethodSig) {
        List<TraceNode> candidates = new ArrayList<>();
        if (latestNode.getInvocationParent() != null) {
            candidates = latestNode.getInvocationParent().getInvocationChildren();
        } else {
            candidates = trace.getTopMethodLevelNodes();
        }

        for (int i = candidates.size() - 1; i >= 0; i--) {
            TraceNode prevOver = candidates.get(i);
            if (prevOver.getOrder() != latestNode.getOrder()) {
                String prevOverInvocation = prevOver.getInvokingMethod();
                if (prevOverInvocation != null && prevOverInvocation.equals(invokingMethodSig)) {

                    if (prevOver.getInvokingMatchNode() == null) {
                        return prevOver;
                    }
                }
            }
        }
        System.currentTimeMillis();
        return null;
    }

    private String getStringValue(final Object obj, String type) {
        try {
            if (obj == null) {
                return "null";
            }

            String simpleType = null;
            if (type != null && type.contains("[]")) {
                simpleType = type.substring(0, type.indexOf("[]"));
            }

            if (simpleType != null) {
                if (simpleType.equals("char")) {
                    char[] charArray = (char[]) obj;
                    return String.valueOf(charArray);
                }
            }

            // if (FilterChecker.isCustomizedToStringClass(obj.getClass().getName())) {
            // java.lang.reflect.Method toStringMethod = null;
            // for (java.lang.reflect.Method method : obj.getClass().getDeclaredMethods()) {
            // if (method.getName().equals(TraceInstrumenter.NEW_TO_STRING_METHOD)) {
            // toStringMethod = method;
            // break;
            // }
            // }
            // if (toStringMethod != null) {
            // return (String) toStringMethod.invoke(obj);
            // }
            // }

            if (JavaTracerConfig.getInstance().isAvoidProxyToString() && isProxyClass(
                obj.getClass())) {
                return obj.getClass().getName();
            }

            if (stringValueBlackList.contains(obj.getClass())) {
                return "$unknown (estimated as too cost to have its value)";
            }

            long t1 = System.currentTimeMillis();
            String value = String.valueOf(obj);// obj.toString();
            long t2 = System.currentTimeMillis();
            if (t2 - t1 > 500) {
                stringValueBlackList.add(obj.getClass());
            }

            return value;
        } catch (Throwable t) {
            return null;
        }
    }

    private void handleException(Throwable t) {
        if (t.getMessage() != null) {
            Log.info("ExecutionTracer error: " + t.getMessage(), this.getClass());
        }
    }

    private void initInvokingDetail(Object invokeObj, String invokeTypeSign, String methodSig,
        Object[] params,
        String paramTypeSignsCode, String residingClassName, TraceNode latestNode) {
        boolean exclusive = LoadedClassRecord.getInstance().isExclusive(invokeTypeSign);
        if (exclusive && latestNode.getBreakPoint().getClassCanonicalName()
            .equals(residingClassName)) {
            InvokingDetail invokeDetail = latestNode.getInvokingDetail();
            if (invokeDetail == null) {
                invokeDetail = new InvokingDetail(latestNode);
                latestNode.setInvokingDetail(invokeDetail);
            }
            invokeDetail.initRelevantVars(invokeObj, params, paramTypeSignsCode);
        }
    }

    private boolean isProxyClass(Class<? extends Object> clazz) {
        if (Proxy.isProxyClass(clazz)) {
            return true;
        }
        /* to detect proxy enhancered by CGLIB */
        int enhancerTagIdx = clazz.getName().indexOf("$$");
        if (enhancerTagIdx > 0 && clazz.getName().lastIndexOf("$$", enhancerTagIdx + 1) > 0) {
            return true;
        }
        return false;
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
                java.lang.reflect.Method method = clazz.getDeclaredMethod(methodName,
                    argumentTypes);
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

        private boolean isStatic(final java.lang.reflect.Method method) {
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
