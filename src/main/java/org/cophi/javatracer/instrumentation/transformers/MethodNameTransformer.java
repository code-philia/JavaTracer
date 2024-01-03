package org.cophi.javatracer.instrumentation.transformers;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class MethodNameTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
        ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        System.out.println(className);
        return classfileBuffer;
    }

}
