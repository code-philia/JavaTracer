package instrumentation.cfgcoverage.instr;

public enum CoverageTracerMethods {
    BRANCH_COVERAGE_GET_TRACER(false, "instrumentation/cfgcoverage/runtime/BranchCoverageTracer",
        "_getTracer", "(Ljava/lang/String;)Linstrumentation/cfgcoverage/runtime/ICoverageTracer;",
        2),
    EXIT_METHOD(true, "instrumentation/cfgcoverage/runtime/ICoverageTracer", "_exitMethod",
        "(Ljava/lang/String;Z)V", 3),
    GET_TRACER(false, "instrumentation/cfgcoverage/runtime/CoverageTracer", "_getTracer",
        "(Ljava/lang/String;ZLjava/lang/String;Ljava/lang/String;[Ljava/lang/Object;Ljava/lang/Object;)Linstrumentation/cfgcoverage/runtime/ICoverageTracer;",
        7),
    ON_DCMP(true, "instrumentation/cfgcoverage/runtime/ICoverageTracer", "_onDcmp", "(DD)V", 3),
    ON_FCMP(true, "instrumentation/cfgcoverage/runtime/ICoverageTracer", "_onFcmp", "(FF)V", 3),
    ON_IF(true, "instrumentation/cfgcoverage/runtime/ICoverageTracer", "_onIf",
        "(IZLjava/lang/String;I)V", 5),
    ON_IF_A_CMP(true, "instrumentation/cfgcoverage/runtime/ICoverageTracer", "_onIfACmp",
        "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/String;I)V", 5),
    ON_IF_I_CMP(true, "instrumentation/cfgcoverage/runtime/ICoverageTracer", "_onIfICmp",
        "(IILjava/lang/String;I)V", 5),
    ON_IF_NULL(true, "instrumentation/cfgcoverage/runtime/ICoverageTracer", "_onIfNull",
        "(Ljava/lang/Object;Ljava/lang/String;I)V", 4),
    ON_LCMP(true, "instrumentation/cfgcoverage/runtime/ICoverageTracer", "_onLcmp", "(JJ)V", 3),
    REACH_NODE(true, "instrumentation/cfgcoverage/runtime/ICoverageTracer", "_reachNode",
        "(Ljava/lang/String;I)V", 3);


    private final boolean interfaceMethod;
    private final String declareClass;
    private final String methodName;
    private final String methodSign;
    private final int argNo;

    CoverageTracerMethods(boolean ifaceMethod, String declareClass, String methodName,
        String methodSign, int argNo) {
        this.interfaceMethod = ifaceMethod;
        this.declareClass = declareClass;
        this.methodName = methodName;
        this.methodSign = methodSign;
        this.argNo = argNo;
    }

    public String getDeclareClass() {
        return declareClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodSign() {
        return methodSign;
    }

    public int getArgNo() {
        return argNo;
    }

    public boolean isInterfaceMethod() {
        return interfaceMethod;
    }
}
