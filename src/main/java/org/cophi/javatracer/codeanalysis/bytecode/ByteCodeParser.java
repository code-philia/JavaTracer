package org.cophi.javatracer.codeanalysis.bytecode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.DescendingVisitor;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.SyntheticRepository;
import org.cophi.javatracer.configs.AppJavaClassPath;

public class ByteCodeParser {

    public static void parse(String className, ByteCodeMethodFinder visitor,
        AppJavaClassPath appClassPath) {
        String originalSystemClassPath = System.getProperty("java.class.path");
        String[] paths = originalSystemClassPath.split(File.pathSeparator);

        try {
            List<String> pathList = new ArrayList<>();
            Collections.addAll(pathList, paths);

            StringBuffer buffer = new StringBuffer(originalSystemClassPath);
            for (String classPath : appClassPath.getClasspaths()) {
                if (!pathList.contains(classPath)) {
                    buffer.append(File.pathSeparator + classPath);
                }
            }
            buffer.append(File.pathSeparator);
            String jdkPath = appClassPath.getJavaHome() + File.separator + "jre" +
                File.separator + "lib" + File.separator + "rt.jar";
            buffer.append(jdkPath);
            System.setProperty("java.class.path", buffer.toString());
            String s = System.getProperty("java.class.path");

            ClassPath0 classPath = new ClassPath0(s);
            Repository.setRepository(SyntheticRepository.getInstance(classPath));

            JavaClass clazz = Repository.lookupClass(className);
            clazz.accept(new DescendingVisitor(clazz, visitor));
            visitor.setJavaClass(clazz);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.setProperty("java.class.path", originalSystemClassPath);
        }

    }
}
