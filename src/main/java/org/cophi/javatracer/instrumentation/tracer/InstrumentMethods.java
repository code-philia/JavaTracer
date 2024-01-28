package org.cophi.javatracer.instrumentation.tracer;

public interface InstrumentMethods {

    int getArgumentNumber();

    String getDeclareClassBinaryName();

    String getDescriptor();

    String getMethodName();
}
