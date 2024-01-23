package org.cophi.javatracer.instrumentation.tracer;

public interface InstrumentMethods {

    int getArgumentNumber();

    String getDeclareClassName();

    String getDescriptor();

    String getMethodName();
}
