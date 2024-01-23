package org.cophi.javatracer.instrumentation.transformers;

import java.lang.instrument.ClassFileTransformer;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import org.cophi.javatracer.configs.ProjectConfig;
import org.cophi.javatracer.instrumentation.tracer.TracerManager;
import org.cophi.javatracer.instrumentation.tracer.TracerState;
import org.cophi.javatracer.utils.ClassNameUtils;

public abstract class AbstractTransformer implements ClassFileTransformer {

    protected final ProjectConfig projectConfig;

    public AbstractTransformer(final ProjectConfig projectConfig) {
        this.projectConfig = projectConfig;
    }
    
    @Override
    public final byte[] transform(final ClassLoader loader, final String classURIName,
        final Class<?> classBeingRedefined,
        final ProtectionDomain protectionDomain, final byte[] classfileBuffer) {
        TracerManager manager = TracerManager.getInstance();
        if (manager.getState() == TracerState.SHUTDOWN) {
            return null;
        }

        if (classURIName == null) {
            return null;
        }

        /*
         * The reason we need to lock and unlock the tracer:
         * when a method which is being traced invoke a another method which class is required to be loaded,
         * we only want to trace inside that invoked method not in class transformer, that's why we lock to
         * prevent tracing here.
         * */
        final long threadId = Thread.currentThread().getId();
        boolean needToReleaseLock = !manager.lock(threadId);

        byte[] data = this.transform_(loader,
            ClassNameUtils.classURINameToCanonicalName(classURIName), classBeingRedefined,
            protectionDomain,
            classfileBuffer);

        if (needToReleaseLock) {
            manager.unlock(threadId);
        }

        return data;
    }

    protected String getSourcePathFromProtectionDomain(final ProtectionDomain protectionDomain) {
        if (protectionDomain != null) {
            URL srcLocation = protectionDomain.getCodeSource().getLocation();
            try {
                return Paths.get(srcLocation.toURI()).toString();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    protected abstract byte[] transform_(final ClassLoader loader, final String className,
        final Class<?> classBeingRedefined,
        final java.security.ProtectionDomain protectionDomain, final byte[] classfileBuffer);
}
