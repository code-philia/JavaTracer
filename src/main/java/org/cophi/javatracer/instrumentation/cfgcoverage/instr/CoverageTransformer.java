package org.cophi.javatracer.instrumentation.cfgcoverage.instr;

import org.cophi.javatracer.instrumentation.AgentLogger;
import org.cophi.javatracer.instrumentation.cfgcoverage.CoverageAgentParams;
import org.cophi.javatracer.instrumentation.filter.GlobalFilterChecker;
import org.cophi.javatracer.instrumentation.instr.AbstractTransformer;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

public class CoverageTransformer implements ClassFileTransformer {

    private final CoverageInstrumenter instrumenter;

    public CoverageTransformer(CoverageAgentParams agentParams) {
        instrumenter = new CoverageInstrumenter(agentParams);
    }

    @Override
    public byte[] transform(ClassLoader loader, String classFName, Class<?> classBeingRedefined,
        ProtectionDomain protectionDomain, byte[] classfileBuffer)
        throws IllegalClassFormatException {

        if (protectionDomain != null) {
            CodeSource codeSource = protectionDomain.getCodeSource();
            if ((codeSource == null) || (codeSource.getLocation() == null)) {
                AgentLogger.debug(
                    String.format("Transformer- Ignore %s [Code source undefined!]", classFName));
                return null;
            }
            URL srcLocation = codeSource.getLocation();
            String path = srcLocation.getFile();
            if (!GlobalFilterChecker.isTransformable(classFName, path, false)
                || !GlobalFilterChecker.isAppClass(classFName)) {
                return null;
            }
            try {
                byte[] data = instrumenter.instrument(classFName, classfileBuffer);
                AbstractTransformer.log(classfileBuffer, data, classFName, false);
                return data;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public CoverageInstrumenter getInstrumenter() {
        return instrumenter;
    }
}
