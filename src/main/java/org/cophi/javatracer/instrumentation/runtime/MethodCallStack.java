package org.cophi.javatracer.instrumentation.runtime;

import java.util.Stack;
import org.cophi.javatracer.configs.ProjectConfig;
import org.cophi.javatracer.model.trace.TraceNode;

public class MethodCallStack {

    Stack<TraceNode> stack = new Stack<>();

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public synchronized TraceNode peek() {
        if (stack.isEmpty()) {
            return null;
        }
        return stack.peek();
    }

    /**
     * return whether we need to change the invocation layer structure by exception
     *
     * @param methodSignature
     * @return
     */
    public boolean popForException(String methodSignature, ProjectConfig projectConfig) {
        if (!stack.isEmpty()) {
            int popLayer = 0;
            boolean needPop = false;

            if (!stack.isEmpty()) {
                TraceNode caller = stack.peek();
                String m = caller.getInvokingMethod();

                if (m == null || m.equals(methodSignature)) {
                    return false;
                }
                System.currentTimeMillis();
            }

            for (int i = stack.size() - 1; i >= 0; i--) {
                TraceNode caller = stack.get(i);
                popLayer++;
                if (caller.getMethodSign().equals(methodSignature)) {
                    needPop = true;
                    break;
                }
            }

            String enterMethodString = projectConfig.getTestCase().testClassName + "#"
                + projectConfig.getTestCase().testMethodName;
            if (methodSignature.contains(enterMethodString)) {
                needPop = true;
            }

            if (needPop) {
                for (int i = 0; i < popLayer; i++) {
                    stack.pop();
                }

                return true;
            }
        }

        return false;
    }

    public TraceNode push(TraceNode node) {
        return stack.push(node);
    }

    public TraceNode safePop() {
        if (stack.size() != 0) {
            return stack.pop();
        }
        return null;
    }

}