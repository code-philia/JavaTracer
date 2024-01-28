package org.cophi.javatracer.recommendation;


import org.cophi.javatracer.utils.Settings;

public class DetailInspectorFactory {

    public static DetailInspector createInspector() {
        if (Settings.isApplyAdvancedInspector) {
            return new DataOmissionInspector();
        } else {
            return new SimpleDetailInspector();
        }
    }
}
