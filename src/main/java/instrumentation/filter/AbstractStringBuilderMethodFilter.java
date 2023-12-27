package instrumentation.filter;

import instrumentation.instr.instruction.info.LineInstructionInfo;
import java.util.Iterator;
import java.util.List;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.Type;
import utils.CollectionUtils;

/**
 * @author lyly
 */
public class AbstractStringBuilderMethodFilter extends AbstractUserFilter {

    @Override
    public void filter(List<LineInstructionInfo> lineInsnInfos, String className, Method method) {
        if (!needToApplyFilter(className, method)) {
            return;
        }
        for (LineInstructionInfo lineInfo : lineInsnInfos) {
            Iterator<InstructionHandle> it = lineInfo.getInvokeInstructions().iterator();
            while (it.hasNext()) {
                String methodName = ((InvokeInstruction) it.next().getInstruction()).getMethodName(
                    lineInfo.getConstPool());
                if (CollectionUtils.existIn(methodName, "ensureCapacityInternal",
                    "getChars", "stringSize")) {
                    it.remove();
                }
            }
        }

    }

    private boolean needToApplyFilter(String className, Method method) {
        return "java.lang.AbstractStringBuilder".equals(className)
            && "append".equals(method.getName())
            && Type.INT.equals(method.getArgumentTypes()[0]);
    }

}
