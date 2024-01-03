package org.cophi.javatracer.core;

/**
 * @author LLT
 */
public enum SystemVariables {
    SYS_SAV_JUNIT_JAR("sav.junit.runner.jar"),
    APP_ENABLE_ASSERTION("assertion.enable", "true"),
    SLICE_COLLECT_VAR("slicing.collect.var", "false"),
    //SLICE_BKP_VAR_INHERIT: values([empty], BACKWARD, FORWARD);
    SLICE_BKP_VAR_INHERIT("slicing.collected.vars.inherit", ""),
    FAULT_LOCATE_USE_SLICE("fault.localization.use.slice", "true"),
    FAULT_LOCATE_SPECTRUM_ALGORITHM("fault.localization.spectrum.algorithm", "TARANTULA");
    private final String name;
    private final String defValue;

    SystemVariables(String name) {
        this(name, null);
    }

    SystemVariables(String name, String defValue) {
        this.name = name;
        this.defValue = defValue;
    }

    public String getName() {
        return name;
    }

    public String getDefValue() {
        return defValue;
    }
}