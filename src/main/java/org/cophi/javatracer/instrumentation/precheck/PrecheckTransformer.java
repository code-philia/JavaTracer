package org.cophi.javatracer.instrumentation.precheck;

import org.cophi.javatracer.instrumentation.AgentLogger;
import org.cophi.javatracer.instrumentation.AgentParams;
import org.cophi.javatracer.instrumentation.filter.GlobalFilterChecker;
import org.cophi.javatracer.instrumentation.instr.AbstractTransformer;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

public class PrecheckTransformer extends AbstractTransformer implements ClassFileTransformer {

    private final PrecheckInstrumenter instrumenter;
    private final List<String> loadedClasses = new ArrayList<>();

    public PrecheckTransformer(AgentParams params) {
        instrumenter = new PrecheckInstrumenter(params);
    }

    @Override
    protected byte[] doTransform(ClassLoader loader, String classFName,
        Class<?> classBeingRedefined,
        ProtectionDomain protectionDomain, byte[] classfileBuffer)
        throws IllegalClassFormatException {
        try {
            loadedClasses.add(classFName.replace("/", "."));
            if (protectionDomain != null) {
                CodeSource codeSource = protectionDomain.getCodeSource();
                if ((codeSource == null) || (codeSource.getLocation() == null)) {
                    AgentLogger.debug(
                        String.format("Transformer- Ignore %s [Code source undefined!]",
                            classFName));
                    return null;
                }
                URL srcLocation = codeSource.getLocation();
                String path = srcLocation.getFile();
                if (!GlobalFilterChecker.isTransformable(classFName, path, false)
                    || !GlobalFilterChecker.isAppClass(classFName)) {
                    return null;
                }
                byte[] data = instrumenter.instrument(classFName, classfileBuffer);
                return data;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getExceedingLimitMethods() {
        return instrumenter.getExceedLimitMethods();
    }

    public List<String> getLoadedClasses() {
        return loadedClasses;
    }
}
