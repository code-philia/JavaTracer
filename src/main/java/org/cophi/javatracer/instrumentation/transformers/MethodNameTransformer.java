package org.cophi.javatracer.instrumentation.transformers;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.cophi.javatracer.configs.ProjectConfig;
import org.cophi.javatracer.log.Log;
import org.cophi.javatracer.log.LogType;
import org.cophi.javatracer.utils.ClassNameUtils;

public class MethodNameTransformer implements ClassFileTransformer {

    protected final ProjectConfig projectConfig;

    public MethodNameTransformer(final ProjectConfig config) {
        this.projectConfig = config;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
        ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        String targetClassName =
            this.projectConfig.isRunningTestCase() ? this.projectConfig.getTestCase().testClassName
                : this.projectConfig.getLaunchClass();
        targetClassName = ClassNameUtils.canonicalToBinaryName(targetClassName);
        byte[] resultBuffer = classfileBuffer;
        if (targetClassName.equals(className)) {
            resultBuffer = this.insertMethodName(classfileBuffer);
        }
        return resultBuffer;
    }

    protected byte[] insertMethodName(final byte[] classfileBuffer) {
        try {
            ClassParser parser = new ClassParser(new ByteArrayInputStream(classfileBuffer), null);
            JavaClass javaClass = parser.parse();
            ConstantPoolGen constantPoolGen = new ConstantPoolGen(javaClass.getConstantPool());
            InstructionFactory factory = new InstructionFactory(constantPoolGen);

            ClassGen classGen = new ClassGen(javaClass);
            for (Method method : javaClass.getMethods()) {
                MethodGen methodGen = new MethodGen(method, javaClass.getClassName(),
                    constantPoolGen);
                InstructionList originalList = methodGen.getInstructionList();
                originalList.insert(factory.createPrintln(
                    Log.genMessage("Method Name: " + method.getName(), this.getClass(),
                        LogType.INFO)
                ));

                methodGen.setInstructionList(originalList);
                methodGen.setMaxStack();

                classGen.replaceMethod(method, methodGen.getMethod());
                originalList.dispose();
            }

            classGen.setConstantPool(constantPoolGen);
            return classGen.getJavaClass().getBytes();
        } catch (Exception e) {
            e.printStackTrace();
            return classfileBuffer;
        }
    }

}
