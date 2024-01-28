package org.cophi.javatracer.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cophi.javatracer.configs.ProjectConfig;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

@SuppressWarnings("restriction")
public class JavaUtil {

    private static final String TO_STRING_SIGN = "()Ljava/lang/String;";
    private static final String TO_STRING_NAME = "toString";
    public static HashMap<String, CompilationUnit> sourceFile2CUMap = new HashMap<>();

    /**
     * generate signature such as methodName(java.lang.String)L
     *
     * @return
     */
    public static String generateMethodSignature(IMethodBinding mBinding) {
//		IMethodBinding mBinding = md.resolveBinding();

        String returnType = mBinding.getReturnType().getKey();

        String methodName = mBinding.getName();

        List<String> paramTypes = new ArrayList<>();
        for (ITypeBinding tBinding : mBinding.getParameterTypes()) {
            String paramType = tBinding.getKey();
            paramTypes.add(paramType);
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append(methodName);
        buffer.append("(");
        for (String pType : paramTypes) {
            buffer.append(pType);
            //buffer.append(";");
        }

        buffer.append(")");
        buffer.append(returnType);
//
//		String sign = buffer.toString();
//		if(sign.contains(";")){
//			sign = sign.substring(0, sign.lastIndexOf(";")-1);
//		}
//		sign = sign + ")" + returnType;

        String sign = buffer.toString();

        return sign;
    }

    public static String getFullNameOfCompilationUnit(CompilationUnit cu) {

        String packageName = "";
        if (cu.getPackage() != null) {
            packageName = cu.getPackage().getName().toString();
        }
        AbstractTypeDeclaration typeDeclaration = (AbstractTypeDeclaration) cu.types().get(0);
        String typeName = typeDeclaration.getName().getIdentifier();

        if (packageName.length() == 0) {
            return typeName;
        } else {
            return packageName + "." + typeName;
        }

    }

    public static CompilationUnit findCompilationUnitInProject(String qualifiedName,
        ProjectConfig projectConfig) {
        CompilationUnit cu = Settings.compilationUnitMap.get(qualifiedName);
        if (null == cu) {
            try {
                ICompilationUnit icu = findICompilationUnitInProject(qualifiedName);
                if (icu != null) {
                    cu = convertICompilationUnitToASTNode(icu);
                } else {
                    boolean isFound = false;
                    for (String sourceFolder : projectConfig.getAllSourceFolder()) {
                        String fileName = sourceFolder + File.separator + qualifiedName.replace(".",
                            File.separator) + ".java";
                        if (new File(fileName).exists()) {
                            cu = findCompiltionUnitBySourcePath(fileName, qualifiedName);
                            isFound = true;
                            break;
                        }
                    }

                    if (!isFound) {
                        System.err.println("cannot find the source file of " + qualifiedName);
                    }

//					String sourceFile = appPath.getSoureCodePath() + File.separator + qualifiedName.replace(".", File.separator) + ".java";
//					String testFile = appPath.getTestCodePath() + File.separator + qualifiedName.replace(".", File.separator) + ".java";
//
//					if(new File(sourceFile).exists()) {
//						cu = findCompiltionUnitBySourcePath(sourceFile, qualifiedName);
//					}
//					else if(new File(testFile).exists()) {
//						cu = findCompiltionUnitBySourcePath(testFile, qualifiedName);
//					}
//					else {
//						System.err.println("cannot find the source file of " + qualifiedName);
//					}

                }

                Settings.compilationUnitMap.put(qualifiedName, cu);
                return cu;
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        return cu;
    }

//    public static CompilationUnit findNonCacheCompilationUnitInProject(String qualifiedName,
//        AppJavaClassPath appPath) {
//        ICompilationUnit icu = findNonCacheICompilationUnitInProject(qualifiedName);
//        CompilationUnit cu = null;
//        if (icu != null) {
//            cu = convertICompilationUnitToASTNode(icu);
//        } else {
//            String sourceFile =
//                appPath.getSoureCodePath() + File.separator + qualifiedName.replace(".",
//                    File.separator) + ".java";
//            String testFile =
//                appPath.getTestCodePath() + File.separator + qualifiedName.replace(".",
//                    File.separator) + ".java";
//
//            if (new File(sourceFile).exists()) {
//                cu = findCompiltionUnitBySourcePath(sourceFile, qualifiedName);
//            } else if (new File(testFile).exists()) {
//                cu = findCompiltionUnitBySourcePath(testFile, qualifiedName);
//            } else {
//                System.err.println("cannot find the source file of " + qualifiedName);
//            }
//        }
//
//        return cu;
//    }

    public static ICompilationUnit findICompilationUnitInProject(String qualifiedName) {
        return findICompilationUnitInProject(qualifiedName, Settings.projectName);
    }

    public static ICompilationUnit findICompilationUnitInProject(String qualifiedName,
        String projectName) {
        ICompilationUnit icu = Settings.iCompilationUnitMap.get(qualifiedName);
        if (null == icu) {
            IProject iProject = getSpecificJavaProjectInWorkspace(projectName);
            if (iProject != null) {
                IJavaProject project = JavaCore.create(iProject);
                try {
                    IType type = project.findType(qualifiedName);
                    if (type == null) {
                        type = project.findType(qualifiedName, new NullProgressMonitor());
                    }

                    if (type != null) {
                        icu = type.getCompilationUnit();
                        Settings.iCompilationUnitMap.put(qualifiedName, icu);
                    }

                } catch (JavaModelException e1) {
                    //System.out.println(e1);
                }
            }
        }

        return icu;
    }

    public static ICompilationUnit findNonCacheICompilationUnitInProject(String qualifiedName) {
        return findNonCacheICompilationUnitInProject(qualifiedName, Settings.projectName);
    }

    public static ICompilationUnit findNonCacheICompilationUnitInProject(String qualifiedName,
        String projectName) {
        IProject iProject = getSpecificJavaProjectInWorkspace(projectName);
        if (iProject != null) {
            IJavaProject project = JavaCore.create(iProject);
            try {
                IType type = project.findType(qualifiedName);
                if (type != null) {
                    return type.getCompilationUnit();
                }

            } catch (JavaModelException e1) {
                e1.printStackTrace();
            }

        }

        return null;
    }

//    public static IPackageFragmentRoot findTestPackageRootInProject() {
//        return findTestPackageRootInProject(Settings.projectName);
//    }
//
//    public static IPackageFragmentRoot findTestPackageRootInProject(String projectName) {
//        IJavaProject project = JavaCore.create(getSpecificJavaProjectInWorkspace(projectName));
//        try {
//            for (IPackageFragmentRoot packageFragmentRoot : project.getPackageFragmentRoots()) {
//                if (!(packageFragmentRoot instanceof JarPackageFragmentRoot)
//                    && packageFragmentRoot.getResource().toString().contains("test")) {
//
//                    return packageFragmentRoot;
////					IPackageFragment packageFrag = packageFragmentRoot.getPackageFragment(packageName);
////
////					String fragName = packageFrag.getElementName();
////					if(packageFrag.exists() && fragName.equals(packageName)){
////						return packageFrag;
////					}
//
//                }
//            }
//
//        } catch (JavaModelException e1) {
//            e1.printStackTrace();
//        }
//
//        return null;
//    }

    @SuppressWarnings({"rawtypes", "deprecation"})
    public static CompilationUnit convertICompilationUnitToASTNode(ICompilationUnit iunit) {
        ASTParser parser = ASTParser.newParser(AST.JLS4);
        Map options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
        parser.setCompilerOptions(options);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        parser.setSource(iunit);

        CompilationUnit cu = null;
        try {
            cu = (CompilationUnit) parser.createAST(null);
            return cu;
        } catch (java.lang.IllegalStateException e) {
            return null;
        }
    }

    public static IProject getSpecificJavaProjectInWorkspace() {
        return getSpecificJavaProjectInWorkspace(Settings.projectName);
    }

    public static IProject getSpecificJavaProjectInWorkspace(String projectName) {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        IProject[] projects = root.getProjects();

        for (int i = 0; i < projects.length; i++) {
            if (projects[i].getName().equals(projectName)) {
                return projects[i];
            }
        }
        return null;
    }

//    public static boolean isTheLocationHeadOfClass(String sourceName, int lineNumber,
//        AppJavaClassPath appPath) {
//        CompilationUnit cu = findCompilationUnitInProject(sourceName, appPath);
//        AbstractTypeDeclaration type = (AbstractTypeDeclaration) cu.types().get(0);
//        int headLine = cu.getLineNumber(type.getName().getStartPosition());
//
//        return headLine == lineNumber;
//    }
//
//    public static boolean isCompatibleMethodSignature(String thisSig, String thatSig,
//        AppJavaClassPath appPath) {
//        if (thatSig.equals(thisSig)) {
//            return true;
//        }
//
//        String thisClassName = thisSig.substring(0, thisSig.indexOf("#"));
//        String thisMethodSig = thisSig.substring(thisSig.indexOf("#") + 1, thisSig.length());
//
//        String thatClassName = thatSig.substring(0, thatSig.indexOf("#"));
//        String thatMethodSig = thatSig.substring(thatSig.indexOf("#") + 1, thatSig.length());
//
//        if (thisMethodSig.equals(thatMethodSig)) {
//            CompilationUnit thisCU = JavaUtil.findCompilationUnitInProject(thisClassName, appPath);
//            CompilationUnit thatCU = JavaUtil.findCompilationUnitInProject(thatClassName, appPath);
//
//            if (thisCU == null || thatCU == null) {
//                return true;
//            }
//
//            AbstractTypeDeclaration thisType = (AbstractTypeDeclaration) thisCU.types().get(0);
//            AbstractTypeDeclaration thatType = (AbstractTypeDeclaration) thatCU.types().get(0);
//
//            ITypeBinding thisTypeBinding = thisType.resolveBinding();
//            ITypeBinding thatTypeBinding = thatType.resolveBinding();
//
//            boolean isSame = thisTypeBinding.getQualifiedName()
//                .equals(thatTypeBinding.getQualifiedName());
//
//            if (isSame) {
//                return true;
//            } else {
//                boolean isCom1 = thisTypeBinding.isSubTypeCompatible(thatTypeBinding);
//                boolean isCom2 = thatTypeBinding.isSubTypeCompatible(thisTypeBinding);
//
//                return isCom1 || isCom2;
//            }
//        }
//
//        return false;
//    }

//    /**
//     * If the prevNode is the invocation parent of postNode, this method return the method binding
//     * of the corresponding method.
//     *
//     * @param prevNode
//     * @param postNode
//     * @return
//     */
//    public static MethodDeclaration checkInvocationParentRelation(TraceNode prevNode,
//        TraceNode postNode, AppJavaClassPath appPath) {
//        List<IMethodBinding> methodInvocationBindings = findMethodInvocations(prevNode, appPath);
//        if (!methodInvocationBindings.isEmpty()) {
//            MethodDeclaration md = findMethodDeclaration(postNode, appPath);
//            if (md == null) {
//                return null;
//            }
//            IMethodBinding methodDeclarationBinding = md.resolveBinding();
//
//            if (canFindCompatibleSig(methodInvocationBindings, methodDeclarationBinding, appPath)) {
//                //return methodDeclarationBinding;
//                return md;
//            }
//        }
//
//        return null;
//    }
//
//    private static List<IMethodBinding> findMethodInvocations(TraceNode prevNode,
//        AppJavaClassPath appPath) {
//        CompilationUnit cu = JavaUtil.findCompilationUnitInProject(
//            prevNode.getDeclaringCompilationUnitName(), appPath);
//
//        MethodInvocationFinder finder = new MethodInvocationFinder(cu, prevNode.getLineNumber());
//        cu.accept(finder);
//
//        List<IMethodBinding> methodInvocations = new ArrayList<>();
//
//        List<MethodInvocation> invocations = finder.getInvocations();
//        for (MethodInvocation invocation : invocations) {
//            IMethodBinding mBinding = invocation.resolveMethodBinding();
//
//            methodInvocations.add(mBinding);
//
//        }
//
//        return methodInvocations;
//    }
//
//    private static MethodDeclaration findMethodDeclaration(TraceNode postNode,
//        AppJavaClassPath appPath) {
//        CompilationUnit cu = JavaUtil.findCompilationUnitInProject(
//            postNode.getDeclaringCompilationUnitName(), appPath);
//
//        MethodDeclarationFinder finder = new MethodDeclarationFinder(cu, postNode.getLineNumber());
//        cu.accept(finder);
//
//        MethodDeclaration md = finder.getMethod();
//
//        return md;
//    }

    public static String convertFullSignature(IMethodBinding binding) {

        String className = binding.getDeclaringClass().getBinaryName();
        String methodSig = generateMethodSignature(binding);

        return className + "#" + methodSig;
    }

//    private static boolean canFindCompatibleSig(
//        List<IMethodBinding> methodInvocationBindings, IMethodBinding methodDeclarationBinding,
//        AppJavaClassPath appPath) {
//
//        List<String> methodInvocationSigs = new ArrayList<>();
//        for (IMethodBinding binding : methodInvocationBindings) {
//            String sig = convertFullSignature(binding);
//            methodInvocationSigs.add(sig);
//        }
//        String methodDeclarationSig = convertFullSignature(methodDeclarationBinding);
//
//        if (methodInvocationSigs.contains(methodDeclarationSig)) {
//            return true;
//        } else {
//            for (String methodInvocationSig : methodInvocationSigs) {
//                if (isCompatibleMethodSignature(methodInvocationSig, methodDeclarationSig,
//                    appPath)) {
//                    return true;
//                }
//            }
//        }
//
//        System.currentTimeMillis();
//
//        return false;
//    }
//
//    public static String createSignature(String className, String methodName, String methodSig) {
//        String sig = className + "#" + methodName + methodSig;
//        return sig;
//    }

    public static CompilationUnit findCompiltionUnitBySourcePath(String javaFilePath,
        String declaringCompilationUnitName) {

        CompilationUnit parsedCU = sourceFile2CUMap.get(javaFilePath);
        if (parsedCU != null) {
            return parsedCU;
        }

        File javaFile = new File(javaFilePath);

        if (javaFile.exists()) {

            String contents;
            try {
                contents = new String(Files.readAllBytes(Paths.get(javaFilePath)));

                final ASTParser parser = ASTParser.newParser(AST.JLS8);
                parser.setKind(ASTParser.K_COMPILATION_UNIT);
                parser.setSource(contents.toCharArray());
                parser.setResolveBindings(true);

                CompilationUnit cu = (CompilationUnit) parser.createAST(null);
                sourceFile2CUMap.put(javaFilePath, cu);

                return cu;

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.print(
                "cannot find " + declaringCompilationUnitName + " under " + javaFilePath);
        }

        return null;
    }
}