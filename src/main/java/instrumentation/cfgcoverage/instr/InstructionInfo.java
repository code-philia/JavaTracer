package instrumentation.cfgcoverage.instr;

import org.apache.bcel.generic.InstructionHandle;

public class InstructionInfo {

    private final InstructionHandle insnHandler;
    private final int insnIdx;
    private boolean isNotIntCmpIf;

    public InstructionInfo(InstructionHandle insnHandler, int insnIdx) {
        this.insnHandler = insnHandler;
        this.insnIdx = insnIdx;
    }

    public InstructionHandle getInsnHandler() {
        return insnHandler;
    }

    public int getInsnIdx() {
        return insnIdx;
    }

    public boolean isNotIntCmpIf() {
        return isNotIntCmpIf;
    }

    public void setNotIntCmpIf(boolean isNotIntCmpIf) {
        this.isNotIntCmpIf = isNotIntCmpIf;
    }
}
