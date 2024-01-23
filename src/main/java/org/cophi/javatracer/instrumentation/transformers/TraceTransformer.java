package org.cophi.javatracer.instrumentation.transformers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.cophi.javatracer.configs.ProjectConfig;
import org.cophi.javatracer.instrumentation.filters.JavaTracerFilter;
import org.cophi.javatracer.instrumentation.instrumentator.JavaTracerInstrumentator;
import org.cophi.javatracer.instrumentation.instrumentator.TraceInstrumentator;
import org.cophi.javatracer.instrumentation.tracer.factories.ExecutionTracerFactory;
import org.cophi.javatracer.log.Log;

public class TraceTransformer extends AbstractTransformer {

    protected final JavaTracerInstrumentator instrumentator;

    public TraceTransformer(final ProjectConfig projectConfig) {
        super(projectConfig);
        this.instrumentator = new TraceInstrumentator(projectConfig);
        ExecutionTracerFactory.getInstance().setProjectConfig(projectConfig);
    }

    /**
     * Check if the class is a bootstrap class. If the class is a bootstrap class, the loader is
     * null.
     *
     * @param loader the class loader
     * @return {@code true} if the class is a bootstrap class, {@code false} otherwise
     */
    protected boolean isBoostrapClass(final ClassLoader loader) {
        return loader == null;
    }

    @Override
    protected byte[] transform_(ClassLoader loader, String className, Class<?> classBeingRedefined,
        java.security.ProtectionDomain protectionDomain, byte[] classfileBuffer) {

        // Check if the given class pass the filter or not
        final String sourcePath = this.getSourcePathFromProtectionDomain(protectionDomain);
        if (!JavaTracerFilter.getInstance().isInstrumentableClass(className, sourcePath)) {
            return classfileBuffer;
        }

        try {
            byte[] data = this.instrumentator.instrument(className, classfileBuffer);
            final String folderPath = "C:\\Users\\WYK\\Desktop\\temp2";
            final String fileName = className + ".class";
            final Path path = Paths.get(folderPath, fileName);
            Files.write(path, data);
            return data;
        } catch (IOException e) {
            Log.error(e.getMessage(), this.getClass());
            return classfileBuffer;
        }
    }

}
