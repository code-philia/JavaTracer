package org.cophi.javatracer.instrumentation.instrumentator;

import java.io.IOException;

public interface JavaTracerInstrumentator {

    byte[] instrument(final String className, byte[] classfileBuffer) throws IOException;

}
