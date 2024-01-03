package org.cophi.javatracer.instrumentation.instr;

import org.cophi.javatracer.instrumentation.AgentParams;
import org.cophi.javatracer.instrumentation.filter.GlobalFilterChecker;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * @author Yun Lin
 */
public class TraceTransformer extends AbstractTransformer implements ClassFileTransformer {

    private final TraceInstrumenter instrumenter;

    public TraceTransformer(AgentParams params) {
        instrumenter = new TraceInstrumenter(params);
    }

    @Override
    protected byte[] doTransform(ClassLoader loader, String classFName,
        Class<?> classBeingRedefined,
        ProtectionDomain protectionDomain, byte[] classfileBuffer)
        throws IllegalClassFormatException {
        /* bootstrap classes */
        if ((loader == null) || (protectionDomain == null)) {
            if (!GlobalFilterChecker.isTransformable(classFName, null, true)) {
                return null;
            }
        }
        if (protectionDomain != null) {
            String path = protectionDomain.getCodeSource().getLocation().getFile();
            if (!GlobalFilterChecker.isTransformable(classFName, path, false)) {
                return null;
            }
        }

        /* do instrumentation */
        try {
            return instrumenter.instrument(classFName, classfileBuffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public TraceInstrumenter getInstrumenter() {
        return this.instrumenter;
    }

}
